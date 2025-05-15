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

	    public MainMenu() {
	        super("Menú Principal");

	        // Cargar imagen
	        bgImage = Toolkit.getDefaultToolkit().getImage("resources/2.jpg");

	        // Pantalla completa
	        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	        int sw = screenSize.width, sh = screenSize.height;
	        setSize(sw, sh);
	        setLayout(null); // Posicionamiento manual

	        // Botones
	        int bw = sw / 4;
	        int bh = sh / 10;
	        int bx = (sw - bw) / 2;

	        Button btnPlay = new Button("Jugar");
	        btnPlay.setBounds(bx, sh / 2 - bh * 2, bw, bh);
	        add(btnPlay);

	        Button btnLevel = new Button("Niveles");
	        btnLevel.setBounds(bx, sh / 2 - bh / 2, bw, bh);
	        add(btnLevel);

	        Button btnExit = new Button("Salir del juego");
	        btnExit.setBounds(bx, sh / 2 + bh, bw, bh);
	        add(btnExit);

	        // Eventos
	        btnPlay.addActionListener(e -> {
	            dispose();
	            BreakoutGame.launchGame();
	        });
	        btnLevel.addActionListener(e -> GameCanvas.showLevelSelectionMenu());
	        btnExit.addActionListener(e -> System.exit(0));

	        // Listener de cierre
	        addWindowListener(new WindowAdapter() {
	            public void windowClosing(WindowEvent e) {
	                System.exit(0);
	            }
	        });

	        setResizable(false);
	        setVisible(true);
	    }

	    // 🔁 Pintar la imagen de fondo directamente en el Frame
	    public void paint(Graphics g) {
	        if (bgImage != null) {
	            g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
	        }
	    }

	    public static void main(String[] args) {
	        new MainMenu();
	    }
	}