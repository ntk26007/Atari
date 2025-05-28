package atari;

import java.awt.Button;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
	private boolean paused = false;
	
	public GameCanvas(int width, int height) {
		this.width = width;
		this.height = height;
		setPreferredSize(new Dimension(width, height));
		addKeyListener(this);
		setFocusable(true);
		requestFocus();
		initGame();
		
		AudioPlayer.detenerAudio();
	    AudioPlayer.reproducirAudio("Resources/nivelFacil.wav"); 
	}

	// Resetea estado para reiniciar partida, reinicia todos valores
	public void resetGame() {
        initGame();
        running = true;
        leftPressed = false;
        rightPressed = false; //para que no se mueva la paleta sola cada vez q reinicias
        paused = false;
        requestFocus();
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

	
	//movimiento de la pelota
	@Override
	public void run() {
		running = true;
		long lastTime = System.nanoTime(); //Guarda el tiempo actual en nanosegundos como referencia inicial
		double nsPerUpdate = 1e9 / 60.0; //Calcula cuántos nanosegundos deben pasar entre cada actualización lógica del juego si queremos 60 actualizaciones por segundo 
		//(1 segundo = 1e9 nanosegundos).
		double delta = 0; //Variable que acumula el tiempo transcurrido.
		while (running) {
			long now = System.nanoTime(); //Obtiene el tiempo actual en cada iteración del bucle, mientras se ejecuta el juego
			delta += (now - lastTime) / nsPerUpdate; //Calcula cuánto tiempo ha pasado desde la última vez (now - lastTime) 
			//y lo añade a delta, ajustado para que coincida con la actualizacion


			lastTime = now;
			while (delta >= 1) { //Si ha pasado suficiente tiempo (es decir, delta >= 1), 
				//se llama al update() que actualiza la lógica del juego 
				update();
				delta--; // ya se ha hecho una actualización
			}
			render(); //dibuja en la pantalla
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) { //Intenta pausar el hilo por 2 milisegundos para evitar que el CPU trabaje de mas
			}
		}
	}

	// movimiento pala
	private void update() {
		 if (paused) return; // Si está en pausa, no actualiza
		boolean wasWaiting = ball.isWaiting();
		if (leftPressed)
			paddle.moveLeft();
		if (rightPressed)
			paddle.moveRight();
		if (!ball.isWaiting()) {
			ball.update();
			ball.checkWallCollision();
			ball.checkPaddleCollision(paddle);
			if (bricks.checkBallCollision(ball)) {
				AudioPlayer.reproducirEfecto("Resources/bloque.wav");
				score++;
			}
			
			if (bricks.isEmpty()) {
				running = false; //se para
				winMenu(); // Mostrar el menú de victoria
			}

		} else {
			ball.update();
		}
		if (!wasWaiting && ball.isWaiting()) { //cuando te caes
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

		// Mostrar mensaje de pausa si está pausado
	    if (paused) {
	    	Font fuentePersonalizada = FuentePersonalizada.cargarFuente(48f);
	        g2.setFont(fuentePersonalizada);
	        String texto = "PAUSA";
	        int x = (width - g2.getFontMetrics().stringWidth(texto)) / 2;
	        int y = height / 2;

	        // Sombra para visibilidad
	        g2.setColor(Color.BLACK);
	        g2.drawString(texto, x + 2, y + 2);
	        g2.setColor(Color.RED);
	        g2.drawString(texto, x, y);
	    }
	    
		g2.dispose();
		bs.show();
	}

	// muestra ventana cuando se acaban las vidas
	private void showGameOverMenu() {
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
	

		// lleva al mismo nivel
		retry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				menu.dispose();
				AudioPlayer.detenerAudio(); // Detener cualquier sonido
		        AudioPlayer.reproducirAudio("Resources/nivelFacil.wav");
				BreakoutGame.restartNivelFacil();
			}
		});

		// lleva a menu
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
		
		//sonido en concreto para perder
		AudioPlayer.detenerAudio();
		AudioPlayer.reproducirAudioUnaVez("Resources/gameOver.wav");


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
		if (e.getKeyCode() == KeyEvent.VK_P) 
			paused = !paused;
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	// cuando se rompen todos los blques
	private void winMenu() {
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

	    // Acciones de los botones
	    retryBtn.addActionListener(e -> {
	        winMenu.dispose();
	        BreakoutGame.restartNivelFacil();
	    });

	    nextBtn.addActionListener(e -> {
	        winMenu.dispose();
	        AudioPlayer.detenerAudio(); 
	        AudioPlayer.reproducirAudio("Resources/facil.wav");
	        BreakoutGame.launchMediumLevel();
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

	    // Sonido de victoria
	    AudioPlayer.detenerAudio();
	    AudioPlayer.reproducirAudioUnaVez("Resources/win.wav"); 

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
		
		//ponemos esta condicion para que la musica del menu se continue al mismo tiempo cuando pasamos al panel de los niveles
		if (!AudioPlayer.isPlaying()) { //el metodo comprueba que la musica en concreto se siga ejecutando
		    AudioPlayer.reproducirAudio("Assets/facil.wav");
		}

		
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

		BotonPersonalizado botonFacil = new BotonPersonalizado("Facil", botonW, botonH);
		botonFacil.setColorFondo(Color.BLUE.darker());
		botonFacil.setBounds(leftX + canvasW + 40, y1 + (canvasH - botonH) / 2, botonW, botonH);
		botonFacil.setFont(fuentePersonalizada);
		
		botonFacil.setAccion(() -> {
		    levelFrame.dispose();
		    BreakoutGame.launchGame();
		});
		levelFrame.add(botonFacil);

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

		BotonPersonalizado botonMedio = new BotonPersonalizado("Intermedio", botonW, botonH);
		botonMedio.setColorFondo(Color.YELLOW.darker());
		botonMedio.setBounds(leftX + canvasW + 40, y2 + (canvasH - botonH) / 2, botonW, botonH);
		botonMedio.setFont(fuentePersonalizada.deriveFont(35f)); 
		
		botonMedio.setAccion(() -> {
		    levelFrame.dispose();
		    BreakoutGame.launchMediumLevel();
		});
		levelFrame.add(botonMedio);

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

		BotonPersonalizado botonDificil = new BotonPersonalizado("Dificil", botonW, botonH);
		botonDificil.setColorFondo(Color.RED.darker());
		botonDificil.setBounds(leftX + canvasW + 40, y3 + (canvasH - botonH) / 2, botonW, botonH);
		botonDificil.setFont(fuentePersonalizada);
		
		botonDificil.setAccion(() -> {
		    levelFrame.dispose();
		    BreakoutGame.launchDificilLevel(); 
		});
		levelFrame.add(botonDificil);
		
		// ------- Botón Volver -------
		int volverW = w / 20;
		int volverH = h / 20;

		// Creas el botón con un texto (puede ser vacío si solo quieres imagen)
		BotonVolver botonVolver = new BotonVolver("Volver");
		
		// Ajustas la posición y tamaño del botón
		botonVolver.setBounds(w - volverW - 40, 40, volverW, volverH);

		// Acción del botón
		botonVolver.setAccion(() -> {
		    levelFrame.dispose();
		});
		
		//BotonVolver.agregarEfectoSonido(botonVolver); no es necesario ya que implementamos una clase en concreto 
		agregarEfectoSonido(botonDificil);
		agregarEfectoSonido(botonMedio);
		agregarEfectoSonido(botonFacil);

		levelFrame.add(botonVolver);
		botonVolver.repaint(); 
		levelFrame.setVisible(true);


	}
	
	
	 //efecto de sonido al pulsar y pasar el raton
    private static void agregarEfectoSonido(BotonPersonalizado boton) {
        boton.addMouseListener(new MouseAdapter() { 
            @Override
            public void mouseEntered(MouseEvent e) { //pasar el ratón
                AudioPlayer.reproducirEfecto("Resources/hover.wav");
            }

            @Override
            public void mousePressed(MouseEvent e) {
                AudioPlayer.reproducirEfecto("Resources/click.wav");
            }
        });
    }
}