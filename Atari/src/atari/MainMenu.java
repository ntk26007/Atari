package atari;

import java.awt.*;
import java.awt.event.*;

public class MainMenu extends Frame {
    private Image bgImage;

    public MainMenu() {
        super("Menú Principal");

        // Cargar imagen de fondo
        bgImage = Toolkit.getDefaultToolkit().getImage("resources/2.jpg");

        // Pantalla completa
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int sw = screenSize.width, sh = screenSize.height;
        setSize(sw, sh);
        setLayout(null); // posicionamiento manual

        // Tamaños de botones
        int bw = sw / 4;
        int bh = sh / 12;
        int bx = (sw - bw) / 2;

        // Botón "Jugar"
        Button btnPlay = new Button("JUGAR");
        btnPlay.setBounds(bx, sh / 2 - bh * 2, bw, bh);
        btnPlay.setFont(new Font("04b 30", Font.BOLD, 24));
        add(btnPlay);

        // Botón "Niveles"
        Button btnLevel = new Button("NIVELES");
        btnLevel.setBounds(bx, sh / 2, bw, bh);
        btnLevel.setFont(new Font("Arial", Font.BOLD, 24));
        add(btnLevel);

        // Botón "Salir"
        Button btnExit = new Button("SALIR");
        btnExit.setBounds(bx, sh / 2 + bh * 2, bw, bh);
        btnExit.setFont(new Font("Arial", Font.BOLD, 24));
        add(btnExit);

        // Eventos de botones
        btnPlay.addActionListener(e -> {
            dispose();
            BreakoutGame.launchGame();
        });

        btnLevel.addActionListener(e -> {
            GameCanvas.showLevelSelectionMenu();
        });

        btnExit.addActionListener(e -> {
            System.exit(0);
        });

        // Cerrar ventana
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        setResizable(false);
        setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    public static void main(String[] args) {
        new MainMenu();
    }
}
