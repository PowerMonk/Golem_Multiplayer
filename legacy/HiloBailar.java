import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class HiloBailar extends Thread {
    
    creaEscenaGrafica creaEscena;
    public volatile boolean apagarHilo = false; 
    
    public HiloBailar(creaEscenaGrafica escena){
        creaEscena = escena;
    }
    
    // ==============================================================
    // MÉTODO PARA REPRODUCIR EL AUDIO
    // ==============================================================
    private void reproducirToosieSlide() {
        try {
            // Buscamos el archivo en la ruta que tienes estructurada en tu proyecto
            File archivoAudio = new File("src/toosieslidewav.wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(archivoAudio);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start(); // Le da play al audio de 6 segundos
        } catch (Exception e) {
            System.out.println("Ups, hubo un problema con el audio: " + e.getMessage());
        }
    }
    
    @Override
    public void run() {
        // 1. Justo antes de empezar a mover el Golem, le damos Play a la canción
        reproducirToosieSlide();
        
        // 2. Comienza la animación
        while(!apagarHilo) {
            try {
                // animacionToosieSlide() devuelve 'true' cuando el ciclo se completa (60 pasos)
                boolean terminoBaile = creaEscena.animacionToosieSlide();
                
                if (terminoBaile) {
                    apagarHilo = true; // Se auto-detiene para no entrar en bucle
                }
                
                // ==============================================================
                // TIMING PERFECTO: 6000 ms (6 seg) / 60 pasos = 100 ms por paso
                // ==============================================================
                Thread.sleep(100);
            }
            catch(InterruptedException ex) {
                break;
            }
        }
    }
}