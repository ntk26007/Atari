package atari;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * 0.0f = volumen estandar.
 * 
 * -10.0f = un poco más bajo.
 * 
 * -20.0f = bastante más bajo.
 * 
 * -50.0f = muy bajo (casi muteado).
 * 
 * -80.0f = completamente en silencio.
 */

public class AudioPlayer {

	private static Clip clip;
    private static FloatControl volumeControl;
    private static final float VOLUMEN_PREDETERMINADO = -12.0f; // volumen bajo (en decibelios)
    private static HashMap<String, Clip> efectos = new HashMap<>();

    public static void reproducirAudio(String ruta) {
        detenerAudio(); // si ya había un audio sonando, lo detenemos

        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(ruta));
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);

            // Configuramos el volumen apenas cargamos el audio
            volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            setVolume(VOLUMEN_PREDETERMINADO);

            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        } catch (Exception ex) {
            System.out.println("Error al reproducir el sonido.");
            ex.printStackTrace();
        }
    }

    public static void detenerAudio() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
    }
    
    public static void reproducirAudioUnaVez(String ruta) {
        detenerAudio();
        try {

            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(ruta));
            Clip efecto = AudioSystem.getClip();
            efecto.open(ais);

            // Reducir el volumen 
            FloatControl volume = (FloatControl) efecto.getControl(FloatControl.Type.MASTER_GAIN);
            volume.setValue(-7.0f); 
            
            efecto.start();
            
            clip.open(ais);
            clip.start(); // No loop
            clip.addLineListener(new LineListener() {
                public void update(LineEvent event) {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                }
            });
            Clip clipGlobal = clip;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //deberia ser pal delay pero no va asiq se queda asi (no hace na)
    public static void cargarEfecto(String nombre, String ruta) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(ruta));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            efectos.put(nombre, clip);
            
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    
    //metodo que nos sirve para reproducir sonidos muy cortos, para el efecto de los bloques
    public static void reproducirEfecto(String ruta) {
        try {
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(new File(ruta));
            Clip efecto = AudioSystem.getClip();
            efecto.open(audioInput);

            // Reducir el volumen (valor en decibelios)
            FloatControl volume = (FloatControl) efecto.getControl(FloatControl.Type.MASTER_GAIN);
            volume.setValue(-6.0f); // -10.0f para más bajo

            efecto.start(); // Solo se reproduce una vez

            // Cierra el clip cuando termine para liberar recursos
            efecto.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    efecto.close();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    
    public static boolean isPlaying() {
        return clip != null && clip.isRunning();
    }


    private static void setVolume(float volumen) {
        if (volumeControl != null) {
            float min = volumeControl.getMinimum();
            float max = volumeControl.getMaximum();

            if (volumen < min) volumen = min;
            if (volumen > max) volumen = max;

            volumeControl.setValue(volumen);
        }
    }
}
