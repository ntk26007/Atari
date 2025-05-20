package atari;

import java.awt.*;
import java.awt.event.*;

public class MainMenu extends Frame {
    private Image bgImage;

    public MainMenu() {
        super("Menú Principal");

        bgImage = Toolkit.getDefaultToolkit().getImage("resources/menu2.png");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int sw = screenSize.width, sh = screenSize.height;
        setLayout(null);
        setSize(sw, sh);
        setResizable(false);
        setUndecorated(true);

        int bw = sw / 4;
        int bh = sh / 12;
        int bx = (sw - bw) / 2;
        
        // Cargar la fuente personalizada
        Font fuentePersonalizada = FuentePersonalizada.cargarFuente(48f);

     // Botones
        BotonPersonalizado btnPlay = new BotonPersonalizado("JUGAR", 150, 50);
        btnPlay.setBounds(bx, sh / 2 - bh * 2, bw, bh);
        btnPlay.setFont(fuentePersonalizada);
        btnPlay.setColorFondo(new Color(128, 0, 128));        // morado
        btnPlay.setColorTexto(Color.WHITE);
        add(btnPlay);

        BotonPersonalizado btnLevel = new BotonPersonalizado("NIVELES", 150, 50);
        btnLevel.setBounds(bx, sh / 2, bw, bh);
        btnLevel.setFont(fuentePersonalizada);
        btnLevel.setColorFondo(new Color(33, 150, 243));      // Azul
        btnLevel.setColorTexto(Color.WHITE);
        add(btnLevel);

        BotonPersonalizado btnExit = new BotonPersonalizado("SALIR", 150, 50);
        btnExit.setBounds(bx, sh / 2 + bh * 2, bw, bh);
        btnExit.setFont(fuentePersonalizada);
        btnExit.setColorFondo(new Color(244, 67, 54));         // Rojo
        btnExit.setColorTexto(Color.WHITE);
        add(btnExit);

        // Eventos de botones
        btnPlay.setAccion(() -> {
            dispose();
            BreakoutGame.launchGame();
        });

        btnLevel.setAccion(() -> {
            GameCanvas.showLevelSelectionMenu();
        });

        btnExit.setAccion(() -> {
            System.exit(0);
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

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

//package atari;
//
//import java.awt.*;
//import java.awt.event.*;
//
//public class MainMenu extends Frame {
//    private Image bgImage;
//
//    public MainMenu() {
//        super("Menú Principal");
//
//        // Cargar imagen de fondo
//        bgImage = Toolkit.getDefaultToolkit().getImage("resources/2.jpg");
//
//        // Pantalla completa
//        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//        int sw = screenSize.width, sh = screenSize.height;
//        setLayout(null);
//        setSize(sw, sh);
//        setResizable(false);
//        setUndecorated(true);
//        
//
//        // Tamaños de botones
//        int bw = sw / 4;
//        int bh = sh / 12;
//        int bx = (sw - bw) / 2;
//
//        Font fuentePersonalizada = FuentePersonalizada.cargarFuente(24f);
//        
//        // Botón "Jugar"
//        Button btnPlay = new Button("JUGAR");
//        btnPlay.setBounds(bx, sh / 2 - bh * 2, bw, bh);
//        btnPlay.setFont(fuentePersonalizada);
//        add(btnPlay);
//
//        // Botón "Niveles"
//        Button btnLevel = new Button("NIVELES");
//        btnLevel.setBounds(bx, sh / 2, bw, bh);
//        btnLevel.setFont(fuentePersonalizada);
//        add(btnLevel);
//
//        // Botón "Salir"
//        Button btnExit = new Button("SALIR");
//        btnExit.setBounds(bx, sh / 2 + bh * 2, bw, bh);
//        btnExit.setFont(fuentePersonalizada);
//        add(btnExit);
//
//        // Eventos de botones
//        btnPlay.addActionListener(e -> {
//            dispose();
//            BreakoutGame.launchGame();
//        });
//
//        btnLevel.addActionListener(e -> {
//            GameCanvas.showLevelSelectionMenu();
//        });
//
//        btnExit.addActionListener(e -> {
//            System.exit(0);
//        });
//
//        // Cerrar ventana
//        addWindowListener(new WindowAdapter() {
//            public void windowClosing(WindowEvent e) {
//                System.exit(0);
//            }
//        });
//
//        setResizable(false);
//        setVisible(true);
//    }
//
//    @Override
//    public void paint(Graphics g) {
//        if (bgImage != null) {
//            g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
//        }
//    }
//
//    public static void main(String[] args) {
//        new MainMenu();
//    }
//}
