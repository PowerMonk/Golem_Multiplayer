public final class AppConfig {
    public static final int MAX_PLAYERS = 3;

    public static final int GAME_PORT = 5000;
    public static final int H2_TCP_PORT = 9092;

    public static final String H2_DB_NAME = "golem_multiplayer";
    public static final String H2_JDBC_URL = "jdbc:h2:tcp://localhost/~/" + H2_DB_NAME;
    public static final String H2_USERNAME = "sa";
    public static final String H2_PASSWORD = "";

    public static final int NETWORK_TICK_MS = 50;
    public static final int DB_FLUSH_MS = 1000;
    public static final int DB_READBACK_MS = 2000;
    public static final int COLLECTIBLE_CHECK_MS = 100;
    public static final int COLLECTIBLE_RESPAWN_MS = 1200;

    public static final double WORLD_HALF_SIZE = 15.0;
    public static final double MOVE_SPEED = 0.22;
    public static final double TURN_SPEED_RAD = 0.08;

    public static final double PLAYER_COLLISION_RADIUS = 0.65;
    public static final double COLLECTIBLE_RADIUS = 0.55;

    private AppConfig() {
    }
}
