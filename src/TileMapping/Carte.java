/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TileMapping;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Exemple de classe carte
 *
 * @author guillaume.laurent
 */

// * Classe représentant une carte composée de tuiles (tiles).
// * Elle charge un tileset (image contenant toutes les tuiles),
 //* lit une carte depuis un fichier texte, et dessine le décor à l'écran.
public class Carte {

    // Largeur de la carte en nombre de tuiles (non utilisée activement ici)
    //private int largeur = 30;

    // Hauteur de la carte en nombre de tuiles (non utilisée activement ici)
    //private int hauteur = 20;

    // Taille d'une tuile en pixels (32x32 par défaut)
    private int tailleTuile = 32;

    // Image d'une tuile individuelle (déclarée mais non utilisée dans ce code)
    //private BufferedImage uneTuile;

    // Tableau contenant toutes les tuiles découpées depuis le tileset
    private BufferedImage[] tuiles;

    /**
     * Carte de décor codée en dur (tableau 2D d'entiers).
     * Chaque entier correspond à l'index d'une tuile dans le tableau `tuiles`.
     * Utilisée comme valeur par défaut si aucun fichier n'est chargé correctement.
     */
    private int[][] decor = {
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 26, 26, 26, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 26, 20, 26, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 26, 20, 26, 0, 0, 0, 0, 0, 57, 2},
        {0, 0, 0, 20, 0, 0, 0, 0, 0, 1, 2, 3},
        {2, 2, 0, 20, 0, 0, 57, 0, 18, 18, 1, 1},
        {3, 3, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1},
        {3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 1, 1},
        {1, 1, 3, 3, 3, 3, 3, 1, 1, 16, 1, 1}
    };

    public Carte(String nomdufichier) {
        
        // Fusion avec le deuxième constructeur de la classse carte
        try {
            BufferedImage tileset = ImageIO.read(getClass().getResource("images/tileSetMinecraft32x32.png"));
            tuiles = new BufferedImage[192];
            for (int i = 0; i < tuiles.length; i++) {
                int x = (i % 16) * tailleTuile;
                int y = (i / 16) * tailleTuile;
                tuiles[i] = tileset.getSubimage(x, y, tailleTuile, tailleTuile);
            }
        } catch (IOException ex) {
            Logger.getLogger(Carte.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            // Ouverture du fichier en lecture avec un BufferedReader
            BufferedReader fichier = new BufferedReader(new FileReader(nomdufichier));

            String ligne;

            // On saute les deux premières lignes (non utilisées ici)
            ligne = fichier.readLine(); // On ignore ligne 1
            ligne = fichier.readLine(); // On ignore ligne 2
            
            // On regarde la ligne 3 de notre fichier
            String ligneInfo = fichier.readLine();
            String[] champsInfo = ligneInfo.split(" ");

            int nbcol      = Integer.parseInt(champsInfo[0]); // Nombre de colonnes
            int nbligne    = Integer.parseInt(champsInfo[1]); // Nombre de lignes
            tailleTuile    = Integer.parseInt(champsInfo[2]); // Taille d'une tuile en px

            // Initialisation du tableau de décor avec les dimensions lues
            decor = new int[nbligne][nbcol];

            int n = 0; // Indice de ligne courante dans le tableau decor

            // Lecture des lignes de tuiles tant que le fichier a du contenu
            while (fichier.ready() && n < nbligne) {
                
                ligne = fichier.readLine();

                // On change de ligne 
                String[] champs = ligne.split(" ");  
                
                for (int j = 0; j < nbcol; j++) {
                    decor[n][j] = Integer.parseInt(champs[j]);
                }
                n = n + 1;
            }

            fichier.close(); // Fermeture du fichier après lecture

        } catch (IOException e) {
            // En cas d'erreur d'entrée/sortie, on affiche la pile d'appels
            e.printStackTrace();
        }
    }

    /**
     * Constructeur de la classe Carte.
     * Charge le tileset depuis les ressources du projet et découpe
     * chaque tuile en sous-images de taille `tailleTuile` x `tailleTuile`.
     *
     * Le tileset utilisé est : "images/tileSetMinecraft32x32.png"
     * Il est supposé contenir 176 tuiles disposées en grille de 16 colonnes.
     */
//    public Carte() {
//        try {
//            // Chargement de l'image tileset depuis les ressources (chemin relatif au classpath)
//            BufferedImage tileset = ImageIO.read(getClass().getResource("images/tileSetMinecraft32x32.png"));
//
//            // Initialisation du tableau pour stocker 176 tuiles individuelles
//            tuiles = new BufferedImage[176];
//
//            for (int i = 0; i < tuiles.length; i++) {
//                // Calcul de la position (x, y) de la tuile i dans le tileset
//                // Les tuiles sont disposées sur 16 colonnes
//                int x = (i % 16) * tailleTuile; // Position horizontale
//                int y = (i / 16) * tailleTuile; // Position verticale
//
//                // Découpe de la sous-image correspondant à la tuile i
//                tuiles[i] = tileset.getSubimage(x, y, tailleTuile, tailleTuile);
//            }
//
//        } catch (IOException ex) {
//            // Journalisation de l'erreur si le fichier image est introuvable ou illisible
//            Logger.getLogger(Carte.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

    /**
     * Méthode de mise à jour de la carte.
     * Actuellement vide — prévu pour de la logique d'animation ou d'événements.
     */
    public void miseAJour() {
        // TODO : ajouter la logique de mise à jour si nécessaire
    }

    /**
     * Dessine le décor de la carte dans le contexte graphique fourni.
     *
     * Appelle la méthode `Carte(String)` pour charger la carte depuis un fichier,
     * puis parcourt le tableau 2D et dessine chaque tuile à sa position (j*32, i*32).
     *
     * @param contexte Le contexte graphique 2D dans lequel dessiner les tuiles
     */
    public void rendu(Graphics2D contexte) {
        // Parcours de chaque ligne de la carte
        for (int i = 0; i < decor.length; i++) {
            // Parcours de chaque colonne de la ligne courante
            for (int j = 0; j < decor[i].length; j++) {

                // Récupération de l'index de la tuile à dessiner
                int numeroTuile = decor[i][j];

                // Dessin de la tuile à la position (j*32, i*32) en pixels
                // S'assure que l'on dessine une tuile qui existe
                if (numeroTuile >= 0 && numeroTuile < tuiles.length) {
                    contexte.drawImage(tuiles[numeroTuile], tailleTuile * j, tailleTuile * i, null);
                }
            }
        }
    }
}
 
