package atari;

import java.awt.Graphics2D;

public class BrickManager {
	private Brick[][] grid;
	private int rows, cols;

	public BrickManager(int rows, int cols, int bw, int bh, int oX, int oY) {
		this.rows = rows;
		this.cols = cols;
		grid = new Brick[rows][cols];
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++) {
				int x = oX + c * (bw + 5), y = oY + r * (bh + 5);
				grid[r][c] = new Brick(x, y, bw, bh);
			}
	}

	//dibuja todos los ladrillos
	public void draw(Graphics2D g) {
		for (Brick[] row : grid)
			for (Brick b : row)
				b.draw(g);
	}

	/**
	 * Comprueba colisión y destruye ladrillo. Devuelve true si destruyó uno.
	 */
	public boolean checkBallCollision(Ball ball) {
		if (ball.isWaiting())
			return false;
		for (Brick[] row : grid) {
			for (Brick b : row) {
				if (!b.isDestroyed() && ball.getBounds().intersects(b.getBounds())) {
					b.setDestroyed(true);
					ball.reverseY();
					return true;
				}
			}
		}
		return false;
	}

	//comprueba cada fila y columna si estan rotos
	public boolean isEmpty() {
		for (Brick[] row : grid) {
			for (Brick b : row) {
				if (!b.isBroken())
					return false;
			}
		}
		return true;
	}

}