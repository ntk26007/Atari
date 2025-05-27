package atari;

import java.awt.*;
import java.awt.event.*;

public class BotonPersonalizado extends Canvas {
    private Image imagen;
    private String texto;
    private Runnable accion;
    private boolean hover = false;
    private Color colorFondo;
    private Color colorFondoHover;
    private Color colorTexto = Color.WHITE;


    public BotonPersonalizado(String texto, int ancho, int alto) {
        this.texto = texto;
        setSize(ancho, alto);

        // Mouse
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (accion != null) accion.run();
            }

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

    public void setColorFondo(Color color) {
        this.colorFondo = color;
        this.colorFondoHover = aclararColor(color, 0.2f); // efecto hover
        repaint();
    }

    public void setColorTexto(Color color) {
        this.colorTexto = color;
        repaint();
    }

    public void setImagen(String rutaImagen) {
        this.imagen = Toolkit.getDefaultToolkit().getImage(rutaImagen);
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        // Pinta fondo del botón, 
        // Si hover es true, devuelve colorFondoHover , Si hover es false, devuelve colorFondo
        
        g2.setColor(hover ? colorFondoHover : colorFondo);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Dibuja imagen si está presente
        if (imagen != null) {
            g2.drawImage(imagen, 0, 0, getWidth(), getHeight(), this);
        }
        if (imagen != null) {
            g2.drawImage(imagen, 0, 0, getWidth(), getHeight(), this);
        }

        // Texto centrado
        g2.setColor(colorTexto);
        FontMetrics fm = g2.getFontMetrics();
        int anchoTexto = fm.stringWidth(texto);
        int altoTexto = fm.getAscent();
        int x = (getWidth() - anchoTexto) / 2;
        int y = (getHeight() + altoTexto) / 2 - 4;
        g2.drawString(texto, x, y);
    }

    @Override
    public void update(Graphics g) {
        paint(g);
    }

    private Color aclararColor(Color color, float factor) {
        int r = (int) Math.min(255, color.getRed() + (255 - color.getRed()) * factor);
        int g = (int) Math.min(255, color.getGreen() + (255 - color.getGreen()) * factor);
        int b = (int) Math.min(255, color.getBlue() + (255 - color.getBlue()) * factor);
        return new Color(r, g, b);
    }
}