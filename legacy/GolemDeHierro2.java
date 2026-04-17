import com.sun.j3d.utils.universe.SimpleUniverse;
import java.awt.GraphicsConfiguration;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.swing.JButton;

public class GolemDeHierro2 extends javax.swing.JFrame {
    
    creaEscenaGrafica creaEscena;
    HiloBailar hcBailar; 
    
    public GolemDeHierro2() {
        initComponents();
        
        this.setLayout(null);
        
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration(); 
        Canvas3D lienzo = new Canvas3D(config);
        lienzo.setBounds(20, 20, 500, 500); 
        this.add(lienzo);
        this.setBounds(300, 50, 700, 600);
        
        // ==============================================================
        // INTERFAZ LIMPIA: SOLO EL BOTÓN BAILAR
        // ==============================================================
        JButton btnBailar = new JButton("¡Bailar Toosie Slide!");
        btnBailar.setBounds(530, 50, 150, 40);
        btnBailar.setFocusable(false);
        this.add(btnBailar);

        // ==============================================================
        // UNIVERSO 3D
        // ==============================================================
        BranchGroup Scene = new BranchGroup(); 
        creaEscena = new creaEscenaGrafica();
        Scene = creaEscena.bgRaiz;
        SimpleUniverse n = new SimpleUniverse(lienzo);
        n.getViewingPlatform().setNominalViewingTransform();
        n.addBranchGraph(Scene);
        
        // ==============================================================
        // EVENTO DEL BOTÓN BAILAR
        // ==============================================================
        btnBailar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // Solo activamos el baile si no está bailando ya
                if (hcBailar == null || !hcBailar.isAlive()) {
                    hcBailar = new HiloBailar(creaEscena);
                    hcBailar.start();
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Golem Dancer");
        pack();
    }                        

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new GolemDeHierro2().setVisible(true));
    }
}