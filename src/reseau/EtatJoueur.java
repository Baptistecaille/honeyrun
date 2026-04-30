package reseau;

import java.io.Serializable;

public class EtatJoueur implements Serializable {

    private static final long serialVersionUID = 1L;

    public int id;
    public int x;
    public int y;
    public boolean hasHoney;
    public boolean hasWon;
    public int lives;

    public EtatJoueur(int id, int x, int y, boolean hasHoney, boolean hasWon, int lives) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.hasHoney = hasHoney;
        this.hasWon = hasWon;
        this.lives = lives;
    }
}
