import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class HostServer {
    private final ConcurrentMap<Integer, ClientSession> sessions = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, PlayerState> playerStates = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, PlayerScore> playerScores = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, String> playerNames = new ConcurrentHashMap<>();

    private final Random random = new Random();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger dbFlushCount = new AtomicInteger(0);
    private final AtomicInteger dbReadbackCount = new AtomicInteger(0);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
    private final ExecutorService clientExecutor = Executors.newCachedThreadPool();

    private final CollectibleState collectible = new CollectibleState();

    private volatile ServerSocket serverSocket;
    private volatile DatabaseManager databaseManager;
    private volatile boolean matchRunning;

    public void start() throws Exception {
        if (!running.compareAndSet(false, true)) {
            return;
        }

        DatabaseManager db = new DatabaseManager();
        db.startForHost();
        databaseManager = db;

        loadScoresFromDb();

        serverSocket = new ServerSocket(AppConfig.GAME_PORT);
        Thread acceptThread = new Thread(this::acceptLoop, "host-accept-loop");
        acceptThread.setDaemon(true);
        acceptThread.start();

        scheduler.scheduleAtFixedRate(this::safeDbFlush, AppConfig.DB_FLUSH_MS, AppConfig.DB_FLUSH_MS, TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(this::safeDbReadback, AppConfig.DB_READBACK_MS, AppConfig.DB_READBACK_MS, TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(this::safeCollectibleLoop, AppConfig.COLLECTIBLE_CHECK_MS, AppConfig.COLLECTIBLE_CHECK_MS, TimeUnit.MILLISECONDS);

        System.out.println("[HOST] Server listening on port " + AppConfig.GAME_PORT);
    }

    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException ignored) {
        }

        for (ClientSession session : new ArrayList<>(sessions.values())) {
            session.close();
        }
        sessions.clear();

        scheduler.shutdownNow();
        clientExecutor.shutdownNow();

        if (databaseManager != null) {
            databaseManager.stop();
            databaseManager = null;
        }

        System.out.println("[HOST] Server stopped");
    }

    private void acceptLoop() {
        while (running.get()) {
            try {
                Socket socket = serverSocket.accept();
                socket.setTcpNoDelay(true);
                registerIncomingClient(socket);
            } catch (IOException ex) {
                if (running.get()) {
                    System.err.println("[HOST] Accept error: " + ex.getMessage());
                }
            }
        }
    }

    private synchronized void registerIncomingClient(Socket socket) {
        int playerId = allocatePlayerId();
        if (playerId < 0) {
            rejectWhenFull(socket);
            return;
        }

        try {
            ClientSession session = new ClientSession(socket, playerId);
            sessions.put(playerId, session);

            initializePlayerIfMissing(playerId);

            session.send(GameMessage.of(MessageType.ASSIGN, playerId));
            sendFullStateTo(session);
            updateLobbyAndMatchState();

            clientExecutor.submit(session);
            System.out.println("[HOST] Player " + playerId + " connected");
        } catch (IOException ex) {
            System.err.println("[HOST] Failed to register player: " + ex.getMessage());
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void rejectWhenFull(Socket socket) {
        try (
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)
        ) {
            out.println(GameMessage.of(MessageType.ERROR, "Lobby is full (3/3)").encode());
        } catch (IOException ignored) {
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private int allocatePlayerId() {
        for (int id = 1; id <= AppConfig.MAX_PLAYERS; id++) {
            if (!sessions.containsKey(id)) {
                return id;
            }
        }
        return -1;
    }

    private void initializePlayerIfMissing(int playerId) {
        playerStates.computeIfAbsent(playerId, id -> {
            PlayerState state = new PlayerState(id);
            applySpawnPosition(state);
            return state;
        });

        playerScores.computeIfAbsent(playerId, id -> {
            PlayerScore score = new PlayerScore();
            score.matchPoints = 0;
            score.lifetimePoints = 0;
            return score;
        });

        playerNames.putIfAbsent(playerId, "Player-" + playerId);
    }

    private void applySpawnPosition(PlayerState state) {
        switch (state.playerId) {
            case 1:
                state.x = 0.0;
                state.z = 0.0;
                break;
            case 2:
                state.x = -5.0;
                state.z = -2.0;
                break;
            case 3:
                state.x = 5.0;
                state.z = -2.0;
                break;
            default:
                state.x = 0.0;
                state.z = 0.0;
                break;
        }
        state.y = 0.0;
        state.heading = 0.0;
        state.lastUpdateMs = System.currentTimeMillis();
    }

    private void updateLobbyAndMatchState() {
        boolean nowRunning = sessions.size() == AppConfig.MAX_PLAYERS;
        if (nowRunning && !matchRunning) {
            matchRunning = true;
            onMatchStarted();
        } else if (!nowRunning && matchRunning) {
            matchRunning = false;
            onMatchPaused();
        }
        broadcastLobby();
    }

    private void onMatchStarted() {
        for (Map.Entry<Integer, PlayerScore> entry : playerScores.entrySet()) {
            entry.getValue().matchPoints = 0;
            broadcastScore(entry.getKey());
        }

        try {
            databaseManager.resetMatchScores();
        } catch (Exception ex) {
            System.err.println("[HOST] Could not reset match scores: " + ex.getMessage());
        }

        collectible.active = false;
        collectible.lastCollectedMs = System.currentTimeMillis();
        broadcastCollectible();

        System.out.println("[HOST] Match started (3/3 players connected)");
    }

    private void onMatchPaused() {
        collectible.active = false;
        broadcastCollectible();
        System.out.println("[HOST] Match paused (waiting for 3 players)");
    }

    private void sendFullStateTo(ClientSession session) {
        for (Map.Entry<Integer, PlayerState> entry : playerStates.entrySet()) {
            PlayerState state = entry.getValue();
            PlayerScore score = playerScores.get(entry.getKey());
            int matchPoints = score == null ? 0 : score.matchPoints;
            int lifetimePoints = score == null ? 0 : score.lifetimePoints;

            session.send(GameMessage.of(
                    MessageType.SNAPSHOT,
                    state.playerId,
                    state.x,
                    state.y,
                    state.z,
                    state.heading,
                    state.lastUpdateMs,
                    matchPoints,
                    lifetimePoints
            ));
        }

        session.send(GameMessage.of(
                MessageType.COLLECTIBLE,
                collectible.active ? 1 : 0,
                collectible.x,
                collectible.y,
                collectible.z
        ));

        session.send(GameMessage.of(
                MessageType.LOBBY,
                sessions.size(),
                AppConfig.MAX_PLAYERS,
                matchRunning ? 1 : 0,
                dbFlushCount.get(),
                dbReadbackCount.get()
        ));
    }

    private void safeDbFlush() {
        if (!running.get() || databaseManager == null) {
            return;
        }
        try {
            for (int playerId = 1; playerId <= AppConfig.MAX_PLAYERS; playerId++) {
                PlayerState state = playerStates.get(playerId);
                PlayerScore score = playerScores.get(playerId);
                String name = playerNames.getOrDefault(playerId, "Player-" + playerId);
                if (state == null || score == null) {
                    continue;
                }
                databaseManager.upsertPlayer(playerId, name);
                databaseManager.upsertPosition(state);
                databaseManager.upsertMatchScore(playerId, score.matchPoints);
                databaseManager.upsertLifetimeScore(playerId, score.lifetimePoints);
            }
            dbFlushCount.incrementAndGet();
        } catch (Exception ex) {
            System.err.println("[HOST] DB flush error: " + ex.getMessage());
        }
    }

    private void safeDbReadback() {
        if (!running.get() || databaseManager == null) {
            return;
        }
        try {
            Map<Integer, PlayerState> dbPositions = databaseManager.loadLatestPositions();
            for (PlayerState dbState : dbPositions.values()) {
                playerStates.put(dbState.playerId, dbState);
                broadcast(GameMessage.of(
                        MessageType.DBSYNC,
                        dbState.playerId,
                        dbState.x,
                        dbState.y,
                        dbState.z,
                        dbState.heading,
                        dbState.lastUpdateMs
                ));
            }
            dbReadbackCount.incrementAndGet();
            broadcastLobby();
        } catch (Exception ex) {
            System.err.println("[HOST] DB readback error: " + ex.getMessage());
        }
    }

    private void safeCollectibleLoop() {
        if (!running.get() || !matchRunning) {
            return;
        }

        long now = System.currentTimeMillis();
        if (!collectible.active && now - collectible.lastCollectedMs >= AppConfig.COLLECTIBLE_RESPAWN_MS) {
            spawnCollectible();
            return;
        }

        if (!collectible.active) {
            return;
        }

        double radiusSum = AppConfig.PLAYER_COLLISION_RADIUS + AppConfig.COLLECTIBLE_RADIUS;
        double radiusSquared = radiusSum * radiusSum;

        for (Map.Entry<Integer, ClientSession> entry : sessions.entrySet()) {
            int playerId = entry.getKey();
            PlayerState state = playerStates.get(playerId);
            if (state == null) {
                continue;
            }

            double dx = state.x - collectible.x;
            double dz = state.z - collectible.z;
            double distSquared = (dx * dx) + (dz * dz);
            if (distSquared <= radiusSquared) {
                onCollectibleCollected(playerId);
                return;
            }
        }
    }

    private void spawnCollectible() {
        collectible.active = true;
        collectible.x = randomCoordinate();
        collectible.y = 0.40;
        collectible.z = randomCoordinate();
        collectible.lastSpawnMs = System.currentTimeMillis();

        broadcastCollectible();

        try {
            databaseManager.insertCollectibleEvent("SPAWN", collectible, null);
        } catch (Exception ex) {
            System.err.println("[HOST] Could not persist collectible spawn: " + ex.getMessage());
        }
    }

    private void onCollectibleCollected(int playerId) {
        PlayerScore score = playerScores.computeIfAbsent(playerId, id -> new PlayerScore());
        score.matchPoints += 1;
        score.lifetimePoints += 1;

        collectible.active = false;
        collectible.lastCollectedMs = System.currentTimeMillis();

        broadcastScore(playerId);
        broadcastCollectible();

        try {
            databaseManager.insertCollectibleEvent("COLLECT", collectible, playerId);
            databaseManager.upsertMatchScore(playerId, score.matchPoints);
            databaseManager.upsertLifetimeScore(playerId, score.lifetimePoints);
        } catch (Exception ex) {
            System.err.println("[HOST] Could not persist collectible collection: " + ex.getMessage());
        }
    }

    private double randomCoordinate() {
        double min = -AppConfig.WORLD_HALF_SIZE;
        double max = AppConfig.WORLD_HALF_SIZE;
        return min + (random.nextDouble() * (max - min));
    }

    private void broadcastLobby() {
        broadcast(GameMessage.of(
                MessageType.LOBBY,
                sessions.size(),
                AppConfig.MAX_PLAYERS,
                matchRunning ? 1 : 0,
                dbFlushCount.get(),
                dbReadbackCount.get()
        ));
    }

    private void broadcastCollectible() {
        broadcast(GameMessage.of(
                MessageType.COLLECTIBLE,
                collectible.active ? 1 : 0,
                collectible.x,
                collectible.y,
                collectible.z
        ));
    }

    private void broadcastScore(int playerId) {
        PlayerScore score = playerScores.get(playerId);
        if (score == null) {
            return;
        }
        broadcast(GameMessage.of(
                MessageType.SCORE,
                playerId,
                score.matchPoints,
                score.lifetimePoints
        ));
    }

    private void broadcast(GameMessage message) {
        List<ClientSession> snapshot = new ArrayList<>(sessions.values());
        for (ClientSession session : snapshot) {
            session.send(message);
        }
    }

    private void handleClientMessage(ClientSession session, GameMessage message) {
        if (message == null) {
            return;
        }

        switch (message.getType()) {
            case HELLO:
                handleHello(session, message);
                break;
            case MOVE:
                handleMove(session, message);
                break;
            case DISCONNECT:
                session.close();
                break;
            case PING:
                session.send(GameMessage.of(MessageType.PING, System.currentTimeMillis()));
                break;
            default:
                break;
        }
    }

    private void handleHello(ClientSession session, GameMessage message) {
        String requestedName = message.getField(0, "Player-" + session.playerId);
        String sanitizedName = requestedName == null || requestedName.isBlank()
                ? ("Player-" + session.playerId)
                : requestedName.trim();

        playerNames.put(session.playerId, sanitizedName);

        try {
            databaseManager.upsertPlayer(session.playerId, sanitizedName);
            PlayerScore score = playerScores.computeIfAbsent(session.playerId, id -> new PlayerScore());
            databaseManager.upsertMatchScore(session.playerId, score.matchPoints);
            databaseManager.upsertLifetimeScore(session.playerId, score.lifetimePoints);
        } catch (Exception ex) {
            System.err.println("[HOST] Could not persist player hello: " + ex.getMessage());
        }
    }

    private void handleMove(ClientSession session, GameMessage message) {
        double x = clamp(message.getDouble(0, 0.0), -AppConfig.WORLD_HALF_SIZE, AppConfig.WORLD_HALF_SIZE);
        double y = 0.0;
        double z = clamp(message.getDouble(2, 0.0), -AppConfig.WORLD_HALF_SIZE, AppConfig.WORLD_HALF_SIZE);
        double heading = normalizeHeading(message.getDouble(3, 0.0));
        long now = System.currentTimeMillis();

        PlayerState state = playerStates.computeIfAbsent(session.playerId, PlayerState::new);
        state.x = x;
        state.y = y;
        state.z = z;
        state.heading = heading;
        state.lastUpdateMs = now;

        PlayerScore score = playerScores.computeIfAbsent(session.playerId, id -> new PlayerScore());

        broadcast(GameMessage.of(
                MessageType.MOVE,
                session.playerId,
                state.x,
                state.y,
                state.z,
                state.heading,
                state.lastUpdateMs,
                score.matchPoints,
                score.lifetimePoints
        ));
    }

    private void onClientDisconnected(int playerId) {
        sessions.remove(playerId);
        broadcast(GameMessage.of(MessageType.DISCONNECT, playerId));
        updateLobbyAndMatchState();
        System.out.println("[HOST] Player " + playerId + " disconnected");
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double normalizeHeading(double heading) {
        double twoPi = Math.PI * 2.0;
        double normalized = heading % twoPi;
        if (normalized < 0.0) {
            normalized += twoPi;
        }
        return normalized;
    }

    private void loadScoresFromDb() {
        if (databaseManager == null) {
            return;
        }

        try {
            Map<Integer, Integer> match = databaseManager.loadMatchScores();
            Map<Integer, Integer> lifetime = databaseManager.loadLifetimeScores();

            for (int id = 1; id <= AppConfig.MAX_PLAYERS; id++) {
                PlayerScore score = playerScores.computeIfAbsent(id, key -> new PlayerScore());
                score.matchPoints = match.getOrDefault(id, 0);
                score.lifetimePoints = lifetime.getOrDefault(id, 0);
            }
        } catch (Exception ex) {
            System.err.println("[HOST] Could not load saved scores: " + ex.getMessage());
        }
    }

    private final class ClientSession implements Runnable {
        private final Socket socket;
        private final int playerId;
        private final BufferedReader reader;
        private final PrintWriter writer;

        private final AtomicBoolean closed = new AtomicBoolean(false);

        private ClientSession(Socket socket, int playerId) throws IOException {
            this.socket = socket;
            this.playerId = playerId;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            this.writer = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
        }

        @Override
        public void run() {
            try {
                String line;
                while (!closed.get() && (line = reader.readLine()) != null) {
                    GameMessage message = GameMessage.parse(line);
                    handleClientMessage(this, message);
                }
            } catch (IOException ex) {
                if (!closed.get()) {
                    System.err.println("[HOST] Client read error for player " + playerId + ": " + ex.getMessage());
                }
            } finally {
                close();
            }
        }

        private synchronized void send(GameMessage message) {
            if (closed.get()) {
                return;
            }
            writer.println(message.encode());
        }

        private void close() {
            if (!closed.compareAndSet(false, true)) {
                return;
            }

            try {
                socket.close();
            } catch (IOException ignored) {
            }

            onClientDisconnected(playerId);
        }
    }
}
