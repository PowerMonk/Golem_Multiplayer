public class PlayerScore {
    public volatile int matchPoints;
    public volatile int lifetimePoints;

    public PlayerScore copy() {
        PlayerScore copied = new PlayerScore();
        copied.matchPoints = matchPoints;
        copied.lifetimePoints = lifetimePoints;
        return copied;
    }
}
