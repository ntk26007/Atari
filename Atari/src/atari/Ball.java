package atari;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Rectangle;

public class Ball {
	 private double x, y;
	    private int diameter;
	    private double dx, dy;
	    private int canvasWidth, canvasHeight;
	    private boolean waiting = false;
	    private long waitStart;
	    private int spawnX, spawnY;

	    public Ball(int x, int y, int diameter, double dx, double dy, int canvasWidth, int canvasHeight) {
	        this.x = x; this.y = y; this.spawnX = x; this.spawnY = y;
	        this.diameter = diameter; this.dx = dx; this.dy = dy;
	        this.canvasWidth = canvasWidth; this.canvasHeight = canvasHeight;
	    }
	    public void update() {
	        if (waiting) {
	            if (System.currentTimeMillis() - waitStart >= 3000) {
	                waiting = false;
	            } else {
	                x = spawnX; y = spawnY;
	                return;
	            }
	        }
	        x += dx;
	        y += dy;
	    }
	    public void checkWallCollision() {
	        if (waiting) return;
	        if (x <= 0 || x + diameter >= canvasWidth) dx *= -1;
	        if (y <= 0) dy *= -1;
	        if (y + diameter >= canvasHeight) {
	            waiting = true;
	            waitStart = System.currentTimeMillis();
	            x = spawnX; y = spawnY;
	        }
	    }
	    public void checkPaddleCollision(Paddle p) {
	        if (waiting) return;
	        Rectangle ballRect = getBounds();
	        Rectangle paddleRect = new Rectangle(p.getX(), p.getY(), p.getWidth(), p.getHeight());
	        if (ballRect.intersects(paddleRect)) {
	            dy = -Math.abs(dy);
	            y = p.getY() - diameter;
	        }
	    }
	    public Rectangle getBounds() {
	        return new Rectangle((int)x, (int)y, diameter, diameter);
	    }
	    public void draw(Graphics2D g) {
	        g.setColor(Color.WHITE);
	        g.fillOval((int)x, (int)y, diameter, diameter);
	    }
	    public boolean isWaiting() { return waiting; }
	    public void reverseY() { dy *= -1; }
	    public void reverseX() { dx *= -1; }
	}