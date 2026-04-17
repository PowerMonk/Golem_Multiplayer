import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;
import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

// Descomenta si estas clases están en otros paquetes, 
// o déjalas así si están en el mismo paquete.
// import java.awt.Color; 
// import Textura; 

public class EscenaGraficaLegacy {
    public BranchGroup bgRaiz;
    int pasos = 0; 
    
    // =========================================================
    // VARIABLES GLOBALES (Articulaciones rotatorias)
    // =========================================================
    TransformGroup tgMoverFigura; 
    
    TransformGroup tgArticulacionHombroIzq;
    TransformGroup tgArticulacionCodoIzq;
    TransformGroup tgArticulacionHombroDer;
    TransformGroup tgArticulacionCodoDer;
    
    TransformGroup tgArticulacionCaderaIzq;
    TransformGroup tgArticulacionRodillaIzq;
    TransformGroup tgArticulacionCaderaDer;
    TransformGroup tgArticulacionRodillaDer;
    
    // --- NUEVAS VARIABLES PARA EL ENTORNO (MUNDO) ---
    TransformGroup tgMoverMundo;
    double anguloGlobalY = 0.0; // Rastreará hacia dónde está mirando el Golem
    float mundoPosX = 0f;
    float mundoPosZ = 0f;
    // Añade esta nueva variable global junto a "int pasos = 0;" al inicio de la clase
    int pasosBaile = 0;
    // ----------------------------------------
    
    public EscenaGraficaLegacy(){
        
        bgRaiz = new BranchGroup();
        Color c = new Color(); 
        
        // 1. CREACIÓN DE GEOMETRÍAS Y TEXTURAS
        // =========================================================
        
        // Banderas para permitir mapeo de texturas
        int primFlags = Primitive.GENERATE_NORMALS + Primitive.GENERATE_TEXTURE_COORDS;
        Textura tex = new Textura(); 
        
        // Textura por defecto para caras faltantes (SOLO EL NOMBRE DEL ARCHIVO)
        Appearance appDefault = tex.crearTexturas("CaderaB_GolemH.png");
        
        // --- CABEZA ---
        Box bxCabeza = new Box(0.15f, 0.15f, 0.15f, primFlags, appDefault);
        bxCabeza.getShape(Box.FRONT).setAppearance(tex.crearTexturas("Cara_GolemH.png"));
        bxCabeza.getShape(Box.BACK).setAppearance(tex.crearTexturas("Nuca_GolemH.png"));
        bxCabeza.getShape(Box.LEFT).setAppearance(tex.crearTexturas("CabezaLS_GolemH.png"));
        bxCabeza.getShape(Box.RIGHT).setAppearance(tex.crearTexturas("CabezaRS_GolemH.png"));
        bxCabeza.getShape(Box.TOP).setAppearance(tex.crearTexturas("MoyeraXD.png"));

        // --- TORSO ---
        Box bxTorso = new Box(0.35f, 0.23f, 0.20f, primFlags, appDefault);
        bxTorso.getShape(Box.FRONT).setAppearance(tex.crearTexturas("Pecho_GolemH.png"));
        bxTorso.getShape(Box.BACK).setAppearance(tex.crearTexturas("Espalda_GolemH.png"));

        // --- CADERA ---
        Box bxCadera = new Box(0.19f, 0.10f, 0.10f, primFlags, appDefault);
        bxCadera.getShape(Box.FRONT).setAppearance(tex.crearTexturas("CaderaF_GolemH.png"));
        bxCadera.getShape(Box.BACK).setAppearance(tex.crearTexturas("CaderaB_GolemH.png"));

        // --- NARIZ --- 
        Box bxNariz = new Box(0.05f, 0.1f, 0.05f, primFlags, appDefault);

        // --- PIERNAS (Muslos) ---
        Box bxLeftUpperLeg = new Box(0.13f, 0.20f, 0.09f, primFlags, appDefault);
        bxLeftUpperLeg.getShape(Box.FRONT).setAppearance(tex.crearTexturas("LeftLegF_GolemH.png"));
        bxLeftUpperLeg.getShape(Box.BACK).setAppearance(tex.crearTexturas("LeftLegB_GolemH.png"));

        Box bxRightUpperLeg = new Box(0.13f, 0.20f, 0.09f, primFlags, appDefault);
        bxRightUpperLeg.getShape(Box.FRONT).setAppearance(tex.crearTexturas("RightLegF_GolemH.png"));
        bxRightUpperLeg.getShape(Box.BACK).setAppearance(tex.crearTexturas("RightLegB_GolemH.png"));

        // --- PIERNAS (Pies / Parte baja) ---
        Appearance appPie = tex.crearTexturas("Pie_GolemH.png");
        Box bxLeftLowerLeg = new Box(0.13f, 0.20f, 0.09f, primFlags, appDefault);
        bxLeftLowerLeg.getShape(Box.FRONT).setAppearance(appPie);
        bxLeftLowerLeg.getShape(Box.BACK).setAppearance(appPie);
        bxLeftLowerLeg.getShape(Box.LEFT).setAppearance(appPie);
        bxLeftLowerLeg.getShape(Box.RIGHT).setAppearance(appPie);

        Box bxRightLowerLeg = new Box(0.13f, 0.20f, 0.09f, primFlags, appDefault);
        bxRightLowerLeg.getShape(Box.FRONT).setAppearance(appPie);
        bxRightLowerLeg.getShape(Box.BACK).setAppearance(appPie);
        bxRightLowerLeg.getShape(Box.LEFT).setAppearance(appPie);
        bxRightLowerLeg.getShape(Box.RIGHT).setAppearance(appPie);

        // --- BRAZOS (Bíceps) ---
        Box bxLeftUpperArm = new Box(0.12f, 0.32f, 0.12f, primFlags, appDefault);
        bxLeftUpperArm.getShape(Box.FRONT).setAppearance(tex.crearTexturas("LeftArmF_GolemH.png"));
        bxLeftUpperArm.getShape(Box.BACK).setAppearance(tex.crearTexturas("LeftArmB_GolemH.png"));
        bxLeftUpperArm.getShape(Box.LEFT).setAppearance(tex.crearTexturas("LeftArmS_GolemH.png")); 

        Box bxRightUpperArm = new Box(0.12f, 0.32f, 0.12f, primFlags, appDefault);
        bxRightUpperArm.getShape(Box.FRONT).setAppearance(tex.crearTexturas("RightArmF_GolemH.png"));
        bxRightUpperArm.getShape(Box.BACK).setAppearance(tex.crearTexturas("RightArmB_GolemH.png"));
        bxRightUpperArm.getShape(Box.RIGHT).setAppearance(tex.crearTexturas("RightArmS_GolemH.png")); 

        // --- BRAZOS (Manos / Parte baja) ---
        Appearance appMano = tex.crearTexturas("Mano_GolemH.png");
        Box bxLeftLowerArm = new Box(0.12f, 0.32f, 0.12f, primFlags, appDefault);
        bxLeftLowerArm.getShape(Box.FRONT).setAppearance(appMano);
        bxLeftLowerArm.getShape(Box.BACK).setAppearance(appMano);
        bxLeftLowerArm.getShape(Box.LEFT).setAppearance(appMano);
        bxLeftLowerArm.getShape(Box.RIGHT).setAppearance(appMano);

        Box bxRightLowerArm = new Box(0.12f, 0.32f, 0.12f, primFlags, appDefault);
        bxRightLowerArm.getShape(Box.FRONT).setAppearance(appMano);
        bxRightLowerArm.getShape(Box.BACK).setAppearance(appMano);
        bxRightLowerArm.getShape(Box.LEFT).setAppearance(appMano);
        bxRightLowerArm.getShape(Box.RIGHT).setAppearance(appMano);
        
        // --- ARTICULACIONES (Se mantienen con color sólido) ---
        Sphere sfLeftShoulder = new Sphere(0.06f, c.setColor(123,52,201));
        Sphere sfRightShoulder = new Sphere(0.06f, c.setColor(123,52,201));
        Sphere sfLeftElbow = new Sphere(0.12f, c.setColor(100,152,102));
        Sphere sfRightElbow = new Sphere(0.12f, c.setColor(100,152,102));
        
        Sphere sfLeftHip = new Sphere(0.06f, c.setColor(50,100,90));
        Sphere sfRightHip = new Sphere(0.06f, c.setColor(50,100,90));
        Sphere sfLeftKnee = new Sphere(0.13f, c.setColor(150,120,190));
        Sphere sfRightKnee = new Sphere(0.13f, c.setColor(150,120,190));
        
        // --- MUNDO (CAJA INVERTIDA) ---
        // Al usar un valor negativo en X (-2.0f), las normales se invierten y la textura se ve por dentro
        Appearance appMundo = tex.crearTexturas("fondo.png");
        Box bxMundo = new Box(-2.0f, 0.80f, 8.0f, primFlags, appMundo);
        bxMundo.getShape(Box.FRONT).setAppearance(appMundo);
        bxMundo.getShape(Box.BACK).setAppearance(appMundo);
        bxMundo.getShape(Box.LEFT).setAppearance(appMundo);
        bxMundo.getShape(Box.RIGHT).setAppearance(appMundo);
        bxMundo.getShape(Box.TOP).setAppearance(appMundo);
        bxMundo.getShape(Box.BOTTOM).setAppearance(appMundo);

        tgMoverMundo = new TransformGroup();
        tgMoverMundo.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgMoverMundo.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        tgMoverMundo.addChild(bxMundo);

        // --- 2. CONFIGURACIÓN DEL MOUSE ---
        tgMoverFigura = new TransformGroup(); 
        tgMoverFigura.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE); 
        tgMoverFigura.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);  
        
        BoundingSphere mouseBounds = new BoundingSphere(new Point3d(), 1000.0);
        MouseRotate myMouseRotate = new MouseRotate(); 
        myMouseRotate.setTransformGroup(tgMoverFigura);
        myMouseRotate.setSchedulingBounds(mouseBounds);
        
        // --- 3. TRANSFORM 3D Y TRANSFORM GROUPS ---
        Transform3D t3dMoverCabeza = new Transform3D();
        t3dMoverCabeza.set(new Vector3f(0f,.38f,.05f));
        TransformGroup tgMoverCabeza = new TransformGroup(t3dMoverCabeza);
        
        Transform3D t3dMoverCadera = new Transform3D();
        t3dMoverCadera.set(new Vector3f(0f,-0.33f,0f)); 
        TransformGroup tgMoverCadera = new TransformGroup(t3dMoverCadera);
        
        Transform3D t3dMoverNariz = new Transform3D();
        t3dMoverNariz.set(new Vector3f(0f,0.30f,0.25f));
        TransformGroup tgMoverNariz = new TransformGroup(t3dMoverNariz);
        
        // JERARQUÍA PIERNAS
        Transform3D t3dArticulacionCaderaIzq = new Transform3D();
        t3dArticulacionCaderaIzq.set(new Vector3f(-0.13f, -0.43f, 0f));
        tgArticulacionCaderaIzq = new TransformGroup(t3dArticulacionCaderaIzq);
        tgArticulacionCaderaIzq.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgArticulacionCaderaIzq.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);

        Transform3D t3dArticulacionCaderaDer = new Transform3D();
        t3dArticulacionCaderaDer.set(new Vector3f(0.13f, -0.43f, 0f));
        tgArticulacionCaderaDer = new TransformGroup(t3dArticulacionCaderaDer);
        tgArticulacionCaderaDer.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgArticulacionCaderaDer.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);

        Transform3D t3dMoverLeftUpperLeg = new Transform3D();
        t3dMoverLeftUpperLeg.set(new Vector3f(0f, -0.20f, 0f));
        TransformGroup tgMoverLeftUpperLeg = new TransformGroup(t3dMoverLeftUpperLeg);
        
        Transform3D t3dMoverRightUpperLeg = new Transform3D();
        t3dMoverRightUpperLeg.set(new Vector3f(0f, -0.20f, 0f));
        TransformGroup tgMoverRightUpperLeg = new TransformGroup(t3dMoverRightUpperLeg);

        Transform3D t3dArticulacionRodillaIzq = new Transform3D();
        t3dArticulacionRodillaIzq.set(new Vector3f(0f, -0.20f, 0f));
        tgArticulacionRodillaIzq = new TransformGroup(t3dArticulacionRodillaIzq);
        tgArticulacionRodillaIzq.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgArticulacionRodillaIzq.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);

        Transform3D t3dArticulacionRodillaDer = new Transform3D();
        t3dArticulacionRodillaDer.set(new Vector3f(0f, -0.20f, 0f));
        tgArticulacionRodillaDer = new TransformGroup(t3dArticulacionRodillaDer);
        tgArticulacionRodillaDer.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgArticulacionRodillaDer.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);

        Transform3D t3dMoverLeftLowerLeg = new Transform3D();
        t3dMoverLeftLowerLeg.set(new Vector3f(0f, -0.20f, 0f));
        TransformGroup tgMoverLeftLowerLeg = new TransformGroup(t3dMoverLeftLowerLeg);
        
        Transform3D t3dMoverRightLowerLeg = new Transform3D();
        t3dMoverRightLowerLeg.set(new Vector3f(0f, -0.20f, 0f));
        TransformGroup tgMoverRightLowerLeg = new TransformGroup(t3dMoverRightLowerLeg);

        // JERARQUÍA BRAZOS
        Transform3D t3dArticulacionHombroIzq = new Transform3D();
        t3dArticulacionHombroIzq.set(new Vector3f(-0.41f, 0.15f, 0f)); 
        tgArticulacionHombroIzq = new TransformGroup(t3dArticulacionHombroIzq);
        tgArticulacionHombroIzq.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgArticulacionHombroIzq.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        
        Transform3D t3dArticulacionHombroDer = new Transform3D();
        t3dArticulacionHombroDer.set(new Vector3f(0.41f, 0.15f, 0f)); 
        tgArticulacionHombroDer = new TransformGroup(t3dArticulacionHombroDer);
        tgArticulacionHombroDer.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgArticulacionHombroDer.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);

        Transform3D t3dMoverLeftUpperArm = new Transform3D();
        t3dMoverLeftUpperArm.set(new Vector3f(0f, -0.32f, 0f)); 
        TransformGroup tgMoverLeftUpperArm = new TransformGroup(t3dMoverLeftUpperArm);
        
        Transform3D t3dMoverRightUpperArm = new Transform3D();
        t3dMoverRightUpperArm.set(new Vector3f(0f, -0.32f, 0f));
        TransformGroup tgMoverRightUpperArm = new TransformGroup(t3dMoverRightUpperArm);
        
        Transform3D t3dArticulacionCodoIzq = new Transform3D();
        t3dArticulacionCodoIzq.set(new Vector3f(0f, -0.32f, 0f));
        tgArticulacionCodoIzq = new TransformGroup(t3dArticulacionCodoIzq);
        tgArticulacionCodoIzq.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgArticulacionCodoIzq.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        
        Transform3D t3dArticulacionCodoDer = new Transform3D();
        t3dArticulacionCodoDer.set(new Vector3f(0f, -0.32f, 0f));
        tgArticulacionCodoDer = new TransformGroup(t3dArticulacionCodoDer);
        tgArticulacionCodoDer.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgArticulacionCodoDer.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);

        Transform3D t3dMoverLeftLowerArm = new Transform3D();
        t3dMoverLeftLowerArm.set(new Vector3f(0f, -0.32f, 0f));
        TransformGroup tgMoverLeftLowerArm = new TransformGroup(t3dMoverLeftLowerArm);
        
        Transform3D t3dMoverRightLowerArm = new Transform3D();
        t3dMoverRightLowerArm.set(new Vector3f(0f, -0.32f, 0f));
        TransformGroup tgMoverRightLowerArm = new TransformGroup(t3dMoverRightLowerArm);

        // ENSAMBLAJE
        bgRaiz.addChild(tgMoverMundo); // Añadimos el mundo a la escena
        bgRaiz.addChild(tgMoverFigura);
        bgRaiz.addChild(myMouseRotate);
        
        tgMoverFigura.addChild(bxTorso);
        tgMoverFigura.addChild(tgMoverCabeza);
        tgMoverFigura.addChild(tgMoverCadera);
        tgMoverFigura.addChild(tgMoverNariz);
        
        tgMoverCabeza.addChild(bxCabeza);
        tgMoverCadera.addChild(bxCadera);
        tgMoverNariz.addChild(bxNariz);

        tgMoverFigura.addChild(tgArticulacionHombroIzq);
        tgMoverFigura.addChild(tgArticulacionHombroDer);
        tgMoverFigura.addChild(tgArticulacionCaderaIzq);
        tgMoverFigura.addChild(tgArticulacionCaderaDer);
        
        tgArticulacionHombroIzq.addChild(sfLeftShoulder);            
        tgArticulacionHombroIzq.addChild(tgMoverLeftUpperArm);       
        tgMoverLeftUpperArm.addChild(bxLeftUpperArm);                
        tgMoverLeftUpperArm.addChild(tgArticulacionCodoIzq);        
        tgArticulacionCodoIzq.addChild(sfLeftElbow);                
        tgArticulacionCodoIzq.addChild(tgMoverLeftLowerArm);        
        tgMoverLeftLowerArm.addChild(bxLeftLowerArm);                
        
        tgArticulacionHombroDer.addChild(sfRightShoulder);          
        tgArticulacionHombroDer.addChild(tgMoverRightUpperArm);     
        tgMoverRightUpperArm.addChild(bxRightUpperArm);             
        tgMoverRightUpperArm.addChild(tgArticulacionCodoDer);       
        tgArticulacionCodoDer.addChild(sfRightElbow);                
        tgArticulacionCodoDer.addChild(tgMoverRightLowerArm);        
        tgMoverRightLowerArm.addChild(bxRightLowerArm);    
        
        tgArticulacionCaderaIzq.addChild(sfLeftHip);
        tgArticulacionCaderaIzq.addChild(tgMoverLeftUpperLeg);
        tgMoverLeftUpperLeg.addChild(bxLeftUpperLeg);
        tgMoverLeftUpperLeg.addChild(tgArticulacionRodillaIzq);
        tgArticulacionRodillaIzq.addChild(sfLeftKnee);
        tgArticulacionRodillaIzq.addChild(tgMoverLeftLowerLeg);
        tgMoverLeftLowerLeg.addChild(bxLeftLowerLeg);

        tgArticulacionCaderaDer.addChild(sfRightHip);
        tgArticulacionCaderaDer.addChild(tgMoverRightUpperLeg);
        tgMoverRightUpperLeg.addChild(bxRightUpperLeg);
        tgMoverRightUpperLeg.addChild(tgArticulacionRodillaDer);
        tgArticulacionRodillaDer.addChild(sfRightKnee);
        tgArticulacionRodillaDer.addChild(tgMoverRightLowerLeg);
        tgMoverRightLowerLeg.addChild(bxRightLowerLeg);
    }
    
    // MÉTODO PARA MOVER MANUALMENTE
    public void girarTG(int grados, String eje, int tgNumero) {
        TransformGroup tgAuxiliar = null;
        switch (tgNumero) {
            case 0: tgAuxiliar = tgArticulacionHombroIzq; break;
            case 1: tgAuxiliar = tgArticulacionCodoIzq; break;
            case 2: tgAuxiliar = tgArticulacionHombroDer; break; 
            case 3: tgAuxiliar = tgArticulacionCodoDer; break;
            case 4: tgAuxiliar = tgArticulacionCaderaIzq; break;
            case 5: tgAuxiliar = tgArticulacionRodillaIzq; break;
            case 6: tgAuxiliar = tgArticulacionCaderaDer; break;
            case 7: tgAuxiliar = tgArticulacionRodillaDer; break;
            case 8: tgAuxiliar = tgMoverFigura; break; // Control general de la figura (Giro)
            default: return;
        }
        
        Transform3D t3dActual = new Transform3D();
        tgAuxiliar.getTransform(t3dActual); 
        Transform3D t3dGiro = new Transform3D();
        double radianes = Math.toRadians(grados); 
        
        switch(eje.toLowerCase()) {
            case "x": t3dGiro.rotX(radianes); break;
            case "y": 
                t3dGiro.rotY(radianes); 
                // Registramos el giro global del Golem para saber hacia dónde avanza
                if(tgNumero == 8) {
                    anguloGlobalY += radianes; 
                }
                break;
            case "z": t3dGiro.rotZ(radianes); break;
            default: return;
        }
        
        t3dActual.mul(t3dGiro);
        tgAuxiliar.setTransform(t3dActual);
    }
    
    // =========================================================
    // MÉTODO DE ANIMACIÓN (CICLO DE CAMINADO EXPLÍCITO)
    // =========================================================
    public void girar5deg() {
        if (pasos >= 0 && pasos < 6) {
            girarTG(5, "x", 2);  
            girarTG(-5, "x", 3);  
            girarTG(-5, "x", 0); 
            girarTG(-5, "x", 1); 
            
            girarTG(5, "x", 4); 
            girarTG(5, "x", 5);  
            girarTG(-5, "x", 6);  
            girarTG(5, "x", 7);  
        } 
        else if (pasos >= 6 && pasos < 12) {
            girarTG(-5, "x", 2); 
            girarTG(5, "x", 3); 
            girarTG(5, "x", 0); 
            girarTG(5, "x", 1); 
            
            girarTG(-5, "x", 4);  
            girarTG(-5, "x", 5); 
            girarTG(5, "x", 6); 
            girarTG(-5, "x", 7); 
        } 
        else if (pasos >= 12 && pasos < 18) {
            girarTG(5, "x", 0);  
            girarTG(-5, "x", 1);  
            girarTG(-5, "x", 2); 
            girarTG(-5, "x", 3); 
            
            girarTG(5, "x", 6); 
            girarTG(5, "x", 7);  
            girarTG(-5, "x", 4);  
            girarTG(5, "x", 5);  
        } 
        else if (pasos >= 18 && pasos < 24) {
            girarTG(-5, "x", 0); 
            girarTG(5, "x", 1); 
            girarTG(5, "x", 2); 
            girarTG(5, "x", 3); 
            
            girarTG(-5, "x", 6);  
            girarTG(-5, "x", 7); 
            girarTG(5, "x", 4); 
            girarTG(-5, "x", 5); 
        }

        pasos++;
        if (pasos == 24) {
            pasos = 0;
        }
    }
    
    // =========================================================
    // MÉTODO PARA TRASLADAR AL GOLEM (Conservado por si lo ocupas)
    // =========================================================
    public void TrasladarG(float x, float y, float z){
        Transform3D t3dActual = new Transform3D();
        tgMoverFigura.getTransform(t3dActual);
        
        Transform3D t3dMover = new Transform3D();
        t3dMover.setTranslation(new Vector3f(x, y, z));
        
        t3dActual.mul(t3dMover);
        tgMoverFigura.setTransform(t3dActual);
    }
    
    // =========================================================
    // MÉTODO PARA QUE EL GOLEM CAMINE (Mueve el entorno en lugar del Golem)
    // =========================================================
    public void caminar(int direccion) {
        girar5deg(); // Hace la animación en el mismo lugar
        
        // direccion debe ser 1 (Adelante) o -1 (Atrás)
        float step = 0.05f * direccion; 
        
        // Trigonometría para mover el mundo en la dirección OPUESTA a la que mira el Golem
        mundoPosX -= step * (float)Math.sin(anguloGlobalY);
        
        mundoPosZ -= step * (float)Math.cos(anguloGlobalY);
        System.out.println("posicion X:" + mundoPosX + " Posición z:" +  mundoPosZ);
        
        
        Transform3D t3dMundo = new Transform3D();
        tgMoverMundo.getTransform(t3dMundo);
        t3dMundo.setTranslation(new Vector3f(mundoPosX, 0f, mundoPosZ));
        tgMoverMundo.setTransform(t3dMundo);
    }
    // =========================================================
    // MÉTODO DE ANIMACIÓN: TOOSIE SLIDE (1ra MITAD)
    // =========================================================
   public boolean animacionToosieSlide() {
        // Devuelve true cuando ha terminado el paso para avisarle al hilo que se apague
        
        // =========================================================
        // PRIMERA MITAD: Right foot up, left foot slide
        // =========================================================
        if (pasosBaile >= 0 && pasosBaile < 5) {
            // 1. "Right foot": Patada derecha hacia adelante rotando en X (Cadera der: 6)
            girarTG(-6, "x", 6); 
        } 
        else if (pasosBaile >= 5 && pasosBaile < 10) {
            // 2. "Up": Mueve la pantorrilla en Z (120°) y regresa la pierna en X hacia atrás (30°)
            girarTG(24, "z", 7); // Pantorrilla hacia arriba/afuera (Rodilla der: 7)
            girarTG(6, "x", 6);  // Regresa la pierna completa hacia atrás (Cadera der: 6)
        } 
        else if (pasosBaile >= 10 && pasosBaile < 15) {
            // 3. Regresamos la pantorrilla a posición neutral
            girarTG(-24, "z", 7); 
        } 
        else if (pasosBaile >= 15 && pasosBaile < 20) {
            // 4. "Left foot...": Repite la patada de la PIERNA DERECHA hacia adelante
            girarTG(-6, "x", 6); 
        } 
        else if (pasosBaile >= 20 && pasosBaile < 25) {
            // 5. "...Slide": Regresa la derecha a neutral, y desliza la izquierda HACIA AFUERA
            girarTG(6, "x", 6);   // Guarda la pierna derecha
            girarTG(-6, "z", 4);  // Abre/desliza la pierna izquierda hacia AFUERA (Cadera izq: 4)
        } 
        else if (pasosBaile >= 25 && pasosBaile < 30) {
            // 6. Regresamos la pierna izquierda a posición neutral
            girarTG(6, "z", 4); 
        }

        // =========================================================
        // SEGUNDA MITAD: Left foot up, right foot slide
        // =========================================================
        else if (pasosBaile >= 30 && pasosBaile < 35) {
            // 7. "Left foot": Patada izquierda hacia adelante rotando en X (Cadera izq: 4)
            girarTG(-6, "x", 4); 
        }
        else if (pasosBaile >= 35 && pasosBaile < 40) {
            // 8. "Up": Pantorrilla izq hacia afuera en Z y regresa la pierna izq en X hacia atrás
            // Como la derecha salió con +24, la izquierda sale con -24
            girarTG(-24, "z", 5); // Pantorrilla hacia arriba/afuera (Rodilla izq: 5)
            girarTG(6, "x", 4);   // Regresa la pierna completa hacia atrás (Cadera izq: 4)
        }
        else if (pasosBaile >= 40 && pasosBaile < 45) {
            // 9. Regresamos la pantorrilla izquierda a posición neutral
            girarTG(24, "z", 5); 
        }
        else if (pasosBaile >= 45 && pasosBaile < 50) {
            // 10. "Right foot...": Repite la patada de la PIERNA IZQUIERDA hacia adelante
            girarTG(-6, "x", 4); 
        }
        else if (pasosBaile >= 50 && pasosBaile < 55) {
            // 11. "...Slide": Regresa la izquierda a neutral, y desliza la derecha HACIA AFUERA
            girarTG(6, "x", 4);   // Guarda la pierna izquierda
            // Como la izquierda deslizó con -6, la derecha desliza con +6
            girarTG(6, "z", 6);   // Abre/desliza la pierna derecha hacia AFUERA (Cadera der: 6)
        }
        else if (pasosBaile >= 55 && pasosBaile < 60) {
            // 12. Regresamos la pierna derecha a posición neutral, terminando el baile
            girarTG(-6, "z", 6); 
        }

        pasosBaile++;

        // Cuando llega al final de toda la secuencia (60 ticks), reinicia
        if (pasosBaile >= 60) {
            pasosBaile = 0;
            return true; // Apaga el hilo
        }
        
        return false; // Sigue bailando
    }
}
