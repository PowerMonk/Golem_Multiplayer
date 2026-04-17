public class CollectibleState {
    public volatile boolean active;
    public volatile double x;
    public volatile double y;
    public volatile double z;
    public volatile long lastSpawnMs;
    public volatile long lastCollectedMs;

    public CollectibleState copy() {
        CollectibleState copied = new CollectibleState();
        copied.active = active;
        copied.x = x;
        copied.y = y;
        copied.z = z;
        copied.lastSpawnMs = lastSpawnMs;
        copied.lastCollectedMs = lastCollectedMs;
        return copied;
    }
}
