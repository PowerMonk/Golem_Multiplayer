import com.sun.j3d.utils.universe.SimpleUniverse;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GraphicsConfiguration;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;
import javax.media.j3d.Canvas3D;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GameWindow extends JFrame {
    private final InputState inputState = new InputState();
    private final JLabel lobbyLabel = new JLabel("Lobby: 0/3");
    private final JLabel scoreLabel = new JLabel("Score: 0 (lifetime 0)");
    private final JLabel dbLabel = new JLabel("DB flush/readback: 0/0");

    private final GameScene3D scene;

    public GameWindow(boolean hostMode, Runnable onClose) {
        setTitle(hostMode ? "Golem Multiplayer - Host" : "Golem Multiplayer - Client");
        setSize(1100, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        Canvas3D canvas = new Canvas3D(config);
        canvas.setFocusable(true);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        topPanel.add(lobbyLabel);
        topPanel.add(scoreLabel);
        topPanel.add(dbLabel);

        add(topPanel, BorderLayout.NORTH);
        add(canvas, BorderLayout.CENTER);

        scene = new GameScene3D(canvas);

        KeyAdapter keyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                toggleKey(e.getKeyCode(), true);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                toggleKey(e.getKeyCode(), false);
            }
        };

        canvas.addKeyListener(keyAdapter);
        addKeyListener(keyAdapter);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (onClose != null) {
                    onClose.run();
                }
            }
        });

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);

        canvas.requestFocusInWindow();
    }

    public InputState getInputState() {
        return inputState;
    }

    public void render(Map<Integer, PlayerState> players, CollectibleState collectible, int localPlayerId) {
        scene.render(players, collectible, localPlayerId);
    }

    public void updateHud(int connected, int required, boolean running, PlayerScore localScore, int dbFlushCount, int dbReadbackCount) {
        String lobbyText = running
                ? ("Lobby: " + connected + "/" + required + " (match active)")
                : ("Lobby: " + connected + "/" + required + " (waiting for 3 players)");
        lobbyLabel.setText(lobbyText);

        int matchPoints = localScore == null ? 0 : localScore.matchPoints;
        int lifetimePoints = localScore == null ? 0 : localScore.lifetimePoints;
        scoreLabel.setText("Score: " + matchPoints + " (lifetime " + lifetimePoints + ")");

        dbLabel.setText("DB flush/readback: " + dbFlushCount + "/" + dbReadbackCount);
    }

    private void toggleKey(int keyCode, boolean pressed) {
        switch (keyCode) {
            case KeyEvent.VK_W:
                inputState.forwardPressed = pressed;
                break;
            case KeyEvent.VK_S:
                inputState.backwardPressed = pressed;
                break;
            case KeyEvent.VK_A:
                inputState.leftPressed = pressed;
                break;
            case KeyEvent.VK_D:
                inputState.rightPressed = pressed;
                break;
            default:
                break;
        }
    }
}
