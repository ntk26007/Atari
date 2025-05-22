package atari;

import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

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
    private static final float VOLUMEN_PREDETERMINADO = -14.0f; // volumen bajo (en decibelios)

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
            Clip clip = AudioSystem.getClip();
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(ruta));
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
