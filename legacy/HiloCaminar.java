/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author aroml
 */
public class HiloCaminar extends Thread {
    
    creaEscenaGrafica creaEscena;
    public volatile boolean apagarHilo = false; // Empezamos en falso para que corra
    
    // --- NUEVA VARIABLE ---
    // Nos dirá hacia dónde caminar: 1 es adelante, -1 es atrás.
    public int direccion = 1; 
    
    public HiloCaminar(creaEscenaGrafica escena){
        // traemos la escena gráfica original
        creaEscena = escena;
    }
    
    @Override
    public void run() {
        // Mientras NO esté apagado el hilo
        while(!apagarHilo) {
            try {
                // Ahora le pasamos la variable "direccion" que pide el método
                // creaEscena.caminar(direccion);
                Thread.sleep(50);
            }
            catch(InterruptedException ex) {
                // Si el hilo es interrumpido, sale del loop
                break;
            }
        }
    }
}