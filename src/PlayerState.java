public class PlayerState {
    public final int playerId;
    public volatile double x;
    public volatile double y;
    public volatile double z;
    public volatile double heading;
    public volatile long lastUpdateMs;

    public PlayerState(int playerId) {
        this.playerId = playerId;
        this.lastUpdateMs = System.currentTimeMillis();
    }

    public PlayerState copy() {
        PlayerState copied = new PlayerState(playerId);
        copied.x = x;
        copied.y = y;
        copied.z = z;
        copied.heading = heading;
        copied.lastUpdateMs = lastUpdateMs;
        return copied;
    }
}
