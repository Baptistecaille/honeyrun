/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Avatar;

import Tools.Avatar;
import Tools.Coordinates;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author alamas
 */


public class Player {
    
    //protected BufferedImage sprite;//une image ou une animation bidimensionnelle intégrée dans une scène ou un environnement de jeu plus vaste
    private Avatar avatar;
    private Coordinates spawn;
    private boolean toucheGauche, toucheDroite, toucheHaut, toucheBas;
    private int score;
    private String name;
    private int lives;            // vies du joueur 
    private boolean hasHoney ;   // le joeur possède ou non du miel
    private boolean isHarvesting ; // le joueur est-il entrain de collecter du miel ?
    private long harvestStartTime ;// Timestamp when harvesting started (in milliseconds since epoch)
    private boolean hasWin; // le joueur a-t-il gagné?
    private boolean hasLost; // le joueur a-t-il encore des vies ?

//    private BufferedImage redimensionner(BufferedImage img, int largeur, int hauteur) {
//    // On crée une nouvelle image avec la transparence (TYPE_INT_ARGB)
//    BufferedImage nouvelleImage = new BufferedImage(largeur, hauteur, BufferedImage.TYPE_INT_ARGB);
//    Graphics2D g2d = nouvelleImage.createGraphics();
//    
//    // On active le lissage pour une meilleure qualité
//    g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, 
//                         java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//    
//    // On dessine l'ancienne image dans la nouvelle
//    g2d.drawImage(img, 0, 0, largeur, hauteur, null);
//    g2d.dispose();
    
//    return nouvelleImage;
//}
//    
    public Player(Avatar avatar, Coordinates spawn, String name) {
//        try {
//            this.sprite = ImageIO.read(getClass().getResource("/resources/bee.png"));
//            this.sprite=redimensionner(this.sprite,70,70);
//        } catch (IOException ex) {
//            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
//        }
        this.avatar=avatar;
        this.name=name;
        this.spawn=spawn;
        this.avatar.getPosition().setX(this.spawn.getX());
        this.avatar.getPosition().setY(this.spawn.getY());
        this.score=0;
        this.toucheGauche = false; 
        this.toucheDroite = false;
        this.toucheBas = false;
        this.toucheHaut=false;
        this.lives=3;
        this.hasHoney=false;
        this.isHarvesting=false;
        this.hasWin=false;
        this.hasLost=false;
        
    }

    public boolean isToucheGauche() {
        return toucheGauche;
    }

    public boolean isToucheDroite() {
        return toucheDroite;
    }

    public boolean isToucheHaut() {
        return toucheHaut;
    }

    public boolean isToucheBas() {
        return toucheBas;
    }

    public int getLives() {
        return lives;
    }

    public boolean isHasHoney() {
        return hasHoney;
    }

    public boolean isIsHarvesting() {
        return isHarvesting;
    }

    public long getHarvestStartTime() {
        return harvestStartTime;
    }

    public boolean isHasWin() {
        return hasWin;
    }

    public boolean isHasLost() {
        return hasLost;
    }

    public void miseAJour() {
        if (this.toucheGauche){
            double x = this.avatar.getPosition().getX();
            x-= 5;
            this.avatar.getPosition().setX(x);
        }
        if (this.toucheDroite){
            double x = this.avatar.getPosition().getX();
            x+= 5;
            this.avatar.getPosition().setX(x);
        }
        if (this.toucheBas){
            double y = this.avatar.getPosition().getY();
            y+= 5;
            this.avatar.getPosition().setY(y);
        }
        if (this.toucheHaut){
            double y = this.avatar.getPosition().getY();
            y-= 5;
            this.avatar.getPosition().setY(y);
        }
        if (this.avatar.getPosition().getX()> 1920 - this.avatar.getImage().getWidth()){// collision avec le bord droit de la scene
            this.avatar.getPosition().setX( 1920 - this.avatar.getImage().getWidth());
        }
        if (this.avatar.getPosition().getX()<0){// collision avec le bord gauche de la scene
           this.avatar.getPosition().setX(0);
        }   
        if(this.avatar.getPosition().getY()> 1088 - this.avatar.getImage().getHeight()){  // collision avec le bord bas de la scene
           this.avatar.getPosition().setY(1088 - this.avatar.getImage().getHeight());
        }
        if (this.avatar.getPosition().getY()<0){// collision avec le bord haut de la scene
            this.avatar.getPosition().setY(0);
        }
        }
        
    

    public void rendu(Graphics2D contexte) {
        contexte.drawImage(this.avatar.getImage(), (int) this.avatar.getPosition().getX(), (int)this.avatar.getPosition().getY(), null);
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public void setAvatar(Avatar avatar) {
        this.avatar = avatar;
    }

    public void setSpawn(Coordinates spawn) {
        this.spawn = spawn;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    

    public Coordinates getSpawn() {
        return spawn;
    }

    public int getScore() {
        return score;
    }

    public String getName() {
        return name;
    }
    
    
    


    
//    public double getLargeur() {
//        return sprite.getHeight();
//    }
//
//    public double getHauteur() {
//        return sprite.getWidth();
//    }
    
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