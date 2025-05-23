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
    private boolean paused = false; // Añadido para la funcionalidad de pausa
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
        
        AudioPlayer.detenerAudio();
	    AudioPlayer.reproducirAudio("Resources/facil.wav");
    }

    // Resetea estado para reiniciar el nivel
    public void resetGame() {
        initGame();
        lives = 3; // Resetear vidas al reiniciar el juego
        score = 0; // Resetear puntuación al reiniciar el juego
        paused = false; // Asegurarse de que no esté pausado al reiniciar
        running = true; // Asegurarse de que el juego esté corriendo al reiniciar
    }

    private void initGame() {
        // Pala + pelota
        paddle = new Paddle((width - 100) / 2, height - 50, 100, 10, width);
        ball = new Ball(width / 2, height - 280, 10, 4, 4, width, height);

        // Bloques - Configuración para la forma de estrella
        bricks = new DurabilityBrickManager();

        // Coordenadas y tamaño 
        int centerX = width / 2; // Centro horizontal de la pantalla
        int centerY = height / 4; // Un cuarto de la altura para que esté arriba
        int brickWidth = 60; 
        int brickHeight = 30; 
        int durability = 3; // Durabilidad para los ladrillos de la estrella

        // Llama al método para añadir la forma de estrella
        bricks.addStarShape(centerX, centerY, brickWidth, brickHeight, durability);

        // Puntuación y vidas iniciales (se reinician en resetGame)
        // lives = 2; // Comentado, se maneja en resetGame
        // score = 0; // Comentado, se maneja en resetGame
    }

    @Override
    public void run() {
        running = true;
        long last = System.nanoTime();
        double nsPerUpdate = 1e9 / 60.0; // 60 FPS
        double delta = 0;
        startTime = System.currentTimeMillis(); // para el temporizador

        while (running) {
            long now = System.nanoTime();
            delta += (now - last) / nsPerUpdate;
            last = now;

            // Lógica de pausa: si está pausado, espera hasta que se reanude
            synchronized (this) {
                while (paused && running) { // Solo espera si está pausado Y el juego sigue corriendo
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // Reestablece el estado de interrupción
                        running = false; // Sale del bucle si el hilo es interrumpido
                        System.out.println("Juego interrumpido durante la pausa.");
                        return; // Salir del método run
                    }
                }
            }

            while (delta >= 1) {
                update();
                delta--;
            }
            render();
            try {
                // Pequeña pausa para evitar un uso excesivo de la CPU si delta es muy pequeño
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt(); // Reestablece el estado de interrupción
                running = false; // Sale del bucle si el hilo es interrumpido
                System.out.println("Juego interrumpido durante el sleep.");
                return; // Salir del método run
            }
        }
    }

    private void update() {
    	if (paused) return; // Si está en pausa, no actualiza
        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed >= TIME_LIMIT) {
            running = false;
            showOverTime(); // cuando se acaba el tiempo salta la pantalla de perder
            return;
        }

        boolean w = ball.isWaiting();
        if (leftPressed)
    		paddle.moveLeft();
    	if (rightPressed)
    		paddle.moveRight();
        if (!w) {
//            if (leftPressed)
//                paddle.moveLeft();
//            if (rightPressed)
//                paddle.moveRight();
            ball.update();
            ball.checkWallCollision();
            ball.checkPaddleCollision(paddle);

            int puntos = bricks.checkBallCollision(ball); // durabilidad bloques
            score += puntos;
            
            if (puntos > 0) {
	            AudioPlayer.reproducirEfecto("Resources/bloque.wav");
	        }

            if (bricks.isEmpty()) {
                running = false;
                winMenu4();
            }
        } else {
            // Si la bola está esperando (después de perder una vida), sigue actualizando su posición
            // para que se mueva con la pala si es necesario.
            ball.update();
        }

        // Si la bola estaba en juego y ahora está esperando (cayó al suelo)
        if (!w && ball.isWaiting()) {
            lives--;
            if (lives <= 0) {
                running = false;
                showGameOverMenu4();
            } else {
                // Reinicia la posición de la bola sobre la pala
                ball.resetPosition(paddle.getX() + paddle.getWidth() / 2, paddle.getY() - ball.getDiameter());
            }
        }
    }

    // Método para pausar el juego
    public synchronized void pauseGame() {
        paused = true;
    }

    // Método para reanudar el juego
    public synchronized void resumeGame() {
        if (paused) {
            paused = false;
            notify(); // Notifica al hilo del juego para que continúe
        }
    }

    // cuando se acaba el tiempo salta otra ventana
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
            timeOver.dispose();
            resetGame(); 
            requestFocus();
            // Asegurar que el hilo del juego se inicie si no está corriendo
            if (gameThread == null || !gameThread.isAlive()) {
                gameThread = new Thread(NivelExtra.this);
                gameThread.start();
            }
        });

        mainMenu.addActionListener(e -> {
            timeOver.dispose();
            if (gameThread != null) {
                gameThread.interrupt(); // Intenta interrumpir el hilo del juego
                try {
                    gameThread.join(); // Espera a que termine
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                gameThread = null; // Limpia la referencia al hilo
            }
            
            AudioPlayer.detenerAudio(); 
	        AudioPlayer.reproducirAudio("Resources/menu.wav");
            BreakoutGame.returnToMenu();
        });

        timeOver.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                timeOver.dispose();
                // Si el usuario cierra la ventana de Tiempo agotado, el juego debería volver al menú principal
                if (gameThread != null) {
                    gameThread.interrupt();
                    try {
                        gameThread.join();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    gameThread = null;
                }
                BreakoutGame.returnToMenu();
            }
        });
        
        AudioPlayer.detenerAudio();
		AudioPlayer.reproducirAudioUnaVez("Resources/gameOver.wav");

        timeOver.setVisible(true);
    }

    // Perder por vidas
    private void showGameOverMenu4() {
        Frame menu = new Frame("Game Over") {
            private Image background = Toolkit.getDefaultToolkit().getImage("resources/sombra1 (1).jpg");

            {
                Toolkit.getDefaultToolkit().prepareImage(background, -1, -1, null);
            }

            Font fuentePersonalizada = FuentePersonalizada.cargarFuente(48f);
            @Override
            public void paint(Graphics g) {
                g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
                g.setFont(fuentePersonalizada); // Fuente del texto
                super.paint(g);
            }
        };

        menu.setResizable(false);
        int w = 400, h = 250;
        menu.setSize(w, h);
        menu.setLayout(null);
		menu.setUndecorated(true);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        menu.setLocation((screen.width - w) / 2, (screen.height - h) / 2);

		Button retry = new Button("Reintentar");
		retry.setBackground(Color.GREEN);
		retry.setBounds(60, 120, 120, 40);
		menu.add(retry);

		Button mainMenu = new Button("Menú");
		mainMenu.setBackground(Color.CYAN);
		mainMenu.setBounds(220, 120, 120, 40);
		menu.add(mainMenu);

        retry.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                menu.dispose();
                resetGame(); 
                requestFocus();
                // Asegura que el hilo del juego se inicie si no está corriendo
                if (gameThread == null || !gameThread.isAlive()) {
                    gameThread = new Thread(NivelExtra.this);
                    gameThread.start();
                }
            }
        });

        mainMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                menu.dispose();
                if (gameThread != null) {
                    gameThread.interrupt();
                    try {
                        gameThread.join();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    gameThread = null;
                }
                
                AudioPlayer.detenerAudio(); 
    	        AudioPlayer.reproducirAudio("Resources/menu.wav");
                BreakoutGame.returnToMenu();
            }
        });

        menu.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                menu.dispose();
                if (gameThread != null) {
                    gameThread.interrupt();
                    try {
                        gameThread.join();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    gameThread = null;
                }
                BreakoutGame.returnToMenu();
            }
        });
        
        AudioPlayer.detenerAudio();
		AudioPlayer.reproducirAudioUnaVez("Resources/gameOver.wav");

        menu.setVisible(true);
    }

    // Ganar
    private void winMenu4() {
        Frame winMenu = new Frame("¡Nivel Completado!") {
            private Image bgImage = Toolkit.getDefaultToolkit().getImage("resources/sombra1 (1).jpg");
            private Image staticImage; // Objeto Image de AWT para la foto estática

            {
                Toolkit.getDefaultToolkit().prepareImage(bgImage, -1, -1, null);
                // Carga la imagen estática usando Toolkit de AWT
                staticImage = Toolkit.getDefaultToolkit().getImage("resources/javi.png"); 
                Toolkit.getDefaultToolkit().prepareImage(staticImage, -1, -1, null); 
            }

            @Override
            public void paint(Graphics g) {
                Dimension size = getSize();
                g.drawImage(bgImage, 0, 0, size.width, size.height, this);
                super.paint(g); // Llama a super.paint para asegurar el dibujo correcto

                // Dibuja la imagen estática proporcionalmente y centrada
                if (staticImage != null) {
                    int menuWidth = getWidth();
                    int menuHeight = getHeight();

                    // Dimensiones objetivo para la imagen (por ejemplo, 80% del ancho, 40% del alto)
                    int imageTargetWidth = (int) (menuWidth * 0.8);
                    int imageTargetHeight = (int) (menuHeight * 0.8);

                    // Asegura que las dimensiones sean positivas
                    if (imageTargetWidth <= 0) imageTargetWidth = 1;
                    if (imageTargetHeight <= 0) imageTargetHeight = 1;

                    // Calcula las dimensiones reales de la imagen manteniendo la relación de aspecto
                    int originalImageWidth = staticImage.getWidth(this);
                    int originalImageHeight = staticImage.getHeight(this);

                    int scaledWidth = imageTargetWidth;
                    int scaledHeight = imageTargetHeight;

                    if (originalImageWidth > 0 && originalImageHeight > 0) {
                        double aspectRatio = (double) originalImageWidth / originalImageHeight;
                        if ((double) imageTargetWidth / imageTargetHeight > aspectRatio) {
                            scaledWidth = (int) (imageTargetHeight * aspectRatio);
                            scaledHeight = imageTargetHeight;
                        } else {
                            scaledWidth = imageTargetWidth;
                            scaledHeight = (int) (imageTargetWidth / aspectRatio);
                        }
                    }

                    // Posiciona la imagen en el centro de la ventana
                    int imageX = (menuWidth - scaledWidth) / 2;
                    int imageY = (menuHeight - scaledHeight) / 2; // Centrado verticalmente

                    g.drawImage(staticImage, imageX, imageY, scaledWidth, scaledHeight, this);
                }
            }
        };

        Font fuentePersonalizada = FuentePersonalizada.cargarFuente(18f);
        winMenu.setResizable(false);
        int w = 500, h = 450;
        winMenu.setSize(w, h);
        winMenu.setLayout(null); 
		winMenu.setUndecorated(true);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        winMenu.setLocation((screen.width - w) / 2, (screen.height - h) / 2);

        int bw = 200, bh = 60, bx = (w - bw) / 2;
        // Posiciona el botón en la parte inferior de la ventana con un padding
        int paddingFromBottom = 30; // Margen desde la parte inferior
        int baseY = h - bh - paddingFromBottom;

        Button menuBtn = new Button("Volver al menú");
        menuBtn.setBackground(Color.red);
        menuBtn.setBounds(bx, baseY, bw, bh);
        menuBtn.setFont(fuentePersonalizada); // Fuente del texto
        winMenu.add(menuBtn);

        menuBtn.addActionListener(e -> {
            winMenu.dispose();
            if (gameThread != null) {
                gameThread.interrupt();
                try {
                    gameThread.join();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                gameThread = null;
            }
            
            AudioPlayer.detenerAudio(); 
	        AudioPlayer.reproducirAudio("Resources/menu.wav");
            BreakoutGame.returnToMenu();
        });

        winMenu.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                winMenu.dispose();
                if (gameThread != null) {
                    gameThread.interrupt();
                    try {
                        gameThread.join();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    gameThread = null;
                }
                BreakoutGame.returnToMenu();
            }
        });

    	AudioPlayer.detenerAudio();
	    AudioPlayer.reproducirAudioUnaVez("Resources/finalWin.wav");
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

        // para el temporizador, lo dibuja en la pantalla
        long remainingTime = Math.max(0, TIME_LIMIT - (System.currentTimeMillis() - startTime));
        long seconds = remainingTime / 1000;
        long minutes = seconds / 60;
        seconds %= 60;
        String timeString = String.format("%02d:%02d", minutes, seconds);
        g.drawString("Tiempo: " + timeString, width / 2 - 60, 24);


        paddle.draw(g);
        ball.draw(g);
        bricks.draw(g); // Dibuja todos los ladrillos, incluida la estrella
        g.dispose();
        bs.show();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT)
            leftPressed = true;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT)
            rightPressed = true;
        if (e.getKeyCode() == KeyEvent.VK_P) {
            if (paused) {
                resumeGame();
            } else {
                pauseGame();
            }
        }
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

        public int checkBallCollision(Ball ball) {
            // Iterar sobre una copia para evitar ConcurrentModificationException si se elimina un ladrillo
            for (int i = 0; i < bricks.size(); i++) {
                DurabilityBrick brick = bricks.get(i);
                if (!brick.isDestroyed() && brick.getBounds().intersects(ball.getBounds())) {
                    brick.hit();
                    ball.reverseY(); // La bola siempre rebota verticalmente al golpear un ladrillo
                    // Si el ladrillo se destruye, se puede añadir lógica adicional aquí
                    // Por ejemplo, eliminarlo de la lista si no se necesita más
                    // if (brick.isDestroyed()) {
                    //     // bricks.remove(i); // Si deseas eliminar el ladrillo de la lista al ser destruido
                    //     // i--; // Ajustar el índice si se elimina
                    // }
                    return 100; // puntos por cada golpe
                }
            }
            return 0; // no se golpeó ningún ladrillo
        }

        public boolean isEmpty() {
            for (DurabilityBrick brick : bricks) {
                if (!brick.isDestroyed()) {
                    return false;
                }
            }
            return true;
        }

        // Se ha eliminado el método 'update' que tomaba ScoreManager, ya que NivelExtra gestiona la puntuación directamente.

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

        // Dibuja una estrella en base a coordenadas y tamaño
        public void addStarShape(int centerX, int centerY, int brickWidth, int brickHeight, int durability) {
            // Coordenadas relativas para formar una estrella de 5 puntas
            // Ajusta estos valores para cambiar la forma o el tamaño de la estrella
            int[][] starCoords = {
                    {0, -2}, // Punta superior
                    {1, -1}, {2, -1}, // Brazo superior derecho
                    {1, 0}, {2, 1}, // Brazo medio derecho
                    {0, 1}, // Base
                    {-2, 1}, {-1, 0}, // Brazo medio izquierdo
                    {-2, -1}, {-1, -1} // Brazo superior izquierdo
            };

            // Factor de escala para la estrella (ajusta según el tamaño deseado)
            int scaleFactor = 3; // Increased scale factor for the overall star shape

            for (int[] coord : starCoords) {
                int x = centerX + coord[0] * (brickWidth / 2 + 2) * scaleFactor;
                int y = centerY + coord[1] * (brickHeight / 2 + 2) * scaleFactor;
                bricks.add(new DurabilityBrick(x, y, brickWidth, brickHeight, durability));
            }

            // Centro de la estrella (opcional, para rellenar el centro)
            bricks.add(new DurabilityBrick(centerX, centerY, brickWidth, brickHeight, durability));
        }
    }

    // la otra clase
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
                // Colores basados en la durabilidad
                if (durability == 3) g.setColor(Color.RED);
                else if (durability == 2) g.setColor(Color.ORANGE);
                else g.setColor(Color.YELLOW);

                g.fillRect(x, y, width, height);
                g.setColor(Color.BLACK); // Borde negro
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