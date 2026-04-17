import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::showRoleDialog);
    }

    private static void showRoleDialog() {
        Object[] options = {"Host", "Join", "Exit"};
        int selection = JOptionPane.showOptionDialog(
                null,
                "Do you want to host or join a match?",
                "Golem Multiplayer",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (selection == 0) {
            startAsHost();
        } else if (selection == 1) {
            startAsClient();
        } else {
            System.exit(0);
        }
    }

    private static void startAsHost() {
        Thread hostThread = new Thread(() -> {
            HostServer host = new HostServer();
            GameClient localClient = null;
            try {
                host.start();
                localClient = new GameClient(
                        "127.0.0.1",
                        AppConfig.GAME_PORT,
                        true,
                        () -> {
                            host.stop();
                            System.exit(0);
                        }
                );
                localClient.start();
            } catch (Exception ex) {
                final String message = "Host startup failed: " + ex.getMessage();
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                        null,
                        message,
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE
                ));

                if (localClient != null) {
                    localClient.stop();
                }
                host.stop();
            }

            final GameClient clientRef = localClient;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (clientRef != null) {
                    clientRef.stop();
                }
                host.stop();
            }));
        }, "host-main-thread");

        hostThread.setDaemon(false);
        hostThread.start();
    }

    private static void startAsClient() {
        String hostIp = JOptionPane.showInputDialog(
                null,
                "Enter host LAN IP:",
                "127.0.0.1"
        );

        if (hostIp == null || hostIp.isBlank()) {
            System.exit(0);
            return;
        }

        Thread clientThread = new Thread(() -> {
            GameClient client = new GameClient(
                    hostIp.trim(),
                    AppConfig.GAME_PORT,
                    false,
                    () -> System.exit(0)
            );

            try {
                client.start();
            } catch (Exception ex) {
                final String message = "Client startup failed: " + ex.getMessage();
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                        null,
                        message,
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE
                ));
                client.stop();
                return;
            }

            Runtime.getRuntime().addShutdownHook(new Thread(client::stop));
        }, "client-main-thread");

        clientThread.setDaemon(false);
        clientThread.start();
    }
}
