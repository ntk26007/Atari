package atari;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
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

	    public GameCanvas(int width, int height) {
	        this.width = width;
	        this.height = height;
	        setPreferredSize(new Dimension(width, height));
	        addKeyListener(this);
	        setFocusable(true);
	        requestFocus();

	        initGame();
	    }

	    private void initGame() {
	        paddle = new Paddle((width - 100) / 2, height - 50, 100, 10, width);
	        ball   = new Ball(width / 2, height / 2, 10, 4, 4, width, height);
	        bricks = new BrickManager(5, 10, 60, 20, 50, 50);
	    }

	    @Override
	    public void run() {
	        running = true;
	        long lastTime = System.nanoTime();
	        double nsPerUpdate = 1000000000.0 / 60.0; // 60 UPS
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
	            try { Thread.sleep(2); } catch (InterruptedException e) {}
	        }
	    }

	    private void update() {
	        if (!ball.isWaiting()) {
	            if (leftPressed)  paddle.moveLeft();
	            if (rightPressed) paddle.moveRight();
	            ball.update();
	            ball.checkWallCollision();
	            ball.checkPaddleCollision(paddle);
	            bricks.checkBallCollision(ball);
	        } else {
	            ball.update(); // mantiene la posición en centro hasta 3 sec
	        }
	    }

	    private void render() {
	        BufferStrategy bs = getBufferStrategy();
	        if (bs == null) {
	            createBufferStrategy(3);
	            return;
	        }
	        Graphics g = bs.getDrawGraphics();
	        Graphics2D g2 = (Graphics2D) g;

	        g2.setColor(Color.BLACK);
	        g2.fillRect(0, 0, width, height);

	        paddle.draw(g2);
	        ball.draw(g2);
	        bricks.draw(g2);

	        g.dispose();
	        bs.show();
	    }

	    @Override public void keyPressed(KeyEvent e) {
	        if (e.getKeyCode() == KeyEvent.VK_LEFT)  leftPressed  = true;
	        if (e.getKeyCode() == KeyEvent.VK_RIGHT) rightPressed = true;
	    }
	    @Override public void keyReleased(KeyEvent e) {
	        if (e.getKeyCode() == KeyEvent.VK_LEFT)  leftPressed  = false;
	        if (e.getKeyCode() == KeyEvent.VK_RIGHT) rightPressed = false;
	    }
	    @Override public void keyTyped(KeyEvent e) {}
	}