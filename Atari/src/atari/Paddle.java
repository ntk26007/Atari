package atari;

import java.awt.Graphics2D;
import java.awt.Color;

public class Paddle {
	private int x, y, width, height, speed = 8;
	private int canvasWidth;

	public Paddle(int x, int y, int width, int height, int canvasWidth) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.canvasWidth = canvasWidth;
	}

	public void moveLeft() {
		x = Math.max(0, x - speed);
	}

	public void moveRight() {
		x = Math.min(canvasWidth - width, x + speed);
	}

	public void draw(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.fillRect(x, y, width, height);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}