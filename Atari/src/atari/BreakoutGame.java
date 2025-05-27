package atari;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;



/**
 * Clase de ejecución del programa
 * 
 * Cada nivel tiene su propio metodo de ejecucion ya que poseen diferentes caracteristicas
 * Se usa el metodo restart para resetear todos los valores de X partida
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
	        // No usar pack() queremos tamaño exacto
	        gameWindow.setVisible(true);
	        canvas.requestFocus();

	        gameWindow.addWindowListener(new java.awt.event.WindowAdapter() {
	            public void windowClosing(java.awt.event.WindowEvent e) {
	                gameWindow.dispose(); //se cierra cada pantalla tras abrir otra
	                MainMenu.main(null);
	            }
	        });

	        new Thread(canvas).start();
	    }
	    
	    //ejecuta nivel medio
	    public static void launchMediumLevel() {	    	
	        if (gameWindow != null) gameWindow.dispose(); // Si hay una ventana anterior, se cierra

	        gameWindow = new Frame("Atari Breakout – Nivel Medio");
	        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize(); //pantalla completa
	        gameWindow.setSize(screen.width, screen.height);
	        gameWindow.setLayout(new BorderLayout());
	        gameWindow.setResizable(false);
	        gameWindow.setUndecorated(true); //sin bordes y sin poder modificar la pantalla

	        NivelMedio mediumCanvas = new NivelMedio(screen.width, screen.height); //llama a obj de la clase que queremos ejecutar, en este caso es pa nivelMedio
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
	        mediumCanvas.requestFocus(); //mediumCanvas recibe el foco del teclado (pulsar teclas)
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
	            nivelMedio.resetGame(); // método de la clase NivelMedio, se llama igual q el metodo del nivel 1 pero hacen una ejecucion diferente
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

	    // Entrada por defecto para abrir el juego
	    public static void main(String[] args) {
	        MainMenu.main(args);
	    }
	}