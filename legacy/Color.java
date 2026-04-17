
import javax.media.j3d.Appearance;
import javax.media.j3d.ColoringAttributes;
import javax.vecmath.Color3f;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author aroml
 */
public class Color {
    Appearance appColor;
    
    public Appearance setColor(int a, int b, int c){
        appColor = new Appearance();
        Color3f color = new Color3f(a/255f,b/255f,c/255f);
        ColoringAttributes atributoscolor = new ColoringAttributes(color,ColoringAttributes.SHADE_FLAT);
        appColor.setColoringAttributes(atributoscolor);
        return appColor;
    }
}
