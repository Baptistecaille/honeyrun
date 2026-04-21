package TileMapping;

import java.awt.Graphics2D;

/**
 * Exemple de classe jeu
 *
 * @author guillaume.laurent
 */
public class Jeu {

    private Carte carte;
     
    public Jeu() {        
        this.carte = new Carte("src/TileMapping/Calque1920.txt");
    }

    public void miseAJour() {
        this.carte.miseAJour();
    }

    public void rendu(Graphics2D contexte) {
        this.carte.rendu(contexte);
    }

}
