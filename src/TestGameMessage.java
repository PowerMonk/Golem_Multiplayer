public class TestGameMessage {
    public static void main(String[] args) {
        String raw = "ASSIGN|2";
        GameMessage msg = GameMessage.parse(raw);
        if (msg == null) {
            System.out.println("parse returned null");
            return;
        }
        System.out.println("Parsed type: " + msg.getType());
        System.out.println("Field0: '" + msg.getField(0, "<none>") + "'");
    }
}
