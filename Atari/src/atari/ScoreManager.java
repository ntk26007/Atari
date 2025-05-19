package atari;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class ScoreManager {
    private int score;
    private int highScore;

    public ScoreManager() {
        this.score = 0;
        this.highScore = 0;
    }

    // Añade puntos a la puntuación actual
    public void increment(int points) {
        score += points;
        if (score > highScore) {
            highScore = score;
        }
    }

    // Dibuja la puntuación en pantalla
    public void draw(Graphics g, int x, int y) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Puntos: " + score, x, y);
        g.drawString("Puntaje Máximo: " + highScore, x, y + 20);
    }

    // Reinicia la puntuación (pero no el récord)
    public void reset() {
        score = 0;
    }

    // Getters por si los necesitas
    public int getScore() {
        return score;
    }

    public int getHighScore() {
        return highScore;
    }
}
