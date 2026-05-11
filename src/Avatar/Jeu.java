/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Avatar;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import TileMapping.Carte;
import Tools.Coordinates;
import Tools.Hitbox;

/**
 *
 * @author alamas
 */
public class Jeu {

    protected BufferedImage decor;
    protected int score;
    protected Player player;
    private Carte calque1;
    private Carte calque2;
    private Carte calque3;

    private BufferedImage redimensionner(BufferedImage img, int largeur, int hauteur) {
        BufferedImage nouvelleImage = new BufferedImage(largeur, hauteur, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = nouvelleImage.createGraphics();
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                             java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(img, 0, 0, largeur, hauteur, null);
        g2d.dispose();
        return nouvelleImage;
    }

    public Jeu() {
        this.calque1 = new Carte("src/TileMapping/Calque1920.txt");
        this.calque2 = new Carte("src/TileMapping/Calque21920.txt");
        this.calque3 = new Carte("src/TileMapping/Calque31920.txt");
        this.score = 0;

        BufferedImage sprite = null;
        try {
            sprite = ImageIO.read(getClass().getResource("/resources/Mantereligieuse.png"));
            sprite = redimensionner(sprite, 96, 96);
        } catch (IOException ex) {
            Logger.getLogger(Jeu.class.getName()).log(Level.SEVERE, null, ex);
        }

        Hitbox playerHitbox = new Hitbox(new Coordinates(80, 60), 96.0, 96.0);
        Player P1 = new Player(80, 60, 200, playerHitbox, null, null, null, "Player1");
        P1.setImage(sprite);
        P1.setMovementBounds(0, 0, 1920 - 96, 1088 - 96);
        P1.startMovement();
        this.player = P1;
    }

    public void rendu(Graphics2D contexte) {
        int n = 32;
        int Xp = (int)(this.player.getX() / (double) n);
        int Yp = (int)(this.player.getY() / (double) n);
        this.calque1.rendu(contexte, Xp, Yp); // fond
        this.calque2.rendu(contexte, Xp, Yp);
        this.calque3.rendu(contexte, Xp, Yp);
        if (this.player.getImage() != null) {
            contexte.drawImage(this.player.getImage(), 560, 544 , null);
        }
    }

    public void miseAJour() {
        // Le mouvement du joueur est géré par son thread interne (startMovement)
        this.calque1.miseAJour();
        this.calque2.miseAJour();
        this.calque3.miseAJour();
    }

    public Player getPlayer() {
        return this.player;
    }
}
