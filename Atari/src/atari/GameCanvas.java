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
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
	        
	        // Configuración dinámica de ladrillos para pantalla completa
	        int cols = 12;                            // número de columnas
	        int rows = 6;                             // número de filas
	        int spacing = 5;                          // espacio base entre ladrillos
	        int sideMargin = width / 20;              // margen lateral adicional (5% pantalla)
	        int totalSpacingX = (cols + 1) * spacing + sideMargin * 2;
	        int brickWidth = (width - totalSpacingX) / cols;
	        int brickHeight = height / 25;            // proporción de altura pantalla
	        int offsetX = sideMargin + spacing;       // margen izquierdo
	        int offsetY = spacing * 9;                // margen superior mayor para vidas/puntuación
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
	            while (delta >= 1) { update(); delta--; }
	            render();
	            try { Thread.sleep(2); } catch (InterruptedException e) {}
	        }
	    }

	    private void update() {
	        boolean wasWaiting = ball.isWaiting();
	        if (!ball.isWaiting()) {
	            if (leftPressed) paddle.moveLeft();
	            if (rightPressed) paddle.moveRight();
	            ball.update();
	            ball.checkWallCollision();
	            ball.checkPaddleCollision(paddle);
	            if (bricks.checkBallCollision(ball)) score++;
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

	    private void render() {
	        BufferStrategy bs = getBufferStrategy();
	        if (bs == null) { createBufferStrategy(3); return; }
	        Graphics2D g2 = (Graphics2D) bs.getDrawGraphics();

	        // Fondo
	        g2.setColor(Color.BLACK);
	        g2.fillRect(0, 0, width, height);

	        // Vidas y puntuación con fuente un poco mayor
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


	    private void showGameOverMenu() {
	        Frame menu = new Frame();
	        menu.setUndecorated(true);
	        menu.setSize(300, 150);
	        menu.setLayout(null);
	        menu.setLocation((width - 300) / 2, (height - 150) / 2);

	        Button retry = new Button("Reintentar");
	        retry.setBounds(50, 50, 80, 30);
	        menu.add(retry);

	        Button mainMenu = new Button("Menú");
	        mainMenu.setBounds(170, 50, 80, 30);
	        menu.add(mainMenu);

	        retry.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	                menu.dispose();
	                BreakoutGame.restartGame();
	            }
	        });
	        mainMenu.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	                menu.dispose();
	                BreakoutGame.returnToMenu();
	            }
	        });

	        menu.setVisible(true);
	    }

	    @Override public void keyPressed(KeyEvent e) {
	        if (e.getKeyCode() == KeyEvent.VK_LEFT) leftPressed = true;
	        if (e.getKeyCode() == KeyEvent.VK_RIGHT) rightPressed = true;
	    }
	    @Override public void keyReleased(KeyEvent e) {
	        if (e.getKeyCode() == KeyEvent.VK_LEFT) leftPressed = false;
	        if (e.getKeyCode() == KeyEvent.VK_RIGHT) rightPressed = false;
	    }
	    @Override public void keyTyped(KeyEvent e) {}
	}