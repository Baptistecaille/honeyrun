package TileMapping;

import Avatar.Player;
import java.awt.Graphics2D;
import Tools.Coordinates ;
import Avatar.Avatar;
import Tools.Hitbox;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Exemple de classe jeu
 *
 * @author guillaume.laurent
 */
public class Jeu {
    private Carte calque1;
    private Carte calque2;
    private Carte calque3;
    private Avatar avatar1;
     
    public Jeu() {        
        this.calque1 = new Carte("src/TileMapping/Calque1920.txt");
        this.calque2 = new Carte("src/TileMapping/Calque21920.txt");
        this.calque3 = new Carte("src/TileMapping/Calque31920.txt");
        
        
    }

    public void miseAJour() {
        this.calque1.miseAJour();
        this.calque2.miseAJour();
        this.calque3.miseAJour();
    }

    
    public void rendu(Graphics2D contexte) {
        int n = 32 ;
        double a = this.avatar1.getPosition().getX();
        double b = this.avatar1.getPosition().getY();
        int Xp = (int) (a / (double) n) ;               //On passe n en double pour pouvoir diviser a et on récupère un int pour avoir le quotient 
        int Yp = (int)(b / (double) n) ;
        this.calque1.rendu(contexte, Xp, Yp); // dessiné en premier (fond)
        this.calque2.rendu(contexte, Xp, Yp); // 2 ème calque
        this.calque3.rendu(contexte, Xp, Yp);// 3 ème calque
    }
}
