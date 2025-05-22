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
 * - Cuando se rompen todos los bloques (dificil) añadir cada 2 min una fila y si se queda sin espacio salta el mini menu
 * - Sonido cuando explota + menu
 * - Pausar el juego pulsando P (mostrar texto que diga pausado)
 * - Acabar la interfaz
 * - Establecer 3 modos (dificil) =
 * 		Facil: 3 vidas, romper al toque
 * 		Medio: 3 vidas, romper a los 2 toques, añadir 2 filas mas de bloques
 * 		Dificil: 2 vidas, romper a los 2 o 3 toques y con un limite de tiempo, añadir muchas filas mas de bloques
 * 
 *  Opcional: potenciadores (aumentar nº de bolas, que vaya mas rapido, cuando clicas un bloque rompe la misma fila del bloque)
 *  
 *  
 *  ARREGLAR=
 *  	
 *  	NIVEL FACL
 *  - Al completar el nivel y pasar al siguiente, no aparece la paleta de juego (revisar varias veces)
 *  
 *  
 *  	NIVEL MEDIO
 *  - Metodo en esta clase para reiniciar el nivel medio, el nivel dificil funciona (no hay metodo pero se reinicia y funciona)
 *  - Colores aleatorios
 *  
 *  	NIVEL DIFICIL
 *  - Para que quede mejor, deberian aparecer mas colores aleatorios como en nivel facil pero he implementado esto:
 *  	Cuando el bloque que dura 3 golpes esta full vida, es en cyan
 *  	" "  " " " " " " "" "  "  2 golpes, es en amarillo
 *  	 " "   "" " " " " " " " " 1 golpe, esta en rosa
 *  
 *    Cambian de color segun su durabilidad pero solo aparecen bloques o amarillos o cyan (el codigo es solo de las clases internas, estan al final de la clase)
 *  
 */

public class BreakoutGame {
	 private static Frame gameWindow;
	    private static GameCanvas canvas;
	    public static NivelMedio nivelMedio; // se declara para el metodo restart concreto de nivelMedio (el resto es lo mismo pero con los otros niveles)
	    public static NivelDificil nivelDificil;
	    private static Canvas currentCanvas;

	    // ejecuta nivel facil
	    public static void launchGame() {
	        gameWindow = new Frame("Atari Breakout");
	  
	        // Pantalla completa para juego
	        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	        int sw = screenSize.width;
	        int sh = screenSize.height;
	        gameWindow.setSize(sw, sh);
	        gameWindow.setResizable(false);
	        gameWindow.setUndecorated(true);
	        gameWindow.setLocation((screenSize.width - sw) / 2, (screenSize.height - sh) / 2);

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
	    
	    //ejecuta nivel medio
	    public static void launchMediumLevel() {	    	
	        if (gameWindow != null) gameWindow.dispose(); // Si hay una ventana anterior, se cierra

	        gameWindow = new Frame("Atari Breakout – Nivel Medio");
	        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	        gameWindow.setSize(screen.width, screen.height);
	        gameWindow.setLayout(new BorderLayout());
	        gameWindow.setResizable(false);
	        gameWindow.setUndecorated(true);

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
	    
	    
	    //ejecuta nivel dificil
	    public static void launchDificilLevel() {

	        if (gameWindow != null) gameWindow.dispose(); 
	        gameWindow = new Frame("Atari Breakout – Nivel Dificil");
	        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	        gameWindow.setSize(screen.width, screen.height);
	        gameWindow.setLayout(new BorderLayout());
	        gameWindow.setResizable(false);
	        gameWindow.setUndecorated(true);

	        NivelDificil hardCanvas = new NivelDificil(screen.width, screen.height);
	        currentCanvas = hardCanvas;
	        gameWindow.add(hardCanvas, BorderLayout.CENTER);

	        gameWindow.addWindowListener(new WindowAdapter() {
	            @Override
	            public void windowClosing(WindowEvent e) {
	                gameWindow.dispose();
	                MainMenu.main(null);
	            }
	        });

	        gameWindow.setVisible(true);
	        hardCanvas.requestFocus();
	        new Thread(hardCanvas).start();
	    }
	    

	    //ejecuta nivel extra
	    public static void launchExtraLevel() {

	        if (gameWindow != null) gameWindow.dispose(); 
	        gameWindow = new Frame("Atari Breakout – Nivel Dificil");
	        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	        gameWindow.setSize(screen.width, screen.height);
	        gameWindow.setLayout(new BorderLayout());
	        gameWindow.setResizable(false);
	        gameWindow.setUndecorated(true);

	        NivelExtra extraCanvas = new NivelExtra(screen.width, screen.height);
	        currentCanvas = extraCanvas;
	        gameWindow.add(extraCanvas, BorderLayout.CENTER);

	        gameWindow.addWindowListener(new WindowAdapter() {
	            @Override
	            public void windowClosing(WindowEvent e) {
	                gameWindow.dispose();
	                MainMenu.main(null);
	            }
	        });

	        gameWindow.setVisible(true);
	        extraCanvas.requestFocus();
	        new Thread(extraCanvas).start();
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