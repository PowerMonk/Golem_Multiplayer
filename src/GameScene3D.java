import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.SimpleUniverse;
import java.util.HashMap;
import java.util.Map;
import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

public class GameScene3D {
    private final SimpleUniverse universe;
    private final TransformGroup worldTransform;
    private final TransformGroup collectibleTransform;
    private final Map<Integer, TransformGroup> playerTransforms = new HashMap<>();

    public GameScene3D(Canvas3D canvas) {
        universe = new SimpleUniverse(canvas);
        universe.getViewingPlatform().setNominalViewingTransform();

        BranchGroup sceneRoot = new BranchGroup();
        worldTransform = new TransformGroup();
        worldTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        sceneRoot.addChild(worldTransform);
        addLights(sceneRoot);
        addFloorAndBoundaries(worldTransform);
        addPlayers(worldTransform);

        collectibleTransform = new TransformGroup();
        collectibleTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        collectibleTransform.addChild(new Sphere(0.45f, colored(255, 220, 20)));
        worldTransform.addChild(collectibleTransform);

        sceneRoot.compile();
        universe.addBranchGraph(sceneRoot);
        configureCamera();
    }

    public void render(Map<Integer, PlayerState> players, CollectibleState collectible, int localPlayerId) {
        PlayerState localState = players.get(localPlayerId);
        if (localState == null) {
            localState = new PlayerState(localPlayerId);
        }

        Transform3D worldTx = new Transform3D();
        worldTx.rotY(-localState.heading);
        Transform3D worldShift = new Transform3D();
        worldShift.setTranslation(new Vector3f((float) -localState.x, 0f, (float) -localState.z));
        worldTx.mul(worldShift);
        worldTransform.setTransform(worldTx);

        for (int playerId = 1; playerId <= AppConfig.MAX_PLAYERS; playerId++) {
            TransformGroup tg = playerTransforms.get(playerId);
            if (tg == null) {
                continue;
            }

            PlayerState state = players.get(playerId);
            Transform3D playerTx = new Transform3D();
            if (state == null) {
                playerTx.setTranslation(new Vector3f(0f, -200f, 0f));
            } else {
                playerTx.rotY(state.heading);
                playerTx.setTranslation(new Vector3f((float) state.x, 0.9f, (float) state.z));
            }
            tg.setTransform(playerTx);
        }

        Transform3D collectibleTx = new Transform3D();
        if (collectible != null && collectible.active) {
            collectibleTx.setTranslation(new Vector3f((float) collectible.x, (float) collectible.y, (float) collectible.z));
        } else {
            collectibleTx.setTranslation(new Vector3f(0f, -200f, 0f));
        }
        collectibleTransform.setTransform(collectibleTx);
    }

    private void configureCamera() {
        Transform3D cameraTx = new Transform3D();
        cameraTx.lookAt(new Point3d(0.0, 3.6, 10.0), new Point3d(0.0, 1.0, 0.0), new Vector3d(0.0, 1.0, 0.0));
        cameraTx.invert();
        universe.getViewingPlatform().getViewPlatformTransform().setTransform(cameraTx);
    }

    private void addLights(BranchGroup root) {
        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 300.0);

        AmbientLight ambient = new AmbientLight(new Color3f(0.55f, 0.55f, 0.55f));
        ambient.setInfluencingBounds(bounds);
        root.addChild(ambient);

        DirectionalLight directional = new DirectionalLight(new Color3f(0.95f, 0.95f, 0.95f), new Vector3f(-0.6f, -1.0f, -0.4f));
        directional.setInfluencingBounds(bounds);
        root.addChild(directional);
    }

    private void addFloorAndBoundaries(TransformGroup parent) {
        Appearance floorAppearance = colored(185, 210, 175);
        TransformGroup floorTG = new TransformGroup();
        Transform3D floorTx = new Transform3D();
        floorTx.setTranslation(new Vector3f(0f, -0.08f, 0f));
        floorTG.setTransform(floorTx);
        floorTG.addChild(new Box((float) AppConfig.WORLD_HALF_SIZE, 0.08f, (float) AppConfig.WORLD_HALF_SIZE, floorAppearance));
        parent.addChild(floorTG);

        Appearance wallAppearance = colored(130, 150, 125);
        float h = (float) AppConfig.WORLD_HALF_SIZE;
        parent.addChild(makeWall(0f, 1.1f, h, h, 1.1f, 0.15f, wallAppearance));
        parent.addChild(makeWall(0f, 1.1f, -h, h, 1.1f, 0.15f, wallAppearance));
        parent.addChild(makeWall(h, 1.1f, 0f, 0.15f, 1.1f, h, wallAppearance));
        parent.addChild(makeWall(-h, 1.1f, 0f, 0.15f, 1.1f, h, wallAppearance));
    }

    private TransformGroup makeWall(float x, float y, float z, float sx, float sy, float sz, Appearance appearance) {
        TransformGroup wallTG = new TransformGroup();
        Transform3D tx = new Transform3D();
        tx.setTranslation(new Vector3f(x, y, z));
        wallTG.setTransform(tx);
        wallTG.addChild(new Box(sx, sy, sz, appearance));
        return wallTG;
    }

    private void addPlayers(TransformGroup parent) {
        int[][] colors = {
            {215, 70, 70},
            {70, 170, 95},
            {75, 100, 210}
        };

        for (int id = 1; id <= AppConfig.MAX_PLAYERS; id++) {
            int[] rgb = colors[id - 1];
            TransformGroup playerRoot = new TransformGroup();
            playerRoot.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            playerRoot.addChild(createSimpleGolem(rgb[0], rgb[1], rgb[2]));
            parent.addChild(playerRoot);
            playerTransforms.put(id, playerRoot);
        }
    }

    private Node createSimpleGolem(int r, int g, int b) {
        Appearance body = colored(r, g, b);
        Appearance dark = colored(
                Math.max(20, r - 35),
                Math.max(20, g - 35),
                Math.max(20, b - 35)
        );

        BranchGroup root = new BranchGroup();

        root.addChild(new Box(0.45f, 0.6f, 0.25f, body));
        root.addChild(offsetNode(0f, 0.95f, 0f, new Box(0.28f, 0.28f, 0.28f, dark)));
        root.addChild(offsetNode(-0.68f, 0.25f, 0f, new Box(0.16f, 0.5f, 0.16f, body)));
        root.addChild(offsetNode(0.68f, 0.25f, 0f, new Box(0.16f, 0.5f, 0.16f, body)));
        root.addChild(offsetNode(-0.22f, -0.8f, 0f, new Box(0.18f, 0.45f, 0.18f, dark)));
        root.addChild(offsetNode(0.22f, -0.8f, 0f, new Box(0.18f, 0.45f, 0.18f, dark)));

        return root;
    }

    private TransformGroup offsetNode(float x, float y, float z, Node node) {
        TransformGroup tg = new TransformGroup();
        Transform3D tx = new Transform3D();
        tx.setTranslation(new Vector3f(x, y, z));
        tg.setTransform(tx);
        tg.addChild(node);
        return tg;
    }

    private Appearance colored(int r, int g, int b) {
        Color helper = new Color();
        return helper.setColor(r, g, b);
    }
}
