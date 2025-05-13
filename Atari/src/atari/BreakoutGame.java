package atari;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;


/**
 * CAMBIOS:
 * - Comentarios en el codigo
 * - Poner vidas dentro de la partida (3) y puntuacion
 * - Cuando se rompen todos los bloques (dificil) añadir cada 2 min una fila y si se queda sin espacio salta el mini menu
 * - Sonido cuando explota + menu
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
	 private static Frame gameWindow;
	    private static GameCanvas canvas;

	    // Se llama desde MainMenu
	    public static void launchGame() {
	        gameWindow = new Frame("Atari Breakout Java2D");
	        // Pantalla completa para juego
	        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	        int sw = screenSize.width;
	        int sh = screenSize.height;
	        gameWindow.setSize(sw, sh);
	        gameWindow.setResizable(false);

	        // Crear canvas con dimensiones de pantalla completa
	        canvas = new GameCanvas(sw, sh);
	        gameWindow.add(canvas);
	        // No usar pack(), porque queremos tamaño exacto
	        gameWindow.setVisible(true);
	        canvas.requestFocus();

	        gameWindow.addWindowListener(new java.awt.event.WindowAdapter() {
	            public void windowClosing(java.awt.event.WindowEvent e) {
	                gameWindow.dispose();
	                MainMenu.main(null);
	            }
	        });

	        new Thread(canvas).start();
	    }
	    

	    // Reinicia la partida desde GameCanvas
	    public static void restartGame() {
	        if (canvas != null) {
	            canvas.resetGame();
	            canvas.requestFocus();
	            new Thread(canvas).start();
	        }
	    }

	    // Vuelve al menú principal desde GameCanvas
	    public static void returnToMenu() {
	        if (gameWindow != null) {
	            gameWindow.dispose();
	        }
	        MainMenu.main(null);
	    }

	    // Entrada por defecto
	    public static void main(String[] args) {
	        MainMenu.main(args);
	    }
	}