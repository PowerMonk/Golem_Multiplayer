import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class GameClient {
    private final String host;
    private final int port;
    private final boolean hostMode;
    private final Runnable onWindowClose;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicLong moveSequence = new AtomicLong(0L);
    private final CountDownLatch assignedLatch = new CountDownLatch(1);

    private final ConcurrentMap<Integer, PlayerState> players = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, PlayerScore> scores = new ConcurrentHashMap<>();
    private final CollectibleState collectible = new CollectibleState();

    private final ExecutorService readerExecutor = Executors.newSingleThreadExecutor();
    private final ScheduledExecutorService tickScheduler = Executors.newSingleThreadScheduledExecutor();

    private volatile Socket socket;
    private volatile BufferedReader reader;
    private volatile PrintWriter writer;

    private volatile int localPlayerId = -1;
    private volatile int connectedPlayers = 0;
    private volatile int requiredPlayers = AppConfig.MAX_PLAYERS;
    private volatile boolean matchRunning = false;
    private volatile int dbFlushCount = 0;
    private volatile int dbReadbackCount = 0;

    private volatile GameWindow window;
    private volatile long tickCounter;

    public GameClient(String host, int port, boolean hostMode, Runnable onWindowClose) {
        this.host = host;
        this.port = port;
        this.hostMode = hostMode;
        this.onWindowClose = onWindowClose;
    }

    public void start() throws Exception {
        if (!running.compareAndSet(false, true)) {
            return;
        }

        socket = new Socket(host, port);
        socket.setTcpNoDelay(true);

        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        writer = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);

        readerExecutor.submit(this::readLoop);
        send(GameMessage.of(MessageType.HELLO, defaultPlayerName()));

        boolean assigned = assignedLatch.await(8, TimeUnit.SECONDS);
        if (!assigned) {
            throw new IllegalStateException("Timed out waiting for ASSIGN from host.");
        }

        createWindowOnEdt();

        tickScheduler.scheduleAtFixedRate(this::safeTick, AppConfig.NETWORK_TICK_MS, AppConfig.NETWORK_TICK_MS, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        try {
            send(GameMessage.of(MessageType.DISCONNECT, localPlayerId));
        } catch (Exception ignored) {
        }

        closeNetwork();
        tickScheduler.shutdownNow();
        readerExecutor.shutdownNow();

        GameWindow windowRef = window;
        if (windowRef != null) {
            SwingUtilities.invokeLater(windowRef::dispose);
        }
    }

    private void safeTick() {
        try {
            if (!running.get()) {
                return;
            }

            updateLocalMovementAndSend();
            renderAndUpdateHud();
        } catch (Exception ex) {
            System.err.println("[CLIENT] Tick error: " + ex.getMessage());
        }
    }

    private void updateLocalMovementAndSend() {
        if (localPlayerId <= 0 || window == null) {
            return;
        }

        PlayerState local = players.computeIfAbsent(localPlayerId, this::createSpawnState);
        InputState input = window.getInputState();

        boolean changed = false;

        if (input.leftPressed) {
            local.heading = normalizeHeading(local.heading + AppConfig.TURN_SPEED_RAD);
            changed = true;
        }
        if (input.rightPressed) {
            local.heading = normalizeHeading(local.heading - AppConfig.TURN_SPEED_RAD);
            changed = true;
        }

        double forward = 0.0;
        if (input.forwardPressed) {
            forward += 1.0;
        }
        if (input.backwardPressed) {
            forward -= 1.0;
        }

        if (forward != 0.0) {
            local.x = clamp(local.x + (Math.sin(local.heading) * AppConfig.MOVE_SPEED * forward));
            local.z = clamp(local.z + (Math.cos(local.heading) * AppConfig.MOVE_SPEED * forward));
            changed = true;
        }

        tickCounter++;
        if (!changed && (tickCounter % 5) != 0) {
            return;
        }

        local.y = 0.0;
        local.lastUpdateMs = System.currentTimeMillis();

        send(GameMessage.of(
                MessageType.MOVE,
                local.x,
                local.y,
                local.z,
                local.heading,
                moveSequence.incrementAndGet(),
                local.lastUpdateMs
        ));
    }

    private void renderAndUpdateHud() {
        GameWindow windowRef = window;
        if (windowRef == null || localPlayerId <= 0) {
            return;
        }

        Map<Integer, PlayerState> playerSnapshot = new HashMap<>();
        for (Map.Entry<Integer, PlayerState> entry : players.entrySet()) {
            playerSnapshot.put(entry.getKey(), entry.getValue().copy());
        }

        CollectibleState collectibleSnapshot = collectible.copy();
        PlayerScore localScore = scores.get(localPlayerId);
        PlayerScore localScoreSnapshot = localScore == null ? null : localScore.copy();

        SwingUtilities.invokeLater(() -> {
            if (!windowRef.isDisplayable()) {
                return;
            }
            windowRef.render(playerSnapshot, collectibleSnapshot, localPlayerId);
            windowRef.updateHud(
                    connectedPlayers,
                    requiredPlayers,
                    matchRunning,
                    localScoreSnapshot,
                    dbFlushCount,
                    dbReadbackCount
            );
        });
    }

    private void readLoop() {
        try {
            String line;
            while (running.get() && (line = reader.readLine()) != null) {
                GameMessage message = GameMessage.parse(line);
                if (message == null) {
                    continue;
                }
                processMessage(message);
            }
        } catch (IOException ex) {
            if (running.get()) {
                System.err.println("[CLIENT] Read loop error: " + ex.getMessage());
            }
        } finally {
            if (running.get()) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                        null,
                        "Connection to host lost.",
                        "Disconnected",
                        JOptionPane.WARNING_MESSAGE
                ));
                stop();
            }
        }
    }

    private void processMessage(GameMessage message) {
        switch (message.getType()) {
            case ASSIGN:
                localPlayerId = message.getInt(0, -1);
                if (localPlayerId > 0) {
                    players.putIfAbsent(localPlayerId, createSpawnState(localPlayerId));
                    scores.putIfAbsent(localPlayerId, new PlayerScore());
                    assignedLatch.countDown();
                }
                break;
            case LOBBY:
                connectedPlayers = message.getInt(0, connectedPlayers);
                requiredPlayers = message.getInt(1, requiredPlayers);
                matchRunning = message.getInt(2, 0) == 1;
                dbFlushCount = message.getInt(3, dbFlushCount);
                dbReadbackCount = message.getInt(4, dbReadbackCount);
                break;
            case MOVE:
                updatePlayerFromMessage(message, 0);
                if (message.getFields().size() >= 8) {
                    int playerId = message.getInt(0, -1);
                    int match = message.getInt(6, 0);
                    int lifetime = message.getInt(7, 0);
                    updateScore(playerId, match, lifetime);
                }
                break;
            case SNAPSHOT:
                updatePlayerFromMessage(message, 0);
                int snapshotPlayerId = message.getInt(0, -1);
                int snapshotMatch = message.getInt(6, 0);
                int snapshotLifetime = message.getInt(7, 0);
                updateScore(snapshotPlayerId, snapshotMatch, snapshotLifetime);
                break;
            case DBSYNC:
                updatePlayerFromMessage(message, 0);
                break;
            case SCORE:
                updateScore(
                        message.getInt(0, -1),
                        message.getInt(1, 0),
                        message.getInt(2, 0)
                );
                break;
            case COLLECTIBLE:
                collectible.active = message.getInt(0, 0) == 1;
                collectible.x = message.getDouble(1, 0.0);
                collectible.y = message.getDouble(2, 0.40);
                collectible.z = message.getDouble(3, 0.0);
                break;
            case DISCONNECT:
                int disconnectedPlayer = message.getInt(0, -1);
                if (disconnectedPlayer > 0) {
                    players.remove(disconnectedPlayer);
                }
                break;
            case ERROR:
                String error = message.getField(0, "Unknown server error");
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                        null,
                        error,
                        "Server Error",
                        JOptionPane.ERROR_MESSAGE
                ));
                stop();
                break;
            case PING:
                break;
            default:
                break;
        }
    }

    private void updatePlayerFromMessage(GameMessage message, int offset) {
        int playerId = message.getInt(offset, -1);
        if (playerId <= 0) {
            return;
        }

        PlayerState state = players.computeIfAbsent(playerId, PlayerState::new);
        state.x = message.getDouble(offset + 1, state.x);
        state.y = message.getDouble(offset + 2, state.y);
        state.z = message.getDouble(offset + 3, state.z);
        state.heading = message.getDouble(offset + 4, state.heading);
        state.lastUpdateMs = message.getLong(offset + 5, System.currentTimeMillis());
    }

    private void updateScore(int playerId, int matchPoints, int lifetimePoints) {
        if (playerId <= 0) {
            return;
        }
        PlayerScore score = scores.computeIfAbsent(playerId, id -> new PlayerScore());
        score.matchPoints = matchPoints;
        score.lifetimePoints = lifetimePoints;
    }

    private void createWindowOnEdt() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            window = new GameWindow(hostMode, () -> {
                stop();
                if (onWindowClose != null) {
                    onWindowClose.run();
                }
            });
        });
    }

    private void send(GameMessage message) {
        PrintWriter out = writer;
        if (out == null || message == null) {
            return;
        }
        synchronized (out) {
            out.println(message.encode());
        }
    }

    private void closeNetwork() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        }

        writer = null;
        reader = null;
        socket = null;
    }

    private PlayerState createSpawnState(int playerId) {
        PlayerState state = new PlayerState(playerId);
        if (playerId == 2) {
            state.x = -5.0;
            state.z = -2.0;
        } else if (playerId == 3) {
            state.x = 5.0;
            state.z = -2.0;
        } else {
            state.x = 0.0;
            state.z = 0.0;
        }
        state.y = 0.0;
        state.heading = 0.0;
        return state;
    }

    private double clamp(double value) {
        return Math.max(-AppConfig.WORLD_HALF_SIZE, Math.min(AppConfig.WORLD_HALF_SIZE, value));
    }

    private double normalizeHeading(double heading) {
        double twoPi = Math.PI * 2.0;
        double normalized = heading % twoPi;
        if (normalized < 0) {
            normalized += twoPi;
        }
        return normalized;
    }

    private String defaultPlayerName() {
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            return hostMode ? ("Host-" + hostName) : ("Client-" + hostName);
        } catch (Exception ex) {
            return hostMode ? "Host" : "Client";
        }
    }
}
