package atari;

import java.awt.Graphics2D;

public class BrickManager {
	 private Brick[][] grid;
	    private int rows, cols;

	    public BrickManager(int rows, int cols, int brickWidth, int brickHeight, int offsetX, int offsetY) {
	        this.rows = rows; this.cols = cols;
	        grid = new Brick[rows][cols];
	        for (int r = 0; r < rows; r++) {
	            for (int c = 0; c < cols; c++) {
	                int x = offsetX + c * (brickWidth + 5);
	                int y = offsetY + r * (brickHeight + 5);
	                grid[r][c] = new Brick(x, y, brickWidth, brickHeight);
	            }
	        }
	    }
	    public void draw(Graphics2D g) {
	        for (Brick[] row : grid) for (Brick b : row) b.draw(g);
	    }
	    public void checkBallCollision(Ball ball) {
	        if (ball.isWaiting()) return;
	        for (Brick[] row : grid) {
	            for (Brick b : row) {
	                if (!b.isDestroyed() && ball.getBounds().intersects(b.getBounds())) {
	                    b.setDestroyed(true);
	                    ball.reverseY();
	                    return;
	                }
	            }
	        }
	    }
	}
