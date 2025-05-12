package atari;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


/**
 * CAMBIOS:
 * - Poner vidas dentro de la partida (3)
 * - Hacer la interfaz
 * - Establecer 3 modos (facil, medio, dificil) =
 * 		Facil: 3 vidas, romper al toque
 * 		Medio: 3 vidas, romper a los 2 toques, añadir 2 filas mas de bloques
 * 		Dificil: 2 vidas, romper a los 2 o 3 toques y con un limite de tiempo, añadir muchas filas mas de bloques
 * 
 * - Cuando se rompen todos los bloques: debe mostrar las vidas usadas, decir que ha ganado, y un mini menu que diga == repetir nivel, siguiente nivel, menu principal o salir
 * 
 *  Opcional: potenciadores (aumentar nº de bolas, que vaya mas rapido, cuando clicas un bloque rompe la misma fila del bloque)
 */

public class BreakoutGame {
    public static void main(String[] args) {
        Frame window = new Frame("Atari Breakout Java2D");
        window.setSize(800, 600);
        window.setResizable(false);

        GameCanvas canvas = new GameCanvas(800, 600);
        window.add(canvas);
        window.pack();
        window.setVisible(true);

        // cerrar con la X de la ventana
        window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        // iniciar loop de juego
        new Thread(canvas).start();
    }
}