import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {
    private Connection connection;
    private Object h2TcpServer;

    public synchronized void startForHost() throws Exception {
        startTcpServer();
        openConnection();
        createSchemaIfNeeded();
    }

    public synchronized void stop() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
            connection = null;
        }

        if (h2TcpServer != null) {
            try {
                Method stopMethod = h2TcpServer.getClass().getMethod("stop");
                stopMethod.invoke(h2TcpServer);
            } catch (Exception ignored) {
            }
            h2TcpServer = null;
        }
    }

    public synchronized void upsertPlayer(int playerId, String playerName) throws SQLException {
        String sql = "MERGE INTO players (id, player_name) KEY(id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, playerId);
            ps.setString(2, playerName == null ? ("Player-" + playerId) : playerName);
            ps.executeUpdate();
        }
    }

    public synchronized void upsertPosition(PlayerState state) throws SQLException {
        String sql = "MERGE INTO player_positions_latest (player_id, x, y, z, heading, updated_at) "
                + "KEY(player_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, state.playerId);
            ps.setDouble(2, state.x);
            ps.setDouble(3, state.y);
            ps.setDouble(4, state.z);
            ps.setDouble(5, state.heading);
            ps.setLong(6, state.lastUpdateMs);
            ps.executeUpdate();
        }
    }

    public synchronized void upsertMatchScore(int playerId, int points) throws SQLException {
        String sql = "MERGE INTO score_match (player_id, points) KEY(player_id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, playerId);
            ps.setInt(2, points);
            ps.executeUpdate();
        }
    }

    public synchronized void upsertLifetimeScore(int playerId, int points) throws SQLException {
        String sql = "MERGE INTO score_lifetime (player_id, points) KEY(player_id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, playerId);
            ps.setInt(2, points);
            ps.executeUpdate();
        }
    }

    public synchronized Map<Integer, PlayerState> loadLatestPositions() throws SQLException {
        Map<Integer, PlayerState> result = new HashMap<>();
        String sql = "SELECT player_id, x, y, z, heading, updated_at FROM player_positions_latest";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int playerId = rs.getInt("player_id");
                PlayerState state = new PlayerState(playerId);
                state.x = rs.getDouble("x");
                state.y = rs.getDouble("y");
                state.z = rs.getDouble("z");
                state.heading = rs.getDouble("heading");
                state.lastUpdateMs = rs.getLong("updated_at");
                result.put(playerId, state);
            }
        }
        return result;
    }

    public synchronized Map<Integer, Integer> loadMatchScores() throws SQLException {
        Map<Integer, Integer> result = new HashMap<>();
        String sql = "SELECT player_id, points FROM score_match";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.put(rs.getInt("player_id"), rs.getInt("points"));
            }
        }
        return result;
    }

    public synchronized Map<Integer, Integer> loadLifetimeScores() throws SQLException {
        Map<Integer, Integer> result = new HashMap<>();
        String sql = "SELECT player_id, points FROM score_lifetime";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.put(rs.getInt("player_id"), rs.getInt("points"));
            }
        }
        return result;
    }

    public synchronized void resetMatchScores() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("UPDATE score_match SET points = 0");
        }
    }

    public synchronized void insertCollectibleEvent(String eventType, CollectibleState collectible, Integer playerId) throws SQLException {
        String sql = "INSERT INTO collectible_events (event_type, x, y, z, player_id, event_time) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, eventType);
            ps.setDouble(2, collectible.x);
            ps.setDouble(3, collectible.y);
            ps.setDouble(4, collectible.z);
            if (playerId == null) {
                ps.setNull(5, java.sql.Types.INTEGER);
            } else {
                ps.setInt(5, playerId);
            }
            ps.setLong(6, System.currentTimeMillis());
            ps.executeUpdate();
        }
    }

    private void openConnection() throws SQLException {
        connection = DriverManager.getConnection(
                AppConfig.H2_JDBC_URL,
                AppConfig.H2_USERNAME,
                AppConfig.H2_PASSWORD
        );
    }

    private void startTcpServer() throws Exception {
        try {
            Class<?> serverClass = Class.forName("org.h2.tools.Server");
            Method createTcpServer = serverClass.getMethod("createTcpServer", String[].class);
            String[] args = new String[]{
                "-tcp",
                "-tcpAllowOthers",
                "-ifNotExists",
                "-tcpPort",
                String.valueOf(AppConfig.H2_TCP_PORT)
            };
            h2TcpServer = createTcpServer.invoke(null, (Object) args);
            Method startMethod = h2TcpServer.getClass().getMethod("start");
            startMethod.invoke(h2TcpServer);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(
                    "H2 library not found. Add h2-*.jar into lib/ and relaunch.",
                    ex
            );
        }
    }

    private void createSchemaIfNeeded() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS players ("
                    + "id INT PRIMARY KEY,"
                    + "player_name VARCHAR(64) NOT NULL,"
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ")");

            st.execute("CREATE TABLE IF NOT EXISTS player_positions_latest ("
                    + "player_id INT PRIMARY KEY,"
                    + "x DOUBLE NOT NULL,"
                    + "y DOUBLE NOT NULL,"
                    + "z DOUBLE NOT NULL,"
                    + "heading DOUBLE NOT NULL,"
                    + "updated_at BIGINT NOT NULL,"
                    + "FOREIGN KEY (player_id) REFERENCES players(id)"
                    + ")");

            st.execute("CREATE TABLE IF NOT EXISTS score_match ("
                    + "player_id INT PRIMARY KEY,"
                    + "points INT NOT NULL DEFAULT 0,"
                    + "FOREIGN KEY (player_id) REFERENCES players(id)"
                    + ")");

            st.execute("CREATE TABLE IF NOT EXISTS score_lifetime ("
                    + "player_id INT PRIMARY KEY,"
                    + "points INT NOT NULL DEFAULT 0,"
                    + "FOREIGN KEY (player_id) REFERENCES players(id)"
                    + ")");

            st.execute("CREATE TABLE IF NOT EXISTS collectible_events ("
                    + "id IDENTITY PRIMARY KEY,"
                    + "event_type VARCHAR(16) NOT NULL,"
                    + "x DOUBLE NOT NULL,"
                    + "y DOUBLE NOT NULL,"
                    + "z DOUBLE NOT NULL,"
                    + "player_id INT,"
                    + "event_time BIGINT NOT NULL"
                    + ")");
        }
    }
}
