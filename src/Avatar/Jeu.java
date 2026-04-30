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
//        try{
//            this.decor = ImageIO.read(getClass().getResource("/resources/fondblanc.png"));
//        }catch(IOException ex){
//            Logger.getLogger(Jeu.class.getName()).log(Level.SEVERE,null, ex);
//        }
        this.score=0;
        BufferedImage sprite =  null;
        try {
            sprite = ImageIO.read(getClass().getResource("/resources/mantereligieuse.png"));
            //sprite=redimensionner(sprite,70,70);
        } catch (IOException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        }
        Coordinates position=new Coordinates(0,0);
        Coordinates spawn=new Coordinates (80,60);
        Avatar avatar=new Avatar(position, sprite , 10);
        Player P1= new Player(avatar,spawn,null);
        this.player=P1;
        this.carte = new Carte("src/TileMapping/Calque1920.txt");
        
    }
    
    public void rendu(Graphics2D contexte){
        contexte.drawImage(this.decor,0,0,null);
        contexte.drawString("Score : " + score,10 ,20);
        this.carte.rendu(contexte,2 ,10); //pour avoir le fond
                                    //1.Rendu du décor 
        //2.Rendu des sprites
        this.player.rendu(contexte);
    }
    
    public void miseAJour(){
        //1.Mise à jour de l'avatar en fonction des commandes des joueurs
        //2.Mise à jour des autres éléments (miel, monstres, etc.)
        //3. Gérer les intéractions (collisions et autres règles)
        this.player.miseAJour();
        this.carte.miseAJour(); // pour avoir le fond
    }
        

    public Player getPlayer(){
        return this.player;
    }
    

     
    }
    


