package atari;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.Random;

public class Brick {
	 private int x, y, width, height;
	    private boolean destroyed = false;
	    private Color fillColor;
	    private static final Random rand = new Random();

	    public Brick(int x, int y, int width, int height) {
	        this.x = x; this.y = y; this.width = width; this.height = height;
	        // color inicial aleatorio usando HSB para mayor variedad y brillo
	        float hue = rand.nextFloat();
	        float saturation = 0.8f + rand.nextFloat() * 0.2f;
	        float brightness = 0.8f + rand.nextFloat() * 0.2f;
	        this.fillColor = Color.getHSBColor(hue, saturation, brightness);
	    }
	    public void draw(Graphics2D g) {
	        if (!destroyed) {
	            g.setColor(fillColor);
	            g.fillRect(x, y, width, height);
	            g.setColor(Color.BLACK);
	            g.drawRect(x, y, width, height);
	        }
	    }
	    public Rectangle getBounds() {
	        return new Rectangle(x, y, width, height);
	    }
	    public boolean isDestroyed() { return destroyed; }
	    public void setDestroyed(boolean val) { destroyed = val; }
	}

