package atari;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BotonPersonalizado extends Canvas{
	  private Image imagen;
	    private String rutaImagen;
	    private Runnable accion;
	    private boolean hover = false;

	    public BotonPersonalizado(String rutaImagen, int ancho, int alto) {
	        this.rutaImagen = rutaImagen;
	        this.imagen = Toolkit.getDefaultToolkit().getImage(rutaImagen);
	        setSize(ancho, alto);

	        addMouseListener(new MouseAdapter() {
	            @Override
	            public void mouseClicked(MouseEvent e) {
	                if (accion != null) accion.run();
	            }

	            //cuando entra
	            @Override
	            public void mouseEntered(MouseEvent e) {
	                hover = true;
	                repaint();
	            }

	            @Override
	            public void mouseExited(MouseEvent e) {
	                hover = false;
	                repaint();
	            }
	        });
	    }

	    public void setAccion(Runnable accion) {
	        this.accion = accion;
	    }

	    @Override
	    public void paint(Graphics g) {
	        if (imagen != null) {
	            g.drawImage(imagen, 0, 0, getWidth(), getHeight(), this);
	        }

	        if (hover) {
	            g.setColor(new Color(255, 255, 255, 60)); // efecto visual 
	            g.fillRect(0, 0, getWidth(), getHeight());
	        }
	    }

	    @Override
	    public void update(Graphics g) {
	        paint(g); // evitar parpadeo
	    }
	}