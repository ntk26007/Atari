package atari;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;



/**
 * CAMBIOS:
 * - Comentarios en el codigo
 * - Clase para cada nivel salvo el 1
 * - Cuando se rompen todos los bloques (dificil) añadir cada 2 min una fila y si se queda sin espacio salta el mini menu
 * - Sonido cuando explota + menu
 * - Acabar la interfaz
 * - Establecer 3 modos (dificil) =
 * 		Facil: 3 vidas, romper al toque
 * 		Medio: 3 vidas, romper a los 2 toques, añadir 2 filas mas de bloques
 * 		Dificil: 2 vidas, romper a los 2 o 3 toques y con un limite de tiempo, añadir muchas filas mas de bloques
 * 
 *  Opcional: potenciadores (aumentar nº de bolas, que vaya mas rapido, cuando clicas un bloque rompe la misma fila del bloque)
 */

public class BreakoutGame {
	 private static Frame gameWindow;
	    private static GameCanvas canvas;
	    public static NivelMedio nivelMedio; // se declara para el metodo restart concreto de nivelMedio (el resto es lo mismo pero con los otros niveles)
	    private static Canvas currentCanvas;

	    // Se llama desde MainMenu
	    public static void launchGame() {
	        gameWindow = new Frame("Atari Breakout");
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
	    
	    
	    public static void launchMediumLevel() {
	        if (gameWindow != null) gameWindow.dispose(); // Si hay una ventana anterior, se cierra

	        gameWindow = new Frame("Atari Breakout – Nivel Medio");
	        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	        gameWindow.setSize(screen.width, screen.height);
	        gameWindow.setLayout(new BorderLayout());

	        NivelMedio mediumCanvas = new NivelMedio(screen.width, screen.height);
	        currentCanvas = mediumCanvas;
	        gameWindow.add(mediumCanvas, BorderLayout.CENTER);

	        gameWindow.addWindowListener(new WindowAdapter() {
	            @Override
	            public void windowClosing(WindowEvent e) {
	                gameWindow.dispose();
	                MainMenu.main(null);
	            }
	        });

	        gameWindow.setVisible(true);
	        mediumCanvas.requestFocus();
	        new Thread(mediumCanvas).start();
	    }

	    // Reinicia la partida desde GameCanvas, el nivel facil
	    public static void restartNivelFacil() {
	        if (canvas != null) {
	            canvas.resetGame();
	            canvas.requestFocus();
	            new Thread(canvas).start();
	        }
	    }
	    
	    
	    //reinicia el nivel medio
	    public static void restartNivelMedio() {
	        if (nivelMedio != null) {
	            nivelMedio.resetGame(); // método de la clase NivelMedio
	            nivelMedio.requestFocus();
	            new Thread(nivelMedio).start();
	        }
	    }
	    

	    // Vuelve al menú principal 
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