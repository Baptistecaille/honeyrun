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

/**
 *
 * @author alamas
 */


public class Player {
    
    protected BufferedImage sprite;    //une image ou une animation bidimensionnelle intégrée dans une scène ou un environnement de jeu plus vaste
    protected double x, y;
    private boolean toucheGauche, toucheDroite, toucheHaut, toucheBas;
    private int score;

    private BufferedImage redimensionner(BufferedImage img, int largeur, int hauteur) {
    // On crée une nouvelle image avec la transparence (TYPE_INT_ARGB)
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
    
    public Player() {
        try {
            this.sprite = ImageIO.read(getClass().getResource("/resources/bee.png"));
            this.sprite=redimensionner(this.sprite,100,100);
        } catch (IOException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.x=0;  // placement de départ
        this.y=10;  // placement de départ 
        this.score=0;
        this.toucheGauche = false; // 
        this.toucheDroite = false;
        this.toucheBas = false;
        this.toucheHaut=false;
    }

    public void miseAJour() {
        if (this.toucheGauche){
            x-= 5;
        }
        if (this.toucheDroite){
            x+= 5;
        }
        if (this.toucheBas){
            y+= 5;
        }
        if (this.toucheHaut){
            y-= 5;
        }
        if (x> 1920 - sprite.getWidth()){// collision avec le bord droit de la scene
            x = 1920 - sprite.getWidth();
        }
        if (x<0){// collision avec le bord gauche de la scene
            x = 0;
        }   
        if(y> 1088 - sprite.getHeight()){  // collision avec le bord bas de la scene
            y = 1088 - sprite.getHeight();
        }
        if (y<0){// collision avec le bord haut de la scene
            y = 0;
        }
        }
        
    

    public void rendu(Graphics2D contexte) {
        contexte.drawImage(this.sprite, (int) x, (int) y, null);
    }


    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
    
    public double getLargeur() {
        return sprite.getHeight();
    }

    public double getHauteur() {
        return sprite.getWidth();
    }
    
    public void setToucheDroite(boolean etat){
        this.toucheDroite = etat;
    }

    
    public void setToucheGauche(boolean etat){
        this.toucheGauche = etat; 
    }
    
    public void setToucheHaut(boolean etat){
        this.toucheHaut = etat;
    }
    
    public void setToucheBas(boolean etat){
        this.toucheBas = etat;
    }
    
    
}