package atari;

import java.awt.Button;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.Random;

public class NivelMedio extends Canvas implements Runnable, KeyListener {
	private int width, height;
	private Paddle paddle;
	private Ball ball;
	private DurabilityBrickManager bricks;
	private boolean leftPressed, rightPressed;
	private int lives = 3;
	private int score = 0;
	private boolean running = false;
	private Thread gameThread;
	private boolean paused = false;


	public NivelMedio(int width, int height) {
		this.width = width;
		this.height = height;
		setPreferredSize(new Dimension(width, height));
		addKeyListener(this);
		setFocusable(true);
		requestFocus();
		initGame();
		
		AudioPlayer.detenerAudio();
	    AudioPlayer.reproducirAudio("Resources/medio.wav");
	}
	
	// Resetea estado para reiniciar el nivel medio
		public void resetGame() {
			leftPressed = false;  // Evita movimiento automático hacia la izquierda
			rightPressed = false; // Evita movimiento automático hacia la derecha
			initGame();
			running = true;
		}

	private void initGame() {
		//pala + pelota
		paddle = new Paddle((width - 100) / 2, height - 50, 100, 10, width);
		ball = new Ball(width / 2, height / 2, 10, 4, 4, width, height);
		
		//bloques
		int rows = 9, cols = 12;
		int spacing = 5;
		int sideMargin = width / 20;
		int totalSpacingX = (cols + 1) * spacing + sideMargin * 2;
		int brickW = (width - totalSpacingX) / cols;
		int brickH = height / 25;
		int offsetX = sideMargin + spacing;
		int offsetY = spacing * 9;
		bricks = new DurabilityBrickManager(rows, cols, brickW, brickH, offsetX, offsetY);
		
		//puntuacion
		lives = 3;
		score = 0;
		lives = 3;
		score = 0;
		
	}

	@Override
	public void run() {
		running = true;
		long last = System.nanoTime();
		double nsPerUpdate = 1e9 / 60.0, delta = 0;
		while (running) {
			long now = System.nanoTime();
			delta += (now - last) / nsPerUpdate;
			last = now;
			while (delta >= 1) {
				update();
				delta--;
			}
			render();
			try {
				Thread.sleep(2);
			} catch (InterruptedException ex) {
			}
		}
	}

	private void update() {
		if (paused) return; // Si está en pausa, no actualiza
	    boolean w = ball.isWaiting();
	    if (leftPressed)
	        paddle.moveLeft();
	    if (rightPressed)
	        paddle.moveRight();
	    if (!w) {
	        ball.update();
	        ball.checkWallCollision();
	        ball.checkPaddleCollision(paddle);

	        int puntos = bricks.checkBallCollision(ball); //durabilidad bloques
	        score += puntos;

	        if (puntos > 0) {
	            AudioPlayer.reproducirEfecto("Resources/bloque.wav");
	        }

	        if (bricks.isEmpty()) {
	            running = false;
	            winMenu2();
	        }
	    } else {
	        ball.update();
	    }
	    if (!w && ball.isWaiting()) {
	        lives--;
	        if (lives <= 0) {
	            running = false;
	            showGameOverMenu2();
	        } else {
	            ball.resetPosition(paddle.getX() + paddle.getWidth() / 2, paddle.getY() - ball.getDiameter());
	        }
	    }
	}


	//Perder
	private void showGameOverMenu2() {
		Frame menu = new Frame("Game Over") {
			private Image background = Toolkit.getDefaultToolkit().getImage("resources/sombra2 (1).jpg");

			{
				Toolkit.getDefaultToolkit().prepareImage(background, -1, -1, null);
			}

			 Font fuentePersonalizada = FuentePersonalizada.cargarFuente(48f);
				@Override
				public void paint(Graphics g) {
					g.drawImage(background, 0, 0, getWidth(), getHeight(), this);

					// Dibuja el texto
					Graphics2D g2d = (Graphics2D) g;
					g2d.setColor(Color.RED); 
					g2d.setFont(fuentePersonalizada); // Fuente del texto

					String text = "GAME OVER";
					FontMetrics fm = g2d.getFontMetrics();
					int textWidth = fm.stringWidth(text);
					int x = (getWidth() - textWidth) / 2;
					int y = 100; // Ajusta según posición deseada sobre los botones

					g2d.drawString(text, x, y);

					super.paint(g);
					
				}
			
		};

		Font fuentePersonalizada = FuentePersonalizada.cargarFuente(18f);
		menu.setResizable(false);
		int w = 500, h = 300;
		menu.setSize(w, h);
		menu.setLayout(null);
		menu.setUndecorated(true);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		menu.setLocation((screen.width - w) / 2, (screen.height - h) / 2);

		//colocar botones
				int buttonWidth = 140;
				int buttonHeight = 50;
				int buttonY = 220;
				int spacing = 40;


				Button retry = new Button("Reintentar");
				retry.setBackground(Color.GREEN);
				retry.setBounds((w / 2) - buttonWidth - (spacing / 2), buttonY, buttonWidth, buttonHeight);
				retry.setFont(fuentePersonalizada); // Fuente del texto
				menu.add(retry);

				Button mainMenu = new Button("Volver a menú");
				mainMenu.setBackground(Color.cyan);
				mainMenu.setBounds((w / 2) + (spacing / 2), buttonY, buttonWidth, buttonHeight);
				mainMenu.setFont(fuentePersonalizada); // Fuente del texto
				menu.add(mainMenu);

		retry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetGame();
				requestFocus();

				if (gameThread == null || !gameThread.isAlive()) {
				    gameThread = new Thread(NivelMedio.this);
				    gameThread.start();
				}

				menu.dispose();
				AudioPlayer.detenerAudio(); 
		        AudioPlayer.reproducirAudio("Resources/medio.wav");
				BreakoutGame.restartNivelMedio();
			}
		});

		mainMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				menu.dispose();
				AudioPlayer.detenerAudio(); 
		        AudioPlayer.reproducirAudio("Resources/menu.wav");
				BreakoutGame.returnToMenu();
			}
		});

		menu.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				menu.dispose();
			}
		});
		
		AudioPlayer.detenerAudio();
		AudioPlayer.reproducirAudioUnaVez("Resources/gameOver.wav");

		menu.setVisible(true);
	}

	
	//Ganar
	private void winMenu2() {
		Frame winMenu = new Frame("¡Nivel Completado!") {
	        private Image bgImage = Toolkit.getDefaultToolkit().getImage("resources/sombra1 (1).jpg");

	        {
	            Toolkit.getDefaultToolkit().prepareImage(bgImage, -1, -1, null);
	        }

	        @Override
	        public void paint(Graphics g) {
	            Dimension size = getSize();
	            g.drawImage(bgImage, 0, 0, size.width, size.height, this);

	            // Texto en la parte superior
	            String titulo = "Nivel Completado!";
	            g.setColor(Color.WHITE);
	            Font fuenteTitulo = FuentePersonalizada.cargarFuente(28f); // Tamaño proporcional
	            g.setFont(fuenteTitulo);
	            FontMetrics fm = g.getFontMetrics();
	            int x = (size.width - fm.stringWidth(titulo)) / 2;
	            int y = fm.getAscent() + 30; // margen desde arriba
	            g.drawString(titulo, x, y);

	            super.paint(g);
	        }
	    };

	    Font fuentePersonalizada = FuentePersonalizada.cargarFuente(18f);
	    winMenu.setResizable(false);
	    int w = 500, h = 300;
	    winMenu.setSize(w, h);
	    winMenu.setLayout(null);
	    winMenu.setUndecorated(true);
	    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	    winMenu.setLocation((screen.width - w) / 2, (screen.height - h) / 2);

	    int bw = 200, bh = 40, bx = (w - bw) / 2;
	    int baseY = 100; // Se baja para dejar espacio al texto

	    Button retryBtn = new Button("Reintentar");
	    retryBtn.setBackground(Color.GREEN);
	    retryBtn.setBounds(bx, baseY, bw, bh);
	    retryBtn.setFont(fuentePersonalizada);
	    winMenu.add(retryBtn);

	    Button nextBtn = new Button("Siguiente Nivel");
	    nextBtn.setBackground(Color.YELLOW);
	    nextBtn.setBounds(bx, baseY + 60, bw, bh);
	    nextBtn.setFont(fuentePersonalizada);
	    winMenu.add(nextBtn);

	    Button menuBtn = new Button("Menú");
	    menuBtn.setBackground(Color.CYAN);
	    menuBtn.setBounds(bx, baseY + 120, bw, bh);
	    menuBtn.setFont(fuentePersonalizada);
	    winMenu.add(menuBtn);

		
		retryBtn.addActionListener(e -> {
			winMenu.dispose();
			AudioPlayer.reproducirAudio("Resources/medio.wav");
			BreakoutGame.restartNivelMedio();
		});

		nextBtn.addActionListener(e -> {
			winMenu.dispose();
			AudioPlayer.detenerAudio(); 
	        AudioPlayer.reproducirAudio("Resources/facil.wav");
			BreakoutGame.launchDificilLevel();
		});

		menuBtn.addActionListener(e -> {
			winMenu.dispose();
			AudioPlayer.detenerAudio(); 
		    AudioPlayer.reproducirAudio("Resources/menu.wav");
			BreakoutGame.returnToMenu();
		});

		winMenu.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				winMenu.dispose();
			}
		});

		AudioPlayer.detenerAudio();
	    AudioPlayer.reproducirAudioUnaVez("Resources/win.wav"); 
		
		winMenu.setResizable(false);
		winMenu.setVisible(true);
	}

	private void render() {
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(3);
			return;
		}
		Graphics2D g = (Graphics2D) bs.getDrawGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, width, height);
		g.setColor(Color.WHITE);
		Font f = g.getFont();
		g.setFont(f.deriveFont(f.getSize2D() + 4f));
		g.drawString("Vidas: " + lives, 10, 24);
		g.drawString("Puntuación: " + score, width - 160, 24);
		g.setFont(f);
		paddle.draw(g);
		ball.draw(g);
		bricks.draw(g);
		// Mostrar mensaje de pausa si está pausado
	    if (paused) {
	    	Font fuentePersonalizada = FuentePersonalizada.cargarFuente(48f);
	        g.setFont(fuentePersonalizada);
	        String texto = "PAUSA";
	        int x = (width - g.getFontMetrics().stringWidth(texto)) / 2;
	        int y = height / 2;

	        // Sombra para visibilidad
	        g.setColor(Color.BLACK);
	        g.drawString(texto, x + 2, y + 2);
	        g.setColor(Color.RED);
	        g.drawString(texto, x, y);
	    }
		g.dispose();
		bs.show();
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_LEFT)
			leftPressed = true;
		if (e.getKeyCode() == KeyEvent.VK_RIGHT)
			rightPressed = true;
		if (e.getKeyCode() == KeyEvent.VK_P)
	        paused = !paused; // Alterna pausa/reanudar
		//if (e.getKeyCode() == KeyEvent.VK_P) running = !running; // pausa pero al volver a pulsar no se quita la pausa
	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_LEFT)
			leftPressed = false;
		if (e.getKeyCode() == KeyEvent.VK_RIGHT)
			rightPressed = false;
	}

	public void keyTyped(KeyEvent e) {
	}

	
	
	/*
	 * Clases internas
	 * 
	 * 1º Maneja durabilidad de todos los bloques recorriendolos
	 * 
	 * 2º Maneja los golpes dados segun el bloque (color)
	 */
	class DurabilityBrickManager {
		private DurabilityBrick[][] grid;
		private int rows, cols;
		private Random rand = new Random();

		public DurabilityBrickManager(int rows, int cols, int bw, int bh, int ox, int oy) {
			this.rows = rows;
			this.cols = cols;
			grid = new DurabilityBrick[rows][cols];
			for (int r = 0; r < rows; r++)
				for (int c = 0; c < cols; c++) {
					int x = ox + c * (bw + 5), y = oy + r * (bh + 5);
					int dur = rand.nextBoolean() ? 2 : 1; //establece aleatorio la durabilidad de todos los bloques al recorrerlos
					grid[r][c] = new DurabilityBrick(x, y, bw, bh, dur);
				}
		}

		//maneja la durabilidad de los ladrillos
		public int checkBallCollision(Ball ball) {
		    if (ball.isWaiting())
		        return 0;

		    for (DurabilityBrick[] row : grid) {
		        for (DurabilityBrick b : row) {
		            if (!b.isBroken() && ball.getBounds().intersects(b.getBounds())) { //intersects = comprueba si dos bloques se superponen y evita q choquen entre si
		                b.hit();
		                ball.reverseY(); //clase Ball
		                if (b.isBroken()) {
		                    return 2; // ladrillo destruido (dos toques)
		                } else {
		                    return 1; // solo dañado
		                }
		            }
		        }
		    }

		    return 0; // sin colisión
		}


		public boolean isEmpty() {
			for (DurabilityBrick[] r : grid)
				for (DurabilityBrick b : r)
					if (!b.isBroken())
						return false;
			return true;
		}

		public void draw(Graphics2D g) {
			for (DurabilityBrick[] r : grid)
				for (DurabilityBrick b : r)
					b.draw(g);
		}
	}

	class DurabilityBrick {
		private int x, y, w, h, hits;
		private boolean broken;
		private Color color;

		//constructor que establece que si golpea el verde, se rompe a los dos golpes, si golpea al azul se rompe de una
		public DurabilityBrick(int x, int y, int w, int h, int hits) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.hits = hits;
			this.color = hits == 2 ? Color.GREEN : Color.CYAN;
		}

		public void hit() {
			hits--;
			if (hits <= 0)
				broken = true;
		}

		public boolean isBroken() {
			return broken;
		}

		public Rectangle getBounds() {
			return new Rectangle(x, y, w, h);
		}

		public void draw(Graphics2D g) {
			if (!broken) {
				g.setColor(color);
				g.fillRect(x, y, w, h);
				g.setColor(Color.BLACK);
				g.drawRect(x, y, w, h);
			}
		}
	}
}
