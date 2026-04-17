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
import javax.vecmath.Vector3d;
import java.awt.Color; 

public class creaEscenaGrafica {
    public BranchGroup bgRaiz;
    
    // VARIABLES GLOBALES 
    TransformGroup tgMoverFigura; 
    
    TransformGroup tgArticulacionHombroIzq;
    TransformGroup tgArticulacionCodoIzq;
    TransformGroup tgArticulacionHombroDer;
    TransformGroup tgArticulacionCodoDer;
    
    TransformGroup tgArticulacionCaderaIzq;
    TransformGroup tgArticulacionRodillaIzq;
    TransformGroup tgArticulacionCaderaDer;
    TransformGroup tgArticulacionRodillaDer;

    // NUEVOS TG GLOBALES PARA SEPARAR LOS EJES (Evita que el brazo se tuerza)
    TransformGroup tgMoverLeftUpperArm;
    TransformGroup tgMoverRightUpperArm;
    TransformGroup tgMoverLeftLowerArm;
    TransformGroup tgMoverRightLowerArm;
    
    // ENTORNO (MUNDO)
    TransformGroup tgMoverMundo;
    float mundoPosX = 0f;
    float mundoPosZ = 0f;
    int pasosBaile = 0;

    // ARREGLO PARA GUARDAR LA POSTURA ORIGINAL (HARD RESET)
    private Transform3D[] transformacionesOriginales = new Transform3D[13];
    
    public creaEscenaGrafica(){
        bgRaiz = new BranchGroup();
        Color c = new Color(0,0,0); 
        int primFlags = Primitive.GENERATE_NORMALS + Primitive.GENERATE_TEXTURE_COORDS;
        Textura tex = new Textura(); 
        Appearance appDefault = tex.crearTexturas("CaderaB_GolemH.png");
        
        Box bxCabeza = new Box(0.15f, 0.15f, 0.15f, primFlags, appDefault);
        bxCabeza.getShape(Box.FRONT).setAppearance(tex.crearTexturas("Cara_GolemH.png"));
        bxCabeza.getShape(Box.BACK).setAppearance(tex.crearTexturas("Nuca_GolemH.png"));
        bxCabeza.getShape(Box.LEFT).setAppearance(tex.crearTexturas("CabezaLS_GolemH.png"));
        bxCabeza.getShape(Box.RIGHT).setAppearance(tex.crearTexturas("CabezaRS_GolemH.png"));
        bxCabeza.getShape(Box.TOP).setAppearance(tex.crearTexturas("MoyeraXD.png"));

        Box bxTorso = new Box(0.35f, 0.23f, 0.20f, primFlags, appDefault);
        bxTorso.getShape(Box.FRONT).setAppearance(tex.crearTexturas("Pecho_GolemH.png"));
        bxTorso.getShape(Box.BACK).setAppearance(tex.crearTexturas("Espalda_GolemH.png"));

        Box bxCadera = new Box(0.19f, 0.10f, 0.10f, primFlags, appDefault);
        bxCadera.getShape(Box.FRONT).setAppearance(tex.crearTexturas("CaderaF_GolemH.png"));
        bxCadera.getShape(Box.BACK).setAppearance(tex.crearTexturas("CaderaB_GolemH.png"));

        Box bxNariz = new Box(0.05f, 0.1f, 0.05f, primFlags, appDefault);

        Box bxLeftUpperLeg = new Box(0.13f, 0.20f, 0.09f, primFlags, appDefault);
        bxLeftUpperLeg.getShape(Box.FRONT).setAppearance(tex.crearTexturas("LeftLegF_GolemH.png"));
        bxLeftUpperLeg.getShape(Box.BACK).setAppearance(tex.crearTexturas("LeftLegB_GolemH.png"));

        Box bxRightUpperLeg = new Box(0.13f, 0.20f, 0.09f, primFlags, appDefault);
        bxRightUpperLeg.getShape(Box.FRONT).setAppearance(tex.crearTexturas("RightLegF_GolemH.png"));
        bxRightUpperLeg.getShape(Box.BACK).setAppearance(tex.crearTexturas("RightLegB_GolemH.png"));

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

        Box bxLeftUpperArm = new Box(0.12f, 0.32f, 0.12f, primFlags, appDefault);
        bxLeftUpperArm.getShape(Box.FRONT).setAppearance(tex.crearTexturas("LeftArmF_GolemH.png"));
        bxLeftUpperArm.getShape(Box.BACK).setAppearance(tex.crearTexturas("LeftArmB_GolemH.png"));
        bxLeftUpperArm.getShape(Box.LEFT).setAppearance(tex.crearTexturas("LeftArmS_GolemH.png")); 

        Box bxRightUpperArm = new Box(0.12f, 0.32f, 0.12f, primFlags, appDefault);
        bxRightUpperArm.getShape(Box.FRONT).setAppearance(tex.crearTexturas("RightArmF_GolemH.png"));
        bxRightUpperArm.getShape(Box.BACK).setAppearance(tex.crearTexturas("RightArmB_GolemH.png"));
        bxRightUpperArm.getShape(Box.RIGHT).setAppearance(tex.crearTexturas("RightArmS_GolemH.png")); 

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
        
        Sphere sfLeftShoulder = new Sphere(0.06f);
        Sphere sfRightShoulder = new Sphere(0.06f);
        Sphere sfLeftElbow = new Sphere(0.12f);
        Sphere sfRightElbow = new Sphere(0.12f);
        
        Sphere sfLeftHip = new Sphere(0.06f);
        Sphere sfRightHip = new Sphere(0.06f);
        Sphere sfLeftKnee = new Sphere(0.13f);
        Sphere sfRightKnee = new Sphere(0.13f);
        
        Appearance appMundo = tex.crearTexturas("fondo.png");
        
        // --- FIX APLICADO AQUÍ: AUMENTAMOS LA ALTURA DEL MUNDO DE 0.80f a 1.50f ---
        Box bxMundo = new Box(-2.0f, 1.50f, 8.0f, primFlags, appMundo);
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

        tgMoverFigura = new TransformGroup(); 
        tgMoverFigura.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE); 
        tgMoverFigura.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);  
        
        BoundingSphere mouseBounds = new BoundingSphere(new Point3d(), 1000.0);
        MouseRotate myMouseRotate = new MouseRotate(); 
        myMouseRotate.setTransformGroup(tgMoverFigura);
        myMouseRotate.setSchedulingBounds(mouseBounds);
        
        Transform3D t3dMoverCabeza = new Transform3D();
        t3dMoverCabeza.set(new Vector3f(0f,.38f,.05f));
        TransformGroup tgMoverCabeza = new TransformGroup(t3dMoverCabeza);
        
        Transform3D t3dMoverCadera = new Transform3D();
        t3dMoverCadera.set(new Vector3f(0f,-0.33f,0f)); 
        TransformGroup tgMoverCadera = new TransformGroup(t3dMoverCadera);
        
        Transform3D t3dMoverNariz = new Transform3D();
        t3dMoverNariz.set(new Vector3f(0f,0.30f,0.25f));
        TransformGroup tgMoverNariz = new TransformGroup(t3dMoverNariz);
        
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

        // --- FIX APLICADO AQUÍ: SEPARAMOS LOS BRAZOS EN VARIABLES GLOBALES CON PERMISOS ---
        Transform3D t3dMoverLeftUpperArm = new Transform3D();
        t3dMoverLeftUpperArm.set(new Vector3f(0f, -0.32f, 0f)); 
        tgMoverLeftUpperArm = new TransformGroup(t3dMoverLeftUpperArm);
        tgMoverLeftUpperArm.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgMoverLeftUpperArm.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        
        Transform3D t3dMoverRightUpperArm = new Transform3D();
        t3dMoverRightUpperArm.set(new Vector3f(0f, -0.32f, 0f));
        tgMoverRightUpperArm = new TransformGroup(t3dMoverRightUpperArm);
        tgMoverRightUpperArm.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgMoverRightUpperArm.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        
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

        // --- FIX APLICADO AQUÍ: SEPARAMOS LOS ANTEBRAZOS EN VARIABLES GLOBALES CON PERMISOS ---
        Transform3D t3dMoverLeftLowerArm = new Transform3D();
        t3dMoverLeftLowerArm.set(new Vector3f(0f, -0.32f, 0f));
        tgMoverLeftLowerArm = new TransformGroup(t3dMoverLeftLowerArm);
        tgMoverLeftLowerArm.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgMoverLeftLowerArm.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        
        Transform3D t3dMoverRightLowerArm = new Transform3D();
        t3dMoverRightLowerArm.set(new Vector3f(0f, -0.32f, 0f));
        tgMoverRightLowerArm = new TransformGroup(t3dMoverRightLowerArm);
        tgMoverRightLowerArm.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgMoverRightLowerArm.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);

        // ENSAMBLAJE
        bgRaiz.addChild(tgMoverMundo); 
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
        
        // ==========================================
        // GURADAMOS LA POSTURA JUSTO AL TERMINAR
        // ==========================================
        guardarPosturaOriginal();
    }
    
    // MÉTODO AUXILIAR PARA OBTENER LOS TG SEGÚN SU NÚMERO
    private TransformGroup getTGByNumero(int tgNumero) {
        switch (tgNumero) {
            case 0: return tgArticulacionHombroIzq;
            case 1: return tgArticulacionCodoIzq;
            case 2: return tgArticulacionHombroDer; 
            case 3: return tgArticulacionCodoDer;
            case 4: return tgArticulacionCaderaIzq;
            case 5: return tgArticulacionRodillaIzq;
            case 6: return tgArticulacionCaderaDer;
            case 7: return tgArticulacionRodillaDer;
            case 8: return tgMoverFigura; 
            case 9: return tgMoverLeftUpperArm;
            case 10: return tgMoverRightUpperArm;
            case 11: return tgMoverLeftLowerArm;
            case 12: return tgMoverRightLowerArm;
            default: return null;
        }
    }

    // TOMA UNA "FOTO" DE LAS MATRICES INICIALES
    public void guardarPosturaOriginal() {
        for (int i = 0; i < transformacionesOriginales.length; i++) {
            transformacionesOriginales[i] = new Transform3D();
            TransformGroup tgAux = getTGByNumero(i);
            if (tgAux != null) {
                tgAux.getTransform(transformacionesOriginales[i]);
            }
        }
    }

    // RESTAURA TODAS LAS EXTREMIDADES A SU ESTADO INICIAL
    public void resetearPostura() {
        for (int i = 0; i < transformacionesOriginales.length; i++) {
            TransformGroup tgAux = getTGByNumero(i);
            if (tgAux != null && transformacionesOriginales[i] != null) {
                tgAux.setTransform(transformacionesOriginales[i]);
            }
        }
    }
    
    public void girarTG(int grados, String eje, int tgNumero) {
        TransformGroup tgAuxiliar = getTGByNumero(tgNumero);
        if (tgAuxiliar == null) return;
        
        Transform3D t3dActual = new Transform3D();
        tgAuxiliar.getTransform(t3dActual); 
        Transform3D t3dGiro = new Transform3D();
        double radianes = Math.toRadians(grados); 
        
        switch(eje.toLowerCase()) {
            case "x": t3dGiro.rotX(radianes); break;
            case "y": t3dGiro.rotY(radianes); break;
            case "z": t3dGiro.rotZ(radianes); break;
            default: return;
        }
        
        t3dActual.mul(t3dGiro);
        tgAuxiliar.setTransform(t3dActual);
    }

    public void deslizarMundo(boolean haciaIzquierda) {
        float step = 0.04f; 
        
        Transform3D t3d = new Transform3D();
        tgMoverFigura.getTransform(t3d);
        Vector3f forward = new Vector3f(0, 0, -1);
        t3d.transform(forward);
        
        double currentAngleY = Math.atan2(-forward.x, -forward.z);
        
        if (haciaIzquierda) {
            mundoPosX += step * (float)Math.cos(currentAngleY);
            mundoPosZ -= step * (float)Math.sin(currentAngleY);
        } else {
            mundoPosX -= step * (float)Math.cos(currentAngleY);
            mundoPosZ += step * (float)Math.sin(currentAngleY);
        }
        
        Transform3D t3dMundo = new Transform3D();
        tgMoverMundo.getTransform(t3dMundo);
        t3dMundo.setTranslation(new Vector3f(mundoPosX, 0f, mundoPosZ));
        tgMoverMundo.setTransform(t3dMundo);
    }

   // =========================================================
    // ANIMACIÓN: TOOSIE SLIDE (FINAL FIX + RESET MAESTRO + PIERNAS AFUERA EN SLIDE)
    // =========================================================
    public boolean animacionToosieSlide() {
        // --- MITAD 1 ---
        if (pasosBaile >= 0 && pasosBaile < 5) {
            // "Right foot": Pierna der. adelante, Brazo y antebrazo izq. adelante (X)
            girarTG(-6, "x", 6); 
            girarTG(-6, "x", 0); // Hombro izq.
            girarTG(-6, "x", 1); // Antebrazo izq.
        } 
        else if (pasosBaile >= 5 && pasosBaile < 10) {
            // "Up": Pantorrilla der. hacia un lado (Z: 120°) y un poco atrás (X: 20°)
            girarTG(24, "z", 7); // 24 * 5 = 120 grados
            girarTG(4, "x", 7);  // 4 * 5 = 20 grados
            girarTG(6, "x", 6);  // Regresa la pierna der. de adelante
            
            // Brazo y antebrazo izq. regresan a su lugar de 'adelante'
            girarTG(6, "x", 0);  
            girarTG(6, "x", 1);  

            // AMBOS brazos se abren
            girarTG(-6, "z", 9);  
            girarTG(6, "z", 10);  
            girarTG(-12, "y", 11); 
            girarTG(12, "y", 12);  
        } 
        else if (pasosBaile >= 10 && pasosBaile < 15) {
            // Regresa la pantorrilla der. a su posición original (0°)
            girarTG(-24, "z", 7); 
            girarTG(-4, "x", 7); 
            
            // AMBOS brazos y antebrazos regresan
            girarTG(6, "z", 9);  
            girarTG(-6, "z", 10); 
            girarTG(12, "y", 11);  
            girarTG(-12, "y", 12); 
        } 
        else if (pasosBaile >= 15 && pasosBaile < 20) {
            // "Left foot...": Pierna izq. adelante
            girarTG(-6, "x", 4); 
            girarTG(-6, "x", 0); // Hombro izq.
            girarTG(-6, "x", 1); // Antebrazo izq.
        } 
        else if (pasosBaile >= 20 && pasosBaile < 25) {
            // "...Slide": Regresa pierna izq, SE DESPLAZA Y MUEVE PIERNA HACIA AFUERA (Z)
            girarTG(6, "x", 4);  
            girarTG(-6, "z", 4); // <-- AÑADIDO: Pierna izq hacia afuera
            deslizarMundo(true); 
            
            // Brazo y antebrazo izq. regresan
            girarTG(6, "x", 0);  
            girarTG(6, "x", 1);  

            // AMBOS brazos abren y antebrazos flexionan
            girarTG(-6, "z", 9);  
            girarTG(6, "z", 10);   
            girarTG(-12, "y", 11); 
            girarTG(12, "y", 12);  
        } 
        else if (pasosBaile >= 25 && pasosBaile < 30) {
            // Termina el slide
            girarTG(6, "z", 4); // <-- AÑADIDO: Regresa la pierna izq de afuera

            // AMBOS brazos y antebrazos regresan a posición neutral
            girarTG(6, "z", 9);  
            girarTG(-6, "z", 10); 
            girarTG(12, "y", 11);  
            girarTG(-12, "y", 12); 
        }
        
        // --- MITAD 2 ---
        else if (pasosBaile >= 30 && pasosBaile < 35) {
            // "Left foot": Pierna izq. adelante, Brazo y antebrazo der. adelante
            girarTG(-6, "x", 4); 
            girarTG(-6, "x", 2); // Hombro der.
            girarTG(-6, "x", 3); // Antebrazo der.
        }
        else if (pasosBaile >= 35 && pasosBaile < 40) {
            // "Up": Pantorrilla izq. hacia un lado (Z: -120°) y un poco atrás (X: 20°)
            girarTG(-24, "z", 5); // -24 * 5 = -120 grados
            girarTG(4, "x", 5);   // 4 * 5 = 20 grados
            girarTG(6, "x", 4);   // Regresa la pierna izq. de adelante
            
            // Brazo y antebrazo der. regresan
            girarTG(6, "x", 2);   
            girarTG(6, "x", 3);   

            // AMBOS brazos abren
            girarTG(-6, "z", 9);  
            girarTG(6, "z", 10);   
            girarTG(-12, "y", 11); 
            girarTG(12, "y", 12);  
        }
        else if (pasosBaile >= 40 && pasosBaile < 45) {
            // Regresa la pantorrilla izq. a su posición original (0°)
            girarTG(24, "z", 5); 
            girarTG(-4, "x", 5); 
            
            // AMBOS brazos y antebrazos regresan
            girarTG(6, "z", 9);  
            girarTG(-6, "z", 10); 
            girarTG(12, "y", 11);  
            girarTG(-12, "y", 12); 
        }
        else if (pasosBaile >= 45 && pasosBaile < 50) {
            // "Right foot...": Pierna der. adelante
            girarTG(-6, "x", 6); 
            girarTG(-6, "x", 2); // Hombro der.
            girarTG(-6, "x", 3); // Antebrazo der.
        }
        else if (pasosBaile >= 50 && pasosBaile < 55) {
            // "...Slide": Regresa pierna der, SE DESPLAZA Y MUEVE PIERNA HACIA AFUERA (Z)
            girarTG(6, "x", 6);   
            girarTG(6, "z", 6);  // <-- AÑADIDO: Pierna der hacia afuera
            deslizarMundo(false); 
            
            // Brazo y antebrazo der. regresan
            girarTG(6, "x", 2);   
            girarTG(6, "x", 3);   

            // AMBOS brazos abren
            girarTG(-6, "z", 9);  
            girarTG(6, "z", 10);   
            girarTG(-12, "y", 11); 
            girarTG(12, "y", 12);  
        }
        else if (pasosBaile >= 55 && pasosBaile < 60) {
            // Termina el slide
            girarTG(-6, "z", 6); // <-- AÑADIDO: Regresa la pierna der de afuera

            // AMBOS brazos y antebrazos regresan a neutral
            girarTG(6, "z", 9);  
            girarTG(-6, "z", 10); 
            girarTG(12, "y", 11);  
            girarTG(-12, "y", 12); 
        }

        pasosBaile++;

        if (pasosBaile >= 60) {
            pasosBaile = 0;
            // ==========================================
            // FIX: HARD RESET A LA POSTURA ORIGINAL
            // ==========================================
            resetearPostura(); 
            return true; 
        }
        
        return false; 
    }
}