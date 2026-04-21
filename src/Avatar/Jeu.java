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
public class Jeu {
    
    protected BufferedImage decor;
    protected int score;
    protected Player player;
    

    public Jeu() {
        try{
            this.decor = ImageIO.read(getClass().getResource("/resources/fondblanc.png"));
        }catch(IOException ex){
            Logger.getLogger(Jeu.class.getName()).log(Level.SEVERE,null, ex);
        }
        this.score=0;
        Player bee= new Player();
        this.player=bee;
    }
    
    public void rendu(Graphics2D contexte){
        contexte.drawImage(this.decor,0,0,null);
        contexte.drawString("Score : " + score,10 ,20); //1.Rendu du décor 
        //2.Rendu des sprites
        this.player.rendu(contexte);
    }
    
    public void miseAJour(){
        //1.Mise à jour de l'avatar en fonction des commandes des joueurs
        //2.Mise à jour des autres éléments (miel, monstres, etc.)
        //3. Gérer les intéractions (collisions et autres règles)
        this.player.miseAJour();
    }
        

    public Player getPlayer(){
        return this.player;
    }
    

     
    }
    


