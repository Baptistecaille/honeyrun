/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TileMapping;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
            BufferedImage tileset = ImageIO.read(getClass().getResource("images/TileSetRpg.png"));
            tuiles = new BufferedImage[432];
            for (int i = 0; i < tuiles.length; i++) {
                int x = (i % 24) * tailleTuile;
                int y = (i / 24) * tailleTuile;
                tuiles[i] = tileset.getSubimage(x, y, tailleTuile, tailleTuile);
            }
        } catch (IOException ex) {
            Logger.getLogger(Carte.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            // Ouverture du fichier en lecture avec un BufferedReader
            BufferedReader fichier = ouvrirCarte(nomdufichier);

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

    private BufferedReader ouvrirCarte(String nomdufichier) throws IOException {
        // On garde le chemin disque pour le dev, mais on sait aussi relire la carte depuis les ressources.
        File fichierLocal = new File(nomdufichier);
        if (fichierLocal.exists()) {
            return new BufferedReader(new FileReader(fichierLocal));
        }

        String cheminRessource = nomdufichier.replace('\\', '/');
        if (cheminRessource.startsWith("src/")) {
            cheminRessource = cheminRessource.substring(4);
        }

        InputStream flux = getClass().getResourceAsStream("/" + cheminRessource);
        if (flux != null) {
            return new BufferedReader(new InputStreamReader(flux));
        }

        throw new IOException("Carte introuvable: " + nomdufichier);
    }
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

    
    public void rendu(Graphics2D contexte, int x, int y) {
    for (int i = 0; i < decor.length; i++) {
        for (int j = 0; j < decor[i].length; j++) {
            int tuileParDefaut = 144;
            int numeroTuile;

            // Vérifie si on est hors limites
            if (i < 0 || i >= decor.length || j < 0 || j >= decor[0].length) {
                numeroTuile = tuileParDefaut; // on remplit le trou
            } 
            else {
                numeroTuile = decor[i][j];
            }
            
            if (numeroTuile <= 0) continue;
            
            int zoom = 3; 
            int offsetX = 10;
            int offsetY = 5;
            
            
            if (numeroTuile < tuiles.length) { // on dessine notre tuile
                contexte.drawImage(tuiles[numeroTuile],zoom * tailleTuile * (j - x + offsetX),zoom * tailleTuile * (i - y + offsetY ),tailleTuile * zoom,tailleTuile * zoom,null);
            }
        }
    }
}
    
public BufferedImage genererImageMiniMapAvecPointTuile(int largeur, int hauteur, double PosX, double PosY, Color couleur) {
  
    // ÉTAPES 1 & 2 : Générer la minimap de base
    
    int cartePixelW = decor[0].length * tailleTuile;  // On récup longueur carte
    int cartePixelH = decor.length * tailleTuile;   // On récup hauteur carte
    
    BufferedImage imageComplete = new BufferedImage(cartePixelW, cartePixelH, BufferedImage.TYPE_INT_ARGB); // On crée une image vide
    Graphics2D g = imageComplete.createGraphics(); // On crée un objet g pour dessiner sur notre image vide
    
    // On dessine notre map sur l'image vide à l'aide des tuiles 
    
    for (int i = 0; i < decor.length; i++) {
        for (int j = 0; j < decor[i].length; j++) {
            int numeroTuile = decor[i][j];
            if (numeroTuile <= 0 || numeroTuile >= tuiles.length) continue;
            g.drawImage(tuiles[numeroTuile], 
                       j * tailleTuile, 
                       i * tailleTuile, 
                       tailleTuile, 
                       tailleTuile, 
                       null);
        }
    }
    g.dispose();
    
    BufferedImage minimap = new BufferedImage(largeur, hauteur, BufferedImage.TYPE_INT_ARGB); // On crée notre minimap vide
    Graphics2D gMini = minimap.createGraphics(); // On crée un nouvel objet pour dessiner sur notre minimap
    gMini.drawImage(imageComplete, 0, 0, largeur, hauteur, null); // Permet de dessiner la mini map par dessus notre map de base 
    
    // ÉTAPE 3 : CONVERTIR LA POSITION EN TUILES → PIXELS → MINIMAP
  
    // Convertir position en tuiles → position en pixels (sur la carte complète)
    // Exemple : tuile (5, 3) avec tailleTuile=32 → pixel (160, 96)
    int pixelX = (int) PosX;
    int pixelY = (int) PosY; 
    
    // Ajouter un offset pour centrer le point au milieu de la tuile
    // (au lieu d'être en haut à gauche)
    pixelX += tailleTuile / 2;
    pixelY += tailleTuile / 2;
    
    // Calculer les ratios d'échelle
    float ratioX = (float) largeur / cartePixelW;
    float ratioY = (float) hauteur / cartePixelH;
    
    // Convertir les pixels vers les coordonnées de la minimap
    int pointX = Math.round(pixelX * ratioX);
    int pointY = Math.round(pixelY * ratioY);

    // DESSINER LE POINT SUR LA MINIMAP
    
    int rayonPoint = 3;
    int diametre = rayonPoint * 2;
    
    // Dessiner le cercle rempli
    gMini.setColor(couleur);
    gMini.fillOval(pointX - rayonPoint, pointY - rayonPoint, diametre, diametre);
    
    // Contour noir
    gMini.setColor(Color.BLACK);
    gMini.setStroke(new BasicStroke(1.5f));
    gMini.drawOval(pointX - rayonPoint, pointY - rayonPoint, diametre, diametre);
    
    gMini.dispose();
    return minimap;
}
  
    public int[][] getDecor() {
        return decor;
}
    
  public void afficherCoeurs(Graphics2D contexte, int nbr_life) {
    try {
        BufferedImage coeurPlein = ImageIO.read(getClass().getResource("images/Coeurplein32.png"));
        BufferedImage coeurVide = ImageIO.read(getClass().getResource("images/Coeurvide32.png"));
        
        int zoom = 2;
        int espacement = 30;
        int max_life = 3;
        
        // Afficher 3 cœurs
        for (int i = 0; i < nbr_life; i++) {
            contexte.drawImage(coeurPlein, 20 + (i * espacement*zoom), 50, 32 * zoom, 32 * zoom, null);
        }
        for (int i = nbr_life ; i < max_life; i++){
             contexte.drawImage(coeurVide, 20 + (i * espacement*zoom), 50, 32 * zoom, 32 * zoom, null);
        }
        
    } catch (IOException ex) {
        System.out.println("Erreur : image non trouvée");
    }
    }
  }
    
//Méthode pour savoir si on a une collision ou non 
//    public prochainetuileaccessible (double x, double y){
//        int Col = 1;
//        int n = 32 ;
//        int Xc = (int)Math.ceil(x/(double)n) ;
//        int Yc = (int)Math.ceil(y/(double)n) ;
//        if (decor[Xc][Yc] == 79){
//            Col = -1 ;
//        }
//        else {
//            Col = 1 ;
//        }    
//        
//        
//        
//    }
//}

 
//    public void rendu(Graphics2D contexte, int x , int y) {
//        // Parcours de chaque ligne de la carte
//        for (int i = 0; i < decor.length; i++) {
//            // Parcours de chaque colonne de la ligne courante
//            for (int j = 0; j < decor[i].length; j++) {
//
//                int numeroTuile;
//                 // Si hors limites on affiche une tuile de bordure (ici 0)
//                if (i < 0 || i >= decor.length || j < 0 || j >= decor[0].length) {
//                    numeroTuile = 0; // ← change cet index selon la tuile voulue
//                } 
//                else {
//                // Récupération de l'index de la tuile à dessiner
//                numeroTuile = decor[i][j];
//                }
//                // On introduit un facteur x pour effectuer un zoom sur la map 
//                int zoom = 3 ;
//                // Dessin de la tuile à la position (j*32, i*32) en pixels avec zoom
//                // S'assure que l'on dessine une tuile qui existe
//                if (numeroTuile >= 0 && numeroTuile < tuiles.length) {
//                    contexte.drawImage(tuiles[numeroTuile], zoom*tailleTuile * (j-x+10), zoom*tailleTuile * (i-y+5),tailleTuile*zoom, tailleTuile*zoom ,null);
//                }
//            }
//        }
//    }
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
