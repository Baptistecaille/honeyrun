/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Avatar;

import TileMapping.Carte;
import Tools.Avatar;
import Tools.Coordinates;
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
public class Jeu {
    
    protected BufferedImage decor;
    protected int score;
    protected Player player;
    private Carte carte;
    private Carte calque1;
    private Carte calque2;
    private Carte calque3;
    
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
  

    public Jeu(){
        
        this.calque1 = new Carte("src/TileMapping/Calque1920.txt");
        this.calque2 = new Carte("src/TileMapping/Calque21920.txt");
        this.calque3 = new Carte("src/TileMapping/Calque31920.txt");
//        try{
//            this.decor = ImageIO.read(getClass().getResource("/resources/fondblanc.png"));
//        }catch(IOException ex){
//            Logger.getLogger(Jeu.class.getName()).log(Level.SEVERE,null, ex);
//        }
        this.score=0;
        BufferedImage sprite =  null;
        try {
            sprite = ImageIO.read(getClass().getResource("/resources/mantereligieuse.png"));
            sprite=redimensionner(sprite,96,96);
        } catch (IOException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        }
        Coordinates position=new Coordinates(0,0);
        Coordinates spawn=new Coordinates (80,60);
        Avatar avatar=new Avatar(position, sprite , 10);
        Player P1= new Player(avatar,spawn,null);
        this.player=P1;
        
        
    }
    
    public void rendu(Graphics2D contexte){
        
        //contexte.drawString("Score : " + score,10 ,20);
        int n = 32 ;
        double a = this.player.getAvatar().getPosition().getX();
        double b = this.player.getAvatar().getPosition().getY();
        int Xp = (int) (a / (double) n) ;               //On passe n en double pour pouvoir diviser a et on récupère un int pour avoir le quotient 
        int Yp = (int)(b / (double) n) ;
        this.calque1.rendu(contexte, Xp, Yp); // dessiné en premier (fond)
        this.calque2.rendu(contexte, Xp, Yp); // 2 ème calque
        this.calque3.rendu(contexte, Xp, Yp);// 3 ème calque
                                    //1.Rendu du décor 
        //2.Rendu des sprites
        this.player.rendu(contexte);
    }
    
    public void miseAJour(){
        //1.Mise à jour de l'avatar en fonction des commandes des joueurs
        //2.Mise à jour des autres éléments (miel, monstres, etc.)
        //3. Gérer les intéractions (collisions et autres règles)
        this.player.miseAJour();
        this.calque1.miseAJour();
        this.calque2.miseAJour();
        this.calque3.miseAJour();// pour avoir le fond
    }
        

    public Player getPlayer(){
        return this.player;
    }
    

     
    }
    


