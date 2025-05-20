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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

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

	// movimiento pala
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

	// evitar el parpadeo
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

	// muestra ventana cuando se acaban las vidas
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

		// lleva al mismo nivel
		retry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				menu.dispose();
				BreakoutGame.restartNivelFacil();
			}
		});

		// lleva a menu
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

	// pulsar flechas para jugar
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

	// cuando se rompen todos los blques
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

		// acciones de los botones
		retryBtn.addActionListener(e -> {
			winMenu.dispose();
			BreakoutGame.restartNivelFacil();
		});

		nextBtn.addActionListener(e -> {
			winMenu.dispose();
			BreakoutGame.launchMediumLevel(); // ejecuta nivel medio
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
			@Override
			public void paint(Graphics g) {
				g.setColor(Color.BLACK); // Fondo negro sólido
				g.fillRect(0, 0, getWidth(), getHeight());
				super.paint(g);
			}
		};

		levelFrame.setResizable(false);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int w = screen.width;
		int h = screen.height;
		levelFrame.setSize(w, h);
		levelFrame.setUndecorated(true);
		levelFrame.setLayout(null);
		levelFrame.setLocation((screen.width - w) / 2, (screen.height - h) / 2);

		// Medidas comunes
		int canvasW = w / 5;       // antes era w / 5
		int canvasH = h / 5;       // antes era h / 6
		int botonW = w / 5;        // antes era w / 6
		int botonH = h / 11;       // antes era h / 12
		int gapY = h / 14;         // más espacio entre filas

		// Coordenadas horizontales (centrado)
		int leftX = (w - (canvasW + 60 + botonW)) / 2;  // separador más grande
		
//		// ------- Fila 1: Fácil -------
//		Image imgFacil = Toolkit.getDefaultToolkit().getImage("resources/star1.png");
//		Canvas previewFacil = new Canvas() {
//			@Override
//			public void paint(Graphics g) {
//				int cw = getWidth(), ch = getHeight();
//				g.setColor(Color.BLACK);
//				g.fillRect(0, 0, cw, ch);
//				g.drawImage(imgFacil, 0, 0, cw, ch, this);
//			}
//		};
//		int y1 = h / 8;
//		previewFacil.setBounds(leftX, y1, canvasW, canvasH);
//		levelFrame.add(previewFacil);
//
//		BotonPersonalizado boton_facil = new BotonPersonalizado("resources/facil.png", botonW, botonH);
//		boton_facil.setBounds(leftX + canvasW + 40, y1 + (canvasH - botonH) / 2, botonW, botonH);
//		boton_facil.setAccion(() -> {
//			levelFrame.dispose();
//			BreakoutGame.launchGame();
//		});
//		levelFrame.add(boton_facil);
//
//		// ------- Fila 2: Intermedio -------
//		Image imgMedio = Toolkit.getDefaultToolkit().getImage("resources/star2.png");
//		Canvas previewMedio = new Canvas() {
//			@Override
//			public void paint(Graphics g) {
//				int cw = getWidth(), ch = getHeight();
//				g.setColor(Color.BLACK);
//				g.fillRect(0, 0, cw, ch);
//				g.drawImage(imgMedio, 0, 0, cw, ch, this);
//			}
//		};
//		int y2 = y1 + canvasH + gapY;
//		previewMedio.setBounds(leftX, y2, canvasW, canvasH);
//		levelFrame.add(previewMedio);
//
//		BotonPersonalizado boton_medio = new BotonPersonalizado("resources/medio.png", botonW, botonH);
//		boton_medio.setBounds(leftX + canvasW + 40, y2 + (canvasH - botonH) / 2, botonW, botonH);
//		boton_medio.setAccion(() -> {
//			levelFrame.dispose();
//			BreakoutGame.launchMediumLevel();
//		});
//		levelFrame.add(boton_medio);
//
//		// ------- Fila 3: Difícil -------
//		Image imgDificil = Toolkit.getDefaultToolkit().getImage("resources/star3.png");
//		Canvas previewDificil = new Canvas() {
//			@Override
//			public void paint(Graphics g) {
//				int cw = getWidth(), ch = getHeight();
//				g.setColor(Color.BLACK);
//				g.fillRect(0, 0, cw, ch);
//				g.drawImage(imgDificil, 0, 0, cw, ch, this);
//			}
//		};
//		int y3 = y2 + canvasH + gapY;
//		previewDificil.setBounds(leftX, y3, canvasW, canvasH);
//		levelFrame.add(previewDificil);
//
//		BotonPersonalizado boton_dificil = new BotonPersonalizado("resources/dificil.png", botonW, botonH);
//		boton_dificil.setBounds(leftX + canvasW + 40, y3 + (canvasH - botonH) / 2, botonW, botonH);
//		boton_dificil.setAccion(() -> {
//			levelFrame.dispose();
//			BreakoutGame.launchDificilLevel();
//		});
//		levelFrame.add(boton_dificil);

		// Cargar la fuente personalizada
        Font fuentePersonalizada = FuentePersonalizada.cargarFuente(48f);
		
		// ------- Fila 1: Fácil -------
		Image imgFacil = Toolkit.getDefaultToolkit().getImage("resources/star1.png");
		Canvas previewFacil = new Canvas() {
		    @Override
		    public void paint(Graphics g) {
		        int cw = getWidth(), ch = getHeight();
		        g.setColor(Color.BLACK);
		        g.fillRect(0, 0, cw, ch);
		        g.drawImage(imgFacil, 0, 0, cw, ch, this);
		    }
		};
		int y1 = h / 8;
		previewFacil.setBounds(leftX, y1, canvasW, canvasH);
		levelFrame.add(previewFacil);

		BotonPersonalizado boton_facil = new BotonPersonalizado("Facil", botonW, botonH);
		boton_facil.setColorFondo(Color.BLUE.darker());
		boton_facil.setBounds(leftX + canvasW + 40, y1 + (canvasH - botonH) / 2, botonW, botonH);
		boton_facil.setFont(fuentePersonalizada);
		boton_facil.setAccion(() -> {
		    levelFrame.dispose();
		    BreakoutGame.launchGame();
		});
		levelFrame.add(boton_facil);

		// ------- Fila 2: Intermedio -------
		Image imgMedio = Toolkit.getDefaultToolkit().getImage("resources/star2.png");
		Canvas previewMedio = new Canvas() {
		    @Override
		    public void paint(Graphics g) {
		        int cw = getWidth(), ch = getHeight();
		        g.setColor(Color.BLACK);
		        g.fillRect(0, 0, cw, ch);
		        g.drawImage(imgMedio, 0, 0, cw, ch, this);
		    }
		};
		int y2 = y1 + canvasH + gapY;
		previewMedio.setBounds(leftX, y2, canvasW, canvasH);
		levelFrame.add(previewMedio);

		BotonPersonalizado boton_medio = new BotonPersonalizado("Intermedio", botonW, botonH);
		boton_medio.setColorFondo(Color.YELLOW.darker());
		boton_medio.setBounds(leftX + canvasW + 40, y2 + (canvasH - botonH) / 2, botonW, botonH);
		boton_medio.setFont(fuentePersonalizada.deriveFont(35f)); 
		boton_medio.setAccion(() -> {
		    levelFrame.dispose();
		    BreakoutGame.launchMediumLevel();
		});
		levelFrame.add(boton_medio);

		// ------- Fila 3: Difícil -------
		Image imgDificil = Toolkit.getDefaultToolkit().getImage("resources/star3.png");
		Canvas previewDificil = new Canvas() {
		    @Override
		    public void paint(Graphics g) {
		        int cw = getWidth(), ch = getHeight();
		        g.setColor(Color.BLACK);
		        g.fillRect(0, 0, cw, ch);
		        g.drawImage(imgDificil, 0, 0, cw, ch, this);
		    }
		};
		int y3 = y2 + canvasH + gapY;
		previewDificil.setBounds(leftX, y3, canvasW, canvasH);
		levelFrame.add(previewDificil);

		BotonPersonalizado boton_dificil = new BotonPersonalizado("Dificil", botonW, botonH);
		boton_dificil.setColorFondo(Color.RED.darker());
		boton_dificil.setBounds(leftX + canvasW + 40, y3 + (canvasH - botonH) / 2, botonW, botonH);
		boton_dificil.setFont(fuentePersonalizada);
		boton_dificil.setAccion(() -> {
		    levelFrame.dispose();
		    BreakoutGame.launchDificilLevel();
		});
		levelFrame.add(boton_dificil);
		
		// ------- Botón Volver -------
		int volverW = w / 20;
		int volverH = h / 20;

		// Creas el botón con un texto (puede ser vacío si solo quieres imagen)
		BotonPersonalizado boton_volver = new BotonPersonalizado("", volverW, volverH);

		// Le asignas la imagen que quieres mostrar
		boton_volver.setImagen("resources/volver.png");

		// Ajustas la posición y tamaño del botón
		boton_volver.setBounds(w - volverW - 40, 40, volverW, volverH);

		// Acción del botón
		boton_volver.setAccion(() -> {
		    levelFrame.dispose();
		});

		levelFrame.add(boton_volver);
		levelFrame.setVisible(true);


	}
/*BotonPersonalizado boton = new BotonPersonalizado("Click aquí", 200, 60);
boton.setColorFondo(Color.BLUE);
boton.setImagen("ruta/a/tu/imagen.png");  // <- carga la imagen
*/

	// Clase interna para botones con fondo negro sólido y imagen encima
//	static class BotonPersonalizado extends Canvas {
//		private Image img;
//		private int ancho, alto;
//		private Runnable accion;
//
//		public BotonPersonalizado(String rutaImagen, int ancho, int alto) {
//			this.ancho = ancho;
//			this.alto = alto;
//			try {
//				img = Toolkit.getDefaultToolkit().getImage(rutaImagen);
//				Toolkit.getDefaultToolkit().prepareImage(img, -1, -1, null);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			setSize(ancho, alto);
//			addMouseListener(new java.awt.event.MouseAdapter() {
//				@Override
//				public void mouseClicked(java.awt.event.MouseEvent e) {
//					if (accion != null) {
//						accion.run();
//					}
//				}
//			});
//		}
//
//		public void setAccion(Runnable accion) {
//			this.accion = accion;
//		}
//
//		@Override
//		public void paint(Graphics g) {
//			// Fondo negro sólido
//			g.setColor(Color.BLACK);
//			g.fillRect(0, 0, getWidth(), getHeight());
//
//			// Dibuja la imagen escalada
//			if (img != null) {
//				g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
//			}
//		}
//	}
}
