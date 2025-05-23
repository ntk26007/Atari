package atari;

import java.awt.AWTEvent;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class BotonVolver extends Canvas implements MouseListener, MouseMotionListener {
	 private String texto;
	    private Color colorFondo = new Color(70, 130, 180);       // Azul por defecto
	    private Color colorFondoHover = new Color(100, 149, 237); // Azul claro al pasar mouse
	    private Color colorTexto = Color.WHITE;

	    private boolean hover = false;
	    private boolean presionado = false;
	    private Runnable accion;

	    public BotonVolver(String texto) {
	        this.texto = texto;
	        setSize(120, 45);
	        setVisible(true);
	        setBackground(new Color(0, 0, 0, 0)); // Fondo transparente
	        addMouseListener(this);
	        addMouseMotionListener(this);
	        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	    }

	    public void setAccion(Runnable accion) {
	        this.accion = accion;
	    }

	    @Override
	    public void paint(Graphics g) {
	        // Fondo dinámico
	        if (presionado) {
	            g.setColor(colorFondo.darker());
	        } else if (hover) {
	            g.setColor(colorFondoHover);
	        } else {
	            g.setColor(colorFondo);
	        }

	        g.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
	        g.setColor(Color.BLACK);
	        g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

	        // Texto centrado
	        g.setFont(new Font("Arial", Font.BOLD, 20));
	        FontMetrics fm = g.getFontMetrics();
	        int x = (getWidth() - fm.stringWidth(texto)) / 2;
	        int y = (getHeight() + fm.getAscent()) / 2 - 4;
	        g.setColor(colorTexto);
	        g.drawString(texto, x, y);
	    }

	    @Override
	    public void update(Graphics g) {
	        paint(g); // evitar parpadeos
	    }

	    @Override
	    public void mouseEntered(MouseEvent e) {
	        hover = true;
	        AudioPlayer.reproducirEfecto("Resources/hover.wav");
	        repaint();
	    }

	    @Override
	    public void mouseExited(MouseEvent e) {
	        hover = false;
	        repaint();
	    }

	    @Override
	    public void mousePressed(MouseEvent e) {
	        presionado = true;
	        AudioPlayer.reproducirEfecto("Resources/click.wav");
	        repaint();
	    }

	    @Override
	    public void mouseReleased(MouseEvent e) {
	        presionado = false;
	        repaint();
	        if (accion != null && hover) {
	            accion.run();
	        }
	    }

	    @Override public void mouseClicked(MouseEvent e) {} // no usado
	    @Override public void mouseDragged(MouseEvent e) {}
	    @Override public void mouseMoved(MouseEvent e) {}

	    public void setColorFondo(Color c) {
	        this.colorFondo = c;
	    }

	    public void setColorTexto(Color c) {
	        this.colorTexto = c;
	    }
	}