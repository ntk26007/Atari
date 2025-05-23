package atari;

import java.awt.*;
import java.awt.event.*;

public class MainMenu extends Frame {
    private Image bgImage;

    public MainMenu() {
        super("Menú Principal");

        bgImage = Toolkit.getDefaultToolkit().getImage("resources/mainMenu.png");

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
        
        agregarEfectoSonido(btnPlay);
        agregarEfectoSonido(btnLevel);
        agregarEfectoSonido(btnExit);


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
        
        //musica fondo
        AudioPlayer.detenerAudio();
	    AudioPlayer.reproducirAudio("Resources/menu.wav"); 

        setVisible(true);
    }
    
    //efecto de sonido al pulsar y pasar el raton
    private void agregarEfectoSonido(BotonPersonalizado boton) {
        boton.addMouseListener(new MouseAdapter() { 
            @Override
            public void mouseEntered(MouseEvent e) { //pasar el ratón
                AudioPlayer.reproducirEfecto("Resources/hover.wav");
            }

            @Override
            public void mousePressed(MouseEvent e) {
                AudioPlayer.reproducirEfecto("Resources/click.wav");
            }
        });
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

