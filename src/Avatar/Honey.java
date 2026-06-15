
package Avatar;

import Tools.Coordinates;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * @author lamas anais
 */
public class Honey {

    protected BufferedImage sprite;
    protected Coordinates position;
    
    private BufferedImage redimensionner(BufferedImage img, int largeur, int hauteur) {
        // On crée une nouvelle image (TYPE_INT_ARGB)
        BufferedImage nouvelleImage = new BufferedImage(largeur, hauteur, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = nouvelleImage.createGraphics();
        // On active le lissage pour une meilleure qualité
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                             java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        // On dessine l'ancienne image dans la nouvelle
        g2d.drawImage(img, 0, 0, largeur, hauteur, null);
        g2d.dispose();
        return nouvelleImage;
    }

    public Honey() {
        try {
            this.sprite = ImageIO.read(getClass().getResource("/resources/honey.png"));
        } catch (IOException ex) {
            Logger.getLogger(Honey.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.sprite=redimensionner(this.sprite,(int) GameConstants.HIVE_SIZE,(int)GameConstants.HIVE_SIZE);
        this.position=new Coordinates(GameConstants.HIVE_X, GameConstants.HIVE_Y);
    }
    
    public void setPosition(Coordinates position){
        this.position=position;
    }

    public void miseAJourMini() {
        try {
            this.sprite = ImageIO.read(getClass().getResource("/resources/honey.png"));
        } catch (IOException ex) {
            Logger.getLogger(Honey.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.sprite=redimensionner(this.sprite,30,30);
    }
    public void miseAJourMaxi(){
        try {
            this.sprite = ImageIO.read(getClass().getResource("/resources/honey.png"));
        } catch (IOException ex) {
            Logger.getLogger(Honey.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.sprite=redimensionner(this.sprite,(int) GameConstants.HIVE_SIZE,(int)GameConstants.HIVE_SIZE);
    }

    public void rendu(Graphics2D contexte) {
        contexte.drawImage(this.sprite, (int) this.position.getX(), (int) this.position.getY(), null);
    }

    public BufferedImage getSprite() {
        return sprite;
    }

    public Coordinates getPosition() {
        return position;
    }


    public double getX() {
        return this.position.getX();
    }

    public double getY() {
        return this.position.getY();
    }
    
    public void setX(double x) {
        this.position.setX(x);
    }

    public void setY(double y) {
        this.position.setY(y);
    }
    

}

