package atari;

import java.awt.Button;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import atari.NivelMedio.DurabilityBrick;
import atari.NivelMedio.DurabilityBrickManager;

public class NivelExtra extends Canvas implements Runnable, KeyListener {
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



	public NivelExtra(int width, int height) {
		this.width = width;
		this.height = height;
		setPreferredSize(new Dimension(width, height));
		addKeyListener(this);
		setFocusable(true);
		requestFocus();
		initGame();
	}
	
	// Resetea estado para reiniciar el nivel medio
		public void resetGame() {
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
		bricks = new DurabilityBrickManager();
		
		int centerX = 300;
		int centerY = 150;
		int brickWidth = 40;
		int brickHeight = 20;
		int durability = 3;

		//brickManager.addStarShape(centerX, centerY, brickWidth, brickHeight, durability);
		
		//puntuacion
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
		long elapsed = System.currentTimeMillis() - startTime;
		if (elapsed >= TIME_LIMIT) {
		    running = false;
		    showOverTime(); //cuando se acaba el tiempo salta la pantalla de perder
		    return;
		}

		boolean w = ball.isWaiting();
		if (!w) {
			if (leftPressed)
				paddle.moveLeft();
			if (rightPressed)
				paddle.moveRight();
			ball.update();
			ball.checkWallCollision();
			ball.checkPaddleCollision(paddle);
			
			//int puntos = bricks.checkBallCollision(ball); //durabilidad bloques
			//score += puntos;

//			if (bricks.isEmpty()) {
//				running = false;
//				winMenu3();
//			}
//		} else {
//			ball.update();
//		}
//		if (!w && ball.isWaiting()) {
//			lives--;
//			if (lives <= 0) {
//				running = false;
//				showGameOverMenu3();
//			} else {
//				ball.resetPosition(paddle.getX() + paddle.getWidth() / 2, paddle.getY() - ball.getDiameter());
//			}
		}
	}
	
	//cuando se acaba el tiempo salta otra ventana
	private void showOverTime() {
	    Frame timeOver = new Frame("¡Tiempo agotado!") {
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

	    timeOver.setSize(400, 250);
	    timeOver.setLayout(null);
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
	            gameThread = new Thread(NivelExtra.this);
	            gameThread.start();
	        }
	        timeOver.dispose();
	    });

	    mainMenu.addActionListener(e -> {
	        timeOver.dispose();
	        BreakoutGame.returnToMenu();
	    });

	    timeOver.setVisible(true);
	}

	

	//Perder
	private void showGameOverMenu3() {
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

		
		retry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetGame();
				requestFocus();

				if (gameThread == null || !gameThread.isAlive()) {
				    gameThread = new Thread(NivelExtra.this);
				    gameThread.start();
				}

				menu.dispose();
				//BreakoutGame.restartNivelDificil();
			}
		});

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

	
	//Ganar
	private void winMenu3() {
		Frame winMenu = new Frame("\u00a1Nivel Completado!") {
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
		
		Button nextBtn = new Button("Nivel Extra");
		nextBtn.setBounds(bx, baseY + 60, bw, bh);
		winMenu.add(nextBtn);

		Button menuBtn = new Button("Menú");
		menuBtn.setBounds(bx, baseY + 120, bw, bh);
		winMenu.add(menuBtn);

		retryBtn.addActionListener(e -> {
			winMenu.dispose();
			//BreakoutGame.restartNivelDificil();
		});
		
		nextBtn.addActionListener(e -> {
			winMenu.dispose();
			//BreakoutGame.launchExtraLevel();
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
		g.dispose();
		bs.show();
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_LEFT)
			leftPressed = true;
		if (e.getKeyCode() == KeyEvent.VK_RIGHT)
			rightPressed = true;
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
	public class DurabilityBrickManager {
	    private List<DurabilityBrick> bricks;

	    public DurabilityBrickManager() {
	        bricks = new ArrayList<>();
	    }

	    public void addBrick(DurabilityBrick brick) {
	        bricks.add(brick);
	    }

	    public void update(Ball ball, ScoreManager scoreManager) {
	        for (DurabilityBrick brick : bricks) {
	            if (!brick.isDestroyed() && brick.getBounds().intersects(ball.getBounds())) {
	                brick.hit();
	                ball.reverseY();
	                scoreManager.increment(100);
	                break;
	            }
	        }
	    }

	    public void draw(Graphics g) {
	        for (DurabilityBrick brick : bricks) {
	            brick.draw(g);
	        }
	    }

	    public boolean areAllBricksDestroyed() {
	        for (DurabilityBrick brick : bricks) {
	            if (!brick.isDestroyed()) return false;
	        }
	        return true;
	    }

	    public void reset() {
	        for (DurabilityBrick brick : bricks) {
	            brick.reset();
	        }
	    }

	    //  Dibuja una estrella en base a coordenadas y tamaño
	    public void addStarShape(int centerX, int centerY, int brickWidth, int brickHeight, int durability) {
	        int[][] starCoords = {
	            {0, -2}, {1, -1}, {2, -1}, {1, 0}, {2, 1},
	            {0, 1}, {-2, 1}, {-1, 0}, {-2, -1}, {-1, -1}
	        };

	        for (int[] coord : starCoords) {
	            int x = centerX + coord[0] * (brickWidth + 2);
	            int y = centerY + coord[1] * (brickHeight + 2);
	            bricks.add(new DurabilityBrick(x, y, brickWidth, brickHeight, durability));
	        }

	        // Centro de la estrella
	        bricks.add(new DurabilityBrick(centerX, centerY, brickWidth, brickHeight, durability));
	    }
	}

	//la otra clase
	public class DurabilityBrick {
	    private int x, y, width, height;
	    private int durability;
	    private final int initialDurability;

	    public DurabilityBrick(int x, int y, int width, int height, int durability) {
	        this.x = x;
	        this.y = y;
	        this.width = width;
	        this.height = height;
	        this.durability = durability;
	        this.initialDurability = durability;
	    }

	    public void draw(Graphics g) {
	        if (!isDestroyed()) {
	            if (durability == 3) g.setColor(Color.RED);
	            else if (durability == 2) g.setColor(Color.ORANGE);
	            else g.setColor(Color.YELLOW);

	            g.fillRect(x, y, width, height);
	            g.setColor(Color.BLACK);
	            g.drawRect(x, y, width, height);
	        }
	    }

	    public void hit() {
	        if (durability > 0) durability--;
	    }

	    public boolean isDestroyed() {
	        return durability <= 0;
	    }

	    public Rectangle getBounds() {
	        return new Rectangle(x, y, width, height);
	    }

	    public void reset() {
	        durability = initialDurability;
	    }
	}
}


