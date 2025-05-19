package atari;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;

public class FuentePersonalizada {

    public static Font cargarFuente(float tamaño) {
        try {
            InputStream is = FuentePersonalizada.class.getResourceAsStream("/fonts/fuente.ttf");
            Font fuente = Font.createFont(Font.TRUETYPE_FONT, is);
            fuente = fuente.deriveFont(tamaño); // Puedes pasar tamaño como 20f, 32f, etc.

            // Registrar en el sistema gráfico
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(fuente);

            return fuente;

        } catch (Exception e) {
            e.printStackTrace();
            return new Font("SansSerif", Font.PLAIN, (int) tamaño); // Fuente por defecto si falla
        }
    }
}
