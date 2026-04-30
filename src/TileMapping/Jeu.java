package TileMapping;

import java.awt.Graphics2D;

/**
 * Exemple de classe jeu
 *
 * @author guillaume.laurent
 */
//public class Jeu {
//
//    private Carte carte;
//     
//    public Jeu() {        
//        this.carte = new Carte("src/TileMapping/Calque1920.txt");
//    }
//
//    public void miseAJour() {
//        this.carte.miseAJour();
//    }
//
//    public void rendu(Graphics2D contexte) {
//        this.carte.rendu(contexte, 5 , 0);
//    }
//
//}
public class Jeu {
    private Carte calque1;
    private Carte calque2;
    private Carte calque3;
     
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
    
    int Xp = 5;
    int Yp = 5;
    public void rendu(Graphics2D contexte) {
        this.calque1.rendu(contexte, Xp, Yp); // dessiné en premier (fond)
        this.calque2.rendu(contexte, Xp, Yp); // 2 ème calque
        this.calque3.rendu(contexte, Xp, Yp);// 3 ème calque
    }
}
