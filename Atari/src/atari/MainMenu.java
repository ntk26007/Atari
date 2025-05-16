package atari;

import java.awt.Button;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class MainMenu extends Frame {
	 private Image bgImage;
	 int bx = 100; // coordenada x
	 int bw = 200; // ancho del botón
	 int bh = 60;  // alto del botón
	 int sh = 600; 

	    public MainMenu() {
	        super("Menú Principal");

	        // Cargar imagen
	        bgImage = Toolkit.getDefaultToolkit().getImage("resources/2.jpg");

	        // Pantalla completa
	        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	        int sw = screenSize.width, sh = screenSize.height;
	        setSize(sw, sh);
	        setLayout(null);

	        // Botones
	        int bw = sw / 4;
	        int bh = sh / 10;
	        int bx = (sw - bw) / 2;

	        BotonPersonalizado btnPlay = new BotonPersonalizado("resources/jugar.png", bw, bh);
	        btnPlay.setLocation(bx, sh / 2 - bh * 2);
	        add(btnPlay);

	        // Botón "Niveles"
	        BotonPersonalizado btnLevel = new BotonPersonalizado("resources/niveles.png", bw, bh);
	        btnLevel.setLocation(bx, sh / 2 - bh / 2);
	        add(btnLevel);

	        // Botón "Salir del juego"
	        BotonPersonalizado btnExit = new BotonPersonalizado("resources/salir.png", bw, bh);
	        btnExit.setLocation(bx, sh / 2 + bh);
	        add(btnExit);

	        // Eventos
	        btnPlay.setAccion(()-> {
	            dispose();
	            BreakoutGame.launchGame();
	        });
	        
	        btnLevel.setAccion(()-> { 
	        	GameCanvas.showLevelSelectionMenu();
	        	});
	        
	        btnExit.setAccion(() -> {
                System.exit(0);
            });

	        // Cerrar pantalla
	        addWindowListener(new WindowAdapter() {
	            public void windowClosing(WindowEvent e) {
	                System.exit(0);
	            }
	        });

	        setResizable(false);
	        setVisible(true);
	    }

	    // Pintar la imagen de fondo directamente 
	    public void paint(Graphics g) {
	        if (bgImage != null) {
	            g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
	        }
	    }

	    public static void main(String[] args) {
	        new MainMenu();
	    }
	}