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

import atari.NivelMedio.DurabilityBrick;
import atari.NivelMedio.DurabilityBrickManager;

public class NivelDificil extends Canvas implements Runnable, KeyListener {
	private int width, height;
	private Paddle paddle;
	private Ball ball;
	private DurabilityBrickManager bricks;
	private boolean leftPressed, rightPressed;
	private int lives = 3;
	private int score = 0;
	private boolean running = false;
	private Thread gameThread;
	private long startTime;
	private final int TIME_LIMIT = 300_000; // 300,000 ms = 5 minutos
	private boolean paused = false;


	public NivelDificil(int width, int height) {
		this.width = width;
		this.height = height;
		setPreferredSize(new Dimension(width, height));
		addKeyListener(this);
		setFocusable(true);
		requestFocus();
		initGame();
		
		AudioPlayer.detenerAudio();
	    AudioPlayer.reproducirAudio("Resources/dificil.wav");
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
		ball = new Ball(width / 2, height - 280, 10, 4, 4, width, height);

		
		//bloques
		int rows = 12, cols = 12;
		int spacing = 5;
		int sideMargin = width / 20;
		int totalSpacingX = (cols + 1) * spacing + sideMargin * 2;
		int brickW = (width - totalSpacingX) / cols;
		int brickH = height / 25;
		int offsetX = sideMargin + spacing;
		int offsetY = spacing * 9;
		bricks = new DurabilityBrickManager(rows, cols, brickW, brickH, offsetX, offsetY);
		
		//puntuacion
		lives = 2;
		score = 0;
		lives = 2;
		score = 0;
		
	}

	@Override
	public void run() {
		running = true;
		long last = System.nanoTime();
		double nsPerUpdate = 1e9 / 60.0, delta = 0;
		startTime = System.currentTimeMillis(); //para el temporizador

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
		long elapsed = System.currentTimeMillis() - startTime;
		if (elapsed >= TIME_LIMIT) {
		    running = false;
		    showOverTime(); //cuando se acaba el tiempo salta la pantalla de perder
		    return;
		}

		boolean w = ball.isWaiting();
		if (leftPressed)
			paddle.moveLeft();
		if (rightPressed)
			paddle.moveRight();
		if (!w) {
//			if (leftPressed)
//				paddle.moveLeft();
//			if (rightPressed)
//				paddle.moveRight();
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
				winMenu3();
			}
		} else {
			ball.update();
		}
		if (!w && ball.isWaiting()) {
			lives--;
			if (lives <= 0) {
				running = false;
				showGameOverMenu3();
			} else {
				ball.resetPosition(paddle.getX() + paddle.getWidth() / 2, paddle.getY() - ball.getDiameter());
			}
		}
	}
	
	//cuando se acaba el tiempo salta otra ventana
	private void showOverTime() {
	    Frame timeOver = new Frame("¡Tiempo agotado!") {
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

	    timeOver.setSize(400, 250);
	    timeOver.setLayout(null);
		timeOver.setUndecorated(true);
	    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	    timeOver.setLocation((screen.width - 400) / 2, (screen.height - 250) / 2);

	    Button retry = new Button("Reintentar");
	    retry.setBounds(60, 120, 120, 40);
	    timeOver.add(retry);

	    Button mainMenu = new Button("Menú");
	    mainMenu.setBounds(220, 120, 120, 40);
	    timeOver.add(mainMenu);

	    retry.addActionListener(e -> {
	        resetGame();
	        requestFocus();
	        if (gameThread == null || !gameThread.isAlive()) {
	            gameThread = new Thread(NivelDificil.this);
	            gameThread.start();
	        }
	        timeOver.dispose();
	    });

	    mainMenu.addActionListener(e -> {
	        timeOver.dispose();
	    	AudioPlayer.detenerAudio(); 
	        AudioPlayer.reproducirAudio("Resources/menu.wav");
	        BreakoutGame.returnToMenu();
	    });
	    
	    AudioPlayer.detenerAudio();
		AudioPlayer.reproducirAudioUnaVez("Resources/gameOver.wav");

	    timeOver.setVisible(true);
	}

	

	//Perder
	private void showGameOverMenu3() {
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
		retry.setFont(fuentePersonalizada);
		menu.add(retry);

		Button mainMenu = new Button("Menú");
		mainMenu.setBackground(Color.CYAN);
		mainMenu.setBounds((w / 2) + (spacing / 2), buttonY, buttonWidth, buttonHeight);
		mainMenu.setFont(fuentePersonalizada);
		menu.add(mainMenu);
		
		retry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetGame();
				requestFocus();

				if (gameThread == null || !gameThread.isAlive()) {
				    gameThread = new Thread(NivelDificil.this);
				    gameThread.start();
				}

				AudioPlayer.detenerAudio(); 
		        AudioPlayer.reproducirAudio("Resources/facil.wav");
				menu.dispose();
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
	private void winMenu3() {
		Frame winMenu = new Frame("\u00a1Nivel Completado!") {
			private Image bgImage = Toolkit.getDefaultToolkit().getImage("resources/sombra1 (1).jpg");
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

		Font fuentePersonalizada = FuentePersonalizada.cargarFuente(18f);
		winMenu.setResizable(false);
		int w = 500, h = 300;
		winMenu.setSize(w, h);
		winMenu.setLayout(null);
		winMenu.setUndecorated(true);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		winMenu.setLocation((screen.width - w) / 2, (screen.height - h) / 2);

		int bw = 200, bh = 40, bx = (w - bw) / 2;
		int baseY = 80;

		Button retryBtn = new Button("Reintentar");
		retryBtn.setBackground(Color.GREEN);
		retryBtn.setBounds(bx, baseY, bw, bh);
		retryBtn.setFont(fuentePersonalizada); // Fuente del texto
		winMenu.add(retryBtn);
		
		Button nextBtn = new Button("Nivel Extra");
		nextBtn.setBackground(Color.YELLOW);
		nextBtn.setBounds(bx, baseY + 60, bw, bh);
		nextBtn.setFont(fuentePersonalizada); // Fuente del texto
		winMenu.add(nextBtn);

		Button menuBtn = new Button("Menú");
		menuBtn.setBackground(Color.CYAN);
		menuBtn.setBounds(bx, baseY + 120, bw, bh);
		menuBtn.setFont(fuentePersonalizada); // Fuente del texto
		winMenu.add(menuBtn);

		retryBtn.addActionListener(e -> {
			winMenu.dispose();
			//BreakoutGame.restartNivelDificil(); funciona bien sin poner esto
		});
		
		nextBtn.addActionListener(e -> {
			winMenu.dispose();
			AudioPlayer.detenerAudio(); 
	        AudioPlayer.reproducirAudio("Resources/facil.wav");
			BreakoutGame.launchExtraLevel();
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
		
		//para el temporizador, lo dibuja en la pantalla
		long remainingTime = Math.max(0, TIME_LIMIT - (System.currentTimeMillis() - startTime));
		long seconds = remainingTime / 1000;
		g.drawString("Tiempo: " + seconds + "s", width / 2 - 40, 24);
		
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
	 * Clases internas para la durabilidad de cada nivel
	 */
	class DurabilityBrickManager {
		private DurabilityBrick[][] grid;
		private int rows, cols;
		private Random rand = new Random();
		Color[] colores = {
			    Color.RED,
			    Color.ORANGE,
			    Color.YELLOW,
			    Color.GREEN,
			    Color.BLUE,
			    new Color(128, 0, 128) // morado 
			};



		public DurabilityBrickManager(int rows, int cols, int bw, int bh, int ox, int oy) {
			this.rows = rows;
			this.cols = cols;
			grid = new DurabilityBrick[rows][cols];
			for (int r = 0; r < rows; r++)
				for (int c = 0; c < cols; c++) {
					int x = ox + c * (bw + 5), y = oy + r * (bh + 5);
					int dur = 2 + rand.nextInt(2); //varia entre durabilidad 2 y 3
					
//					 int colorIndex = rand.nextInt(colores.length); // índice aleatorio de colores
//				     Color colorAleatorio = colores[colorIndex]; //sirve pa q aparezcan mas colores iniciales supuestamente pero no salen
					 grid[r][c] = new DurabilityBrick(x, y, bw, bh, dur);
				}
		}

		//maneja la durabilidad de los ladrillos
		public int checkBallCollision(Ball ball) {
		    if (ball.isWaiting())
		        return 0;

		    for (DurabilityBrick[] row : grid) {
		        for (DurabilityBrick b : row) {
		            if (!b.isBroken() && ball.getBounds().intersects(b.getBounds())) {
		                b.hit();
		                ball.reverseY();
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
		
		public DurabilityBrick(int x, int y, int w, int h, int hits) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.hits = hits;
			if (hits == 3) {
		        this.color = Color.RED; //durabilidad 3
		        this.color = Color.CYAN;
		    } else if (hits == 2) {
		        this.color = Color.ORANGE; //durabiliad 2
		    } else {
		        this.color = Color.PINK;
		    }
		}

		public void hit() {
		    hits--;
		    if (hits <= 0) {
		        broken = true;
		    } else {
		        updateColor(); // cambiar color según golpes restantes
		    }
		}

		//actualiza el color segun el estado del bloque
		private void updateColor() {
		    if (hits == 2) {
		        color = Color.ORANGE;
		    } else if (hits == 1) {
		        color = Color.PINK;
		    }
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

