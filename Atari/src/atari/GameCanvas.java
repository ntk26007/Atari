package atari;

import java.awt.Button;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Label;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class GameCanvas extends Canvas implements Runnable, KeyListener {
	private int width, height;
	private boolean running = false;
	private Paddle paddle;
	private Ball ball;
	private BrickManager bricks;
	private boolean leftPressed, rightPressed;
	private int lives;
	private int score;

	public GameCanvas(int width, int height) {
		this.width = width;
		this.height = height;
		setPreferredSize(new Dimension(width, height));
		addKeyListener(this);
		setFocusable(true);
		requestFocus();
		initGame();
	}

	// Resetea estado para reiniciar partida
	public void resetGame() {
		initGame();
		running = true;
	}

	private void initGame() {
		// Centro pala y pelota
		paddle = new Paddle((width - 100) / 2, height - 50, 100, 10, width);
		ball = new Ball(width / 2, height / 2, 10, 4, 4, width, height);

		// bloques
		int cols = 12; // número de columnas
		int rows = 6; // número de filas
		int spacing = 5; // espacio base entre ladrillos
		int sideMargin = width / 20; // margen lateral 
		int totalSpacingX = (cols + 1) * spacing + sideMargin * 2;
		int brickWidth = (width - totalSpacingX) / cols;
		int brickHeight = height / 25; // altura pantalla
		int offsetX = sideMargin + spacing; // margen izquierdo
		int offsetY = spacing * 9; // margen superior para vidas/puntuación
		bricks = new BrickManager(rows, cols, brickWidth, brickHeight, offsetX, offsetY);

		// Vidas y puntuación
		lives = 3;
		score = 0;
		lives = 3;
		score = 0;
	}

	@Override
	public void run() {
		running = true;
		long lastTime = System.nanoTime();
		double nsPerUpdate = 1e9 / 60.0;
		double delta = 0;
		while (running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / nsPerUpdate;
			lastTime = now;
			while (delta >= 1) {
				update();
				delta--;
			}
			render();
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
			}
		}
	}

	//movimiento pala
	private void update() {
		boolean wasWaiting = ball.isWaiting();
		if (!ball.isWaiting()) {
			if (leftPressed)
				paddle.moveLeft();
			if (rightPressed)
				paddle.moveRight();
			ball.update();
			ball.checkWallCollision();
			ball.checkPaddleCollision(paddle);
			if (bricks.checkBallCollision(ball))
				score++;
			if (bricks.isEmpty()) {
			    running = false;
			    winMenu(); // Mostrar el menú de victoria
			}

		} else {
			ball.update();
		}
		if (!wasWaiting && ball.isWaiting()) {
			lives--;
			if (lives <= 0) {
				running = false;
				showGameOverMenu();
			}
		}
	}

	//evitar el parpadeo
	private void render() {
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(3);
			return;
		}
		Graphics2D g2 = (Graphics2D) bs.getDrawGraphics();

		// Fondo del juego base
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, width, height);

		// Vidas y puntuación
		g2.setColor(Color.WHITE);
		Font original = g2.getFont();
		g2.setFont(original.deriveFont(original.getSize2D() + 4f));
		g2.drawString("Vidas: " + lives, 10, 20 + 4);
		g2.drawString("Puntuación: " + score, width - 140, 20 + 4);
		g2.setFont(original);

		// Dibujo de juego
		paddle.draw(g2);
		ball.draw(g2);
		bricks.draw(g2);

		g2.dispose();
		bs.show();
	}

	//muestra ventana cuando se acaban las vidas
	private void showGameOverMenu() {
	    Frame menu = new Frame("Game Over") {
	        private Image background = Toolkit.getDefaultToolkit().getImage("resources/1.jpg");

	        {
	            Toolkit.getDefaultToolkit().prepareImage(background, -1, -1, null);
	        }

	        @Override
	        public void paint(Graphics g) {
	            g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
	            super.paint(g);
	        }
	    };
	    
	    menu.setResizable(false);
	    int w = 400, h = 250;
	    menu.setSize(w, h);
	    menu.setLayout(null);
	    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	    menu.setLocation((screen.width - w) / 2, (screen.height - h) / 2);

	    Button retry = new Button("Reintentar");
	    retry.setBounds(60, 120, 120, 40);
	    menu.add(retry);

	    Button mainMenu = new Button("Menú");
	    mainMenu.setBounds(220, 120, 120, 40);
	    menu.add(mainMenu);

	    //lleva al mismo nivel (cuando se implemente el nivel medio mirar pa q vuelva a ese nivel y no al 1)
	    retry.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            menu.dispose();
	            BreakoutGame.restartGame();
	        }
	    });
	    
	    //lleva a menu
	    mainMenu.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            menu.dispose();
	            BreakoutGame.returnToMenu();
	        }
	    });

	    menu.addWindowListener(new WindowAdapter() {
	        public void windowClosing(WindowEvent e) {
	            menu.dispose();
	        }
	    });

	    menu.setVisible(true);
	}

	//pulsar flechas para jugar
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_LEFT)
			leftPressed = true;
		if (e.getKeyCode() == KeyEvent.VK_RIGHT)
			rightPressed = true;
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_LEFT)
			leftPressed = false;
		if (e.getKeyCode() == KeyEvent.VK_RIGHT)
			rightPressed = false;
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}
	
	//cuando se rompen todos los blques
	 private void winMenu() {
	        Frame winMenu = new Frame("¡Nivel Completado!") {
	            private Image bgImage = Toolkit.getDefaultToolkit().getImage("resources/1.jpg");
	            {
	                Toolkit.getDefaultToolkit().prepareImage(bgImage, -1, -1, null);
	            }
	            @Override
	            public void paint(Graphics g) {
	                Dimension size = getSize();
	                g.drawImage(bgImage, 0, 0, size.width, size.height, this);
	                super.paint(g);
	            }
	        };

	        winMenu.setResizable(false);
	        int w = 400, h = 300;
	        winMenu.setSize(w, h);
	        winMenu.setLayout(null);
	        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	        winMenu.setLocation((screen.width - w) / 2, (screen.height - h) / 2);

	        int bw = 200, bh = 40, bx = (w - bw) / 2;
	        int baseY = 80;

	        Button retryBtn = new Button("Reintentar");
	        retryBtn.setBounds(bx, baseY, bw, bh);
	        winMenu.add(retryBtn);

	        Button nextBtn = new Button("Siguiente Nivel");
	        nextBtn.setBounds(bx, baseY + 60, bw, bh);
	        winMenu.add(nextBtn);

	        Button menuBtn = new Button("Menú");
	        menuBtn.setBounds(bx, baseY + 120, bw, bh);
	        winMenu.add(menuBtn);

	        retryBtn.addActionListener(e -> {
	            winMenu.dispose();
	            BreakoutGame.restartGame();
	        });

	        nextBtn.addActionListener(e -> {
	            winMenu.dispose();
	            BreakoutGame.launchMediumLevel(); //lógica del nivel medio
	        });

	        menuBtn.addActionListener(e -> {
	            winMenu.dispose();
	            BreakoutGame.returnToMenu();
	        });

	        winMenu.addWindowListener(new WindowAdapter() {
	            public void windowClosing(WindowEvent e) {
	                winMenu.dispose();
	            }
	        });

	        winMenu.setResizable(false);
	        winMenu.setVisible(true);
	    }
	 
	// Nuevo método para mostrar pantalla de selección de nivel en el menu
	 public static void showLevelSelectionMenu() {
	        Frame levelFrame = new Frame("Seleccionar Nivel") {
	            private Image bg = Toolkit.getDefaultToolkit().getImage("resources/1.jpg");
	            {
	                Toolkit.getDefaultToolkit().prepareImage(bg, -1, -1, null);
	            }

	            @Override
	            public void paint(Graphics g) {
	                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
	                super.paint(g);
	            }
	        };
	        
	        levelFrame.setResizable(false);
	        int w = 1000, h = 700;
	        levelFrame.setSize(w, h);
	        levelFrame.setLayout(null);
	        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize(); //tamaño
	        levelFrame.setLocation((screen.width - w) / 2, (screen.height - h) / 2);
	        
	        

	     // Imágenes de niveles
	        //Image imgFacil = Toolkit.getDefaultToolkit().getImage("resources/star1.png");
	        Image imgMedio = Toolkit.getDefaultToolkit().getImage("resources/star2.png");
	        Image imgDificil = Toolkit.getDefaultToolkit().getImage("resources/star3.png");

	        // ------- Vista previa nivel Fácil -------
	        JPanel previewFacil = new JPanel() {
	            BufferedImage img;

	            {
	                try {
	                    img = ImageIO.read(new File("resources/star1.png")); // Usa la ruta de tu imagen
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	                setOpaque(false); // Para que el fondo del panel también sea transparente
	            }

	            @Override
	            protected void paintComponent(Graphics g) {
	                super.paintComponent(g);
	                if (img != null) {
	                    g.drawImage(img, 0, 0, 240, 180, this);
	                }
	            }
	        };
	        previewFacil.setBounds(120, 150, 240, 160);
	        levelFrame.add(previewFacil);

	        // Botón Fácil
	        BotonPersonalizado boton_facil = new BotonPersonalizado("resources/facil.png", 170, 60);
	        boton_facil.setBounds(120, 330, 240, 60);
	        boton_facil.setAccion(() -> {
	            levelFrame.dispose();
	            BreakoutGame.launchGame(); //se ejecuta el nivel + facil (por defecto)
	        });
	        levelFrame.add(boton_facil);

	        // ------- Vista previa nivel Intermedio, cambiar a como lo facil pero se ve mal -------
	        Canvas previewMedio = new Canvas() {
	            public void paint(Graphics g) {
	                g.drawImage(imgMedio, 0, 0, 240, 180, this);
	            }
	        };
	        previewMedio.setBounds(390, 150, 240, 160);
	        levelFrame.add(previewMedio);

	        // Botón Intermedio
	        BotonPersonalizado boton_medio = new BotonPersonalizado("resources/medio.png", 170, 60);
	        boton_medio.setBounds(390, 330, 240, 60);
	        boton_medio.setAccion(() -> {
	            levelFrame.dispose();
	            BreakoutGame.launchMediumLevel(); // se ejecuta el nivel medio
	        });
	        levelFrame.add(boton_medio);
	        
	        

	        // ------- Vista previa nivel Difícil -------
	        Canvas previewDificil = new Canvas() {
	            public void paint(Graphics g) {
	                g.drawImage(imgDificil, 0, 0, 240, 180, this);
	            }
	        };
	        previewDificil.setBounds(660, 150, 240, 160);
	        levelFrame.add(previewDificil);

	        // Botón Difícil
	        BotonPersonalizado boton_dificil = new BotonPersonalizado("resources/dificil.png", 170, 60);
	        boton_dificil.setBounds(660, 330, 240, 60);
	        boton_dificil.setAccion(() -> {
	            levelFrame.dispose();
	            // BreakoutGame.startLevel(2);
	        });
	        levelFrame.add(boton_dificil);


	        levelFrame.addWindowListener(new WindowAdapter() {
	            public void windowClosing(WindowEvent e) {
	                levelFrame.dispose();
	            }
	        });

	        levelFrame.setVisible(true);
	    }
}