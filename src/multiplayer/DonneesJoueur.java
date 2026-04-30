package multiplayer;

import java.awt.Color;

public class DonneesJoueur {

    public static final Color[] COULEURS = {
        null,         // index 0 inutilisé
        Color.RED,    // 1 — Rouge
        Color.BLUE,   // 2 — Bleu
        Color.GREEN,  // 3 — Vert
        Color.YELLOW  // 4 — Jaune
    };

    public final int id;
    public final String nom;
    public final double x;
    public final double y;
    public final double spawnX;
    public final double spawnY;
    public final int avatar;
    public final boolean hasHoney;
    public final boolean hasWin;
    public final int lifes;

    public DonneesJoueur(int id, String nom, double x, double y, double spawnX, double spawnY,
                         int avatar, boolean hasHoney, boolean hasWin, int lifes) {
        this.id = id;
        this.nom = nom;
        this.x = x;
        this.y = y;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.avatar = avatar;
        this.hasHoney = hasHoney;
        this.hasWin = hasWin;
        this.lifes = lifes;
    }

    public Color getCouleur() {
        return (avatar >= 1 && avatar <= 4) ? COULEURS[avatar] : Color.WHITE;
    }
}