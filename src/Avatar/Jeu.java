/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Avatar;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Color;

import javax.imageio.ImageIO;

import TileMapping.Carte;
import Tools.Coordinates;
import Tools.Hitbox;

/**
 *
 * @author alamas
 */
public class Jeu {

    public static final int TILE_SIZE = 32;
    public static final int MAP_ZOOM = 3;
    public static final int CAMERA_OFFSET_TILES_X = 10;
    public static final int CAMERA_OFFSET_TILES_Y = 5;
    private static final double MONSTER_SPEED = 32.0;
    private static final double MONSTER_SIZE = 32.0;

    protected BufferedImage decor;
    protected int score;
    protected Player player;
    private ArrayList<Monsters> monsters;
    private Carte calque1;
    private Carte calque2;
    private Carte calque3;
    private BufferedImage minimap;

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

    public Jeu() {
        this(80, 60, "Player1");
    }

    public Jeu(double playerSpawnX, double playerSpawnY, String playerName) {
        this.calque1 = new Carte("src/TileMapping/Calque11920.txt");
        this.calque2 = new Carte("src/TileMapping/Calque221920.txt");
        //this.calque3 = new Carte("src/TileMapping/Calque31920.txt");
        this.minimap = this.calque2.genererImageMiniMap(300, 225);
        this.score = 0;

        // On accepte soit la ressource empaquetee, soit le fichier present dans le projet pour rester runnable en dev.
        BufferedImage sprite = chargerSprite();

        this.monsters = new ArrayList<>();
        double[][] monsterSpawns = {{384, 288}, {160, 160}, {608, 416}};
        for (double[] s : monsterSpawns) {
            Hitbox mHitbox = new Hitbox(new Coordinates(s[0], s[1]), MONSTER_SIZE, MONSTER_SIZE);
            Monsters m = new Monsters(s[0], s[1], MONSTER_SPEED, mHitbox);
            m.setMovementBounds(0, 0, 1920 - MONSTER_SIZE, 1088 - MONSTER_SIZE);
            m.startMovement();
            m.setColor(Color.BLACK);
            this.monsters.add(m);
        }

        Hitbox hiveZone = new Hitbox(new Coordinates(912, 496), 96, 96);
        Hitbox spawnZone = new Hitbox(new Coordinates(playerSpawnX, playerSpawnY), 96, 96);

        Hitbox playerHitbox = new Hitbox(new Coordinates(playerSpawnX, playerSpawnY), 32.0, 32.0);
        Player P1 = new Player(playerSpawnX, playerSpawnY, 200, playerHitbox, hiveZone, spawnZone, this.monsters, playerName);
        P1.setImage(sprite);
        P1.setMovementBounds(0, 0, 1920 - playerHitbox.getWidth(), 1088 - playerHitbox.getHeight());
        P1.startMovement();
        this.player = P1;
    }

    private BufferedImage chargerSprite() {
        try {
            // Premier essai: chargement depuis le classpath quand l'application est lancee depuis le jar.
            var resource = getClass().getResource("/resources/Mantereligieuse.png");
            if (resource != null) {
                BufferedImage sprite = ImageIO.read(resource);
                if (sprite != null) {
                    return redimensionner(sprite, 96, 96);
                }
            }

            // Fallback utile pendant le dev quand on execute depuis la racine du projet.
            File fallback = new File("src/resources/Mantereligieuse.png");
            if (fallback.exists()) {
                BufferedImage sprite = ImageIO.read(fallback);
                if (sprite != null) {
                    return redimensionner(sprite, 96, 96);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Jeu.class.getName()).log(Level.SEVERE, null, ex);
        }

        BufferedImage placeholder = new BufferedImage(96, 96, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = placeholder.createGraphics();
        g2d.setColor(Color.MAGENTA);
        g2d.fillRect(0, 0, 96, 96);
        g2d.dispose();
        return placeholder;
    }
        private void renduMiniMap(Graphics2D contexte, int largeurEcran, int hauteurEcran) {
        if (minimap == null) return;
        int x = largeurEcran - minimap.getWidth()  - 15;
        int y = hauteurEcran - minimap.getHeight() - 15;
        contexte.drawImage(minimap, x, y, null);
    }

    public void rendu(Graphics2D contexte,int largeurEcran, int hauteurEcran) {
        // On repart d'une frame propre avant de redessiner la camera.
        contexte.setBackground(new Color(0, 0, 0, 0));
        contexte.clearRect(0, 0, largeurEcran, hauteurEcran);

        double a = this.player.getPosition().getX();
        double b = this.player.getPosition().getY();
        int Xp = (int) (a / (double) TILE_SIZE) ;               //On passe n en double pour pouvoir diviser a et on récupère un int pour avoir le quotient
        int Yp = (int)(b / (double) TILE_SIZE) ;
        this.calque1.rendu(contexte, Xp, Yp); // dessiné en premier (fond)
        this.calque2.rendu(contexte, Xp, Yp); // 2 ème calque
        //this.calque3.rendu(contexte, Xp, Yp);// 3 ème calque
                                    //1.Rendu du décor
        //2.Rendu des sprites
        this.renduMonstres(contexte);
        this.player.rendu(contexte,
            (int)(player.getHitbox().getWidth()  * MAP_ZOOM),
            (int)(player.getHitbox().getHeight() * MAP_ZOOM));
        renduMiniMap(contexte, largeurEcran, hauteurEcran);

    }

    public int worldToScreenX(double worldX) {
        int cameraTileX = (int) (this.player.getPosition().getX() / (double) TILE_SIZE);
        return (int) Math.round(MAP_ZOOM * (worldX - cameraTileX * TILE_SIZE + CAMERA_OFFSET_TILES_X * TILE_SIZE));
    }

    public int worldToScreenY(double worldY) {
        int cameraTileY = (int) (this.player.getPosition().getY() / (double) TILE_SIZE);
        return (int) Math.round(MAP_ZOOM * (worldY - cameraTileY * TILE_SIZE + CAMERA_OFFSET_TILES_Y * TILE_SIZE));
    }

    private void renduMonstres(Graphics2D contexte) {
        if (this.monsters == null) {
            return;
        }

        // Meme repere que Carte.rendu: les monstres gardent une position monde, puis la camera tile les projette a l'ecran.
        for (Monsters monster : this.monsters) {
            int x = worldToScreenX(monster.getX());
            int y = worldToScreenY(monster.getY());
            int w = (int) Math.round(monster.getHitbox().getWidth());
            int h = (int) Math.round(monster.getHitbox().getHeight());
            contexte.setColor(monster.getColor() != null ? monster.getColor() : Color.BLACK);
            contexte.fillRect(x, y, w * MAP_ZOOM, h * MAP_ZOOM);
        }
    }

    public void miseAJour() {
        // Le mouvement du joueur est géré par son thread interne (startMovement)
        this.calque1.miseAJour();
        this.calque2.miseAJour();
        //this.calque3.miseAJour();
    }

    public Player getPlayer() {
        return this.player;
    }

    public void stopMonstres() {
        if (monsters != null) {
            for (Monsters m : monsters) {
                m.stopMovement();
            }
        }
    }
}
