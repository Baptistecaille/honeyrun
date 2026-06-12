/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Avatar;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import TileMapping.Carte;
import TileMapping.CollisionMap;
import Tools.Coordinates;
import Tools.Hitbox;
import multiplayer.DonneesJoueur;
import multiplayer.DonneesMonstre;

/**
 *
 * @author alamas, cpoussie, bcaillerie, elehec
 */
public class Jeu {

    public static final int TILE_SIZE = GameConstants.TILE_SIZE;
    public static final int MAP_ZOOM = GameConstants.MAP_ZOOM;
    public static final int CAMERA_OFFSET_TILES_X = GameConstants.CAMERA_OFFSET_TILES_X;
    public static final int CAMERA_OFFSET_TILES_Y = GameConstants.CAMERA_OFFSET_TILES_Y;

    protected BufferedImage decor;
    protected int score;
    protected Player player;
    protected Honey honey;
    private ArrayList<Monsters> monsters;
    private Carte calque1;
    private Carte calque2;
    private Carte calque3;
    private BufferedImage minimap;
    private CollisionMap collisionMap;
    private boolean monstresAutoritaires = true;

    // Liste des autres joueurs pour savoir qui porte le miel et afficher le pot au-dessus du porteur
    private volatile ArrayList<DonneesJoueur> autresJoueurs = new ArrayList<>();
    

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
        this(80, 60, "Player1", 0);
    }

    public Jeu(double playerSpawnX, double playerSpawnY, String playerName, int skinId) {
        this.calque1 = new Carte("src/TileMapping/Calque11920.txt");
        this.calque2 = new Carte("src/TileMapping/Calque221920.txt");
        this.honey =new Honey();
        this.collisionMap = new CollisionMap("src/TileMapping/Calque111920_1.txt");
        //this.calque3 = new Carte("src/TileMapping/Calque31920.txt");
        
        this.score = 0;

        // On accepte soit la ressource empaquetee, soit le fichier present dans le projet pour rester runnable en dev.
        BufferedImage sprite = chargerSprite(skinId);
        // Chargement du sprite des monstres depuis son chemin direct
        BufferedImage monsterSprite = null;
        try {
            monsterSprite = redimensionner(ImageIO.read(new File("src/resources/Abeille.png")), GameConstants.SPRITE_SIZE, GameConstants.SPRITE_SIZE);
        }
        catch (IOException ex) {
                monsterSprite = new BufferedImage(GameConstants.SPRITE_SIZE, GameConstants.SPRITE_SIZE, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = monsterSprite.createGraphics();
                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, GameConstants.SPRITE_SIZE, GameConstants.SPRITE_SIZE);
                g2d.dispose();
        }

        this.monsters = new ArrayList<>();
        double[][] monsterSpawns = GameConstants.MONSTER_SPAWNS;
        for (double[] s : monsterSpawns) {
            Hitbox mHitbox = new Hitbox(new Coordinates(s[0], s[1]), GameConstants.MONSTER_SIZE, GameConstants.MONSTER_SIZE);
            Monsters m = new Monsters(s[0], s[1], GameConstants.MONSTER_SPEED, mHitbox);
            m.setChaseSpeed(GameConstants.MONSTER_CHASE_SPEED);
            m.setMovementBounds(0, 0, GameConstants.SCREEN_WIDTH - GameConstants.MONSTER_SIZE, GameConstants.SCREEN_HEIGHT - GameConstants.MONSTER_SIZE);
            m.startMovement();
            m.setCollisionMap(collisionMap);
            m.setColor(Color.BLACK);
            m.setImage(monsterSprite);
            this.monsters.add(m);
        }

        Hitbox hiveZone = new Hitbox(new Coordinates(GameConstants.HIVE_X, GameConstants.HIVE_Y), GameConstants.HIVE_SIZE, GameConstants.HIVE_SIZE);
        Hitbox spawnZone = new Hitbox(new Coordinates(playerSpawnX, playerSpawnY), GameConstants.SPAWN_ZONE_SIZE, GameConstants.SPAWN_ZONE_SIZE);

        Hitbox playerHitbox = new Hitbox(new Coordinates(playerSpawnX, playerSpawnY), GameConstants.PLAYER_SIZE, GameConstants.PLAYER_SIZE);
        Player P1 = new Player(playerSpawnX, playerSpawnY, GameConstants.PLAYER_SPEED, playerHitbox, hiveZone, spawnZone, this.monsters, playerName);
        P1.setImage(sprite);
        P1.setMovementBounds(0, 0, GameConstants.SCREEN_WIDTH - playerHitbox.getWidth(), GameConstants.SCREEN_HEIGHT - playerHitbox.getHeight());
        P1.startMovement();
        this.player = P1;
        this.player.setCollisionMap(this.collisionMap);
        this.minimap = this.calque2.genererImageMiniMapAvecPointTuile(300, 225, this.player.getX(), this.player.getY(),Color.BLACK);
        
    }

    private BufferedImage chargerSprite(int skinId) {
        String[] noms = {null, "Araignee", "Mantereligieuse", "Scarabe", "Criquet"};
        // Si le skinId est en dehors de l'intervalle [1, 4], on utilise "Mantereligieuse" comme sprite par défaut pour éviter les erreurs d'index et avoir un sprite visible.
        String nomFichier = (skinId >= 1 && skinId < noms.length) ? noms[skinId] : "Mantereligieuse"; 

        try {
            var resource = getClass().getResource("/resources/" + nomFichier + ".png");
            if (resource != null) {
                BufferedImage sprite = ImageIO.read(resource);
                if (sprite != null) {
                    return redimensionner(sprite, GameConstants.SPRITE_SIZE, GameConstants.SPRITE_SIZE);
                }
            }

            File fallback = new File("src/resources/" + nomFichier + ".png");
            if (fallback.exists()) {
                BufferedImage sprite = ImageIO.read(fallback);
                if (sprite != null) {
                    return redimensionner(sprite, GameConstants.SPRITE_SIZE, GameConstants.SPRITE_SIZE);
                }
            }
        } catch (IOException ex) {
            System.err.println("Erreur lors du chargement du sprite : " + ex.getMessage());
        }
        BufferedImage placeholder = new BufferedImage(GameConstants.SPRITE_SIZE, GameConstants.SPRITE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = placeholder.createGraphics();
        g2d.setColor(Color.MAGENTA);
        g2d.fillRect(0, 0, GameConstants.SPRITE_SIZE, GameConstants.SPRITE_SIZE);
        g2d.dispose();
        return placeholder;
    }
        private void renduMiniMap(Graphics2D contexte, int largeurEcran, int hauteurEcran) {
        if (minimap == null) return;
        int x = largeurEcran - minimap.getWidth()  - 15;
        int y = hauteurEcran - minimap.getHeight() - 830;
        contexte.drawImage(minimap, x, y, null);
    }

    public void rendu(Graphics2D contexte,int largeurEcran, int hauteurEcran) {
        // On repart d'une frame propre avant de redessiner la camera.
        contexte.setBackground(new Color(0, 0, 0, 0));
        contexte.clearRect(0, 0, largeurEcran, hauteurEcran);

        double a = this.player.getPosition().getX();
        double b = this.player.getPosition().getY();
        int Xp = (int) (a / (double) TILE_SIZE) ; //On passe n en double pour pouvoir diviser a et on récupère un int pour avoir le quotient
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
        // On vérifie si un autre joueur porte le miel
        boolean autreJoueurALeMiel = false;
        for (DonneesJoueur j : autresJoueurs) {
            if (j.hasHoney) { autreJoueurALeMiel = true; break; }
        }

        if (this.player.hasHoney()) {
            // Si le joueur local porte le miel : on l'affiche en mini à côté de son sprite
            this.honey.setX(GameConstants.LOCAL_PLAYER_SCREEN_X + GameConstants.PLAYER_SIZE);
            this.honey.setY(GameConstants.LOCAL_PLAYER_SCREEN_Y - GameConstants.PLAYER_SIZE / 2);
            this.honey.rendu(contexte);
        } else if (!autreJoueurALeMiel) {
            // Personne n'a le miel, on met le pot en position centrale sur la carte
            this.honey.setX(worldToScreenX(GameConstants.HIVE_X));
            this.honey.setY(worldToScreenY(GameConstants.HIVE_Y));
            this.honey.rendu(contexte);
        }
        // Si un autre joueur porte le miel : pot central non affiché
        this.calque2.afficherCoeurs(contexte, this.player.getLives());
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
            int w = (int) Math.round(monster.getHitbox().getWidth()) * MAP_ZOOM; // on applique le zoom à la taille du hitbox pour que le sprite corresponde à la hitbox
            int h = (int) Math.round(monster.getHitbox().getHeight()) * MAP_ZOOM; // même chose pour la hauteur
            // Si le monstre a un sprite, on l'affiche ; sinon on replie sur un rectangle de couleur.
            if (monster.getImage() != null) {
                contexte.drawImage(monster.getImage(), x, y, w, h, null); // on dessine le sprite du monstre
            } else {
                contexte.setColor(monster.getColor() != null ? monster.getColor() : Color.BLACK); // si le monstre n'a pas d'image et pas de couleur, on utilise le noir par défaut
                contexte.fillRect(x, y, w, h);
            }
        }
    }

    public void miseAJour() {
        // Le mouvement du joueur est géré par son thread interne (startMovement)
        this.calque1.miseAJour();
        this.calque2.miseAJour();
        this.minimap = this.calque2.genererImageMiniMapAvecPointTuile(300, 225, this.player.getX(), this.player.getY(),Color.BLACK);
       
       
        if (this.player.hasHoney()){
            this.honey.miseAJourMini();}
        else{
            this.honey.miseAJourMaxi();
                    }
        
        //this.calque3.miseAJour();
    }
   

    public Player getPlayer() {
        return this.player;
    }

    // Met à jour la liste des autres joueurs pour savoir qui porte le miel
    public void setAutresJoueurs(ArrayList<DonneesJoueur> joueurs) {
        this.autresJoueurs = joueurs != null ? joueurs : new ArrayList<>();
    }

    // Retourne le sprite du pot de miel pour l'afficher en mini au-dessus du porteur dans FenetreDeJeu
    public BufferedImage getHoneySprite() {
        return honey.getSprite();
    }

    public void mettreAJourJoueursPourMonstres(ArrayList<DonneesJoueur> joueurs) {
        ArrayList<DonneesJoueur> snapshot = joueurs != null ? new ArrayList<>(joueurs) : new ArrayList<>(); // si la liste de joueurs est null, on en crée une vide pour éviter les NullPointerException
        if (monsters != null) {
            for (Monsters monster : monsters) {
                monster.setJoueurs(snapshot);
            }
        }
    }

    public synchronized void setMonstresAutoritaires(boolean autoritaires) {
        if (this.monstresAutoritaires == autoritaires) {
            return;
        }
        this.monstresAutoritaires = autoritaires;
        if (monsters == null) {
            return;
        }
        for (Monsters monster : monsters) {
            if (autoritaires) {
                monster.startMovement();
            } else {
                monster.stopMovement();
            }
        }
    }

    public ArrayList<DonneesMonstre> creerDonneesMonstres(List<DonneesMonstre> references) {
        ArrayList<DonneesMonstre> donnees = new ArrayList<>();
        if (monsters == null || references == null) {
            return donnees;
        }
        int count = Math.min(monsters.size(), references.size());
        for (int i = 0; i < count; i++) {
            Monsters monster = monsters.get(i);
            DonneesMonstre reference = references.get(i);
            donnees.add(new DonneesMonstre(reference.id, monster.getX(), monster.getY()));
        }
        return donnees;
    }

    public void appliquerDonneesMonstres(List<DonneesMonstre> donnees) {
        if (monsters == null || donnees == null) {
            return;
        }
        int count = Math.min(monsters.size(), donnees.size());
        for (int i = 0; i < count; i++) {
            DonneesMonstre donnee = donnees.get(i);
            monsters.get(i).setPosition(donnee.x, donnee.y);
        }
    }

    public void stopMonstres() {
        if (monsters != null) {
            for (Monsters m : monsters) {
                m.stopMovement();
            }
        }
    }
}
