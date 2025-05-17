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


	public NivelMedio(int width, int height) {
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
		ball = new Ball(width / 2, height / 2, 10, 4, 4, width, height);
		
		//bloques
		int rows = 8, cols = 12;
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
		boolean w = ball.isWaiting();
		if (!w) {
			if (leftPressed)
				paddle.moveLeft();
			if (rightPressed)
				paddle.moveRight();
			ball.update();
			ball.checkWallCollision();
			ball.checkPaddleCollision(paddle);
			
			int puntos = bricks.checkBallCollision(ball); //durabilidad bloques
			score += puntos;

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

		//reintentar, esto es lo ultimo q me dijo el chat pero creo que debe ser solo con dos lineas
		// osea esto = menu.dispose();
		//			   BreakoutGame.restartNivelMedio();
		retry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetGame();
				requestFocus();

				if (gameThread == null || !gameThread.isAlive()) {
				    gameThread = new Thread(NivelMedio.this);
				    gameThread.start();
				}

				menu.dispose();
				BreakoutGame.restartNivelMedio();
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
	private void winMenu2() {
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

		Button nextBtn = new Button("Siguiente Nivel");
		nextBtn.setBounds(bx, baseY + 60, bw, bh);
		winMenu.add(nextBtn);

		Button menuBtn = new Button("Menú");
		menuBtn.setBounds(bx, baseY + 120, bw, bh);
		winMenu.add(menuBtn);

		retryBtn.addActionListener(e -> {
			winMenu.dispose();
			BreakoutGame.restartNivelMedio();
		});

		nextBtn.addActionListener(e -> {
			winMenu.dispose();
			//BreakoutGame.launchDificilLevel();
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
					int dur = rand.nextBoolean() ? 2 : 1;
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
			this.color = hits == 2 ? Color.PINK : Color.CYAN;
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
