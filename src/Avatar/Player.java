package Avatar;
import java.awt.Graphics2D;
import java.sql.SQLException;
import java.util.ArrayList;

import TileMapping.CollisionMap;
import Tools.Coordinates; // miel unique : gestion des erreurs SQL lors du vol
import Tools.Hitbox;
import multiplayer.DonneesJoueur;
import multiplayer.GestionnaireJoueurs; // miel unique : données des autres joueurs (position, hasHoney)

public class Player extends Avatar {
    private static final double TILE_SIZE = GameConstants.TILE_SIZE;

    // Ces drapeaux sont lus par un thread de mouvement et ecrits par Swing, donc ils doivent etre visibles partout.
    private volatile boolean toucheGauche, toucheDroite, toucheHaut, toucheBas;

    private final String name;
    private final Coordinates spawn;

    private volatile int lives = 3;
    private volatile boolean hasHoney = false;
    private volatile boolean isHarvesting = false;
    private volatile long harvestStartTime = 0;

    private final Hitbox hiveZone, spawnZone;
    private final ArrayList<Monsters> monsters;

    // GameConstants est un fichier qui contient toutes les variables importantes du jeu
    private double boundsMinX = 0, boundsMinY = 0;
    private double boundsMaxX = GameConstants.SCREEN_WIDTH, boundsMaxY = GameConstants.SCREEN_HEIGHT;


    private volatile boolean running = false;
    private Thread movementThread;

    private volatile long invincibleUntil = 0;
    private volatile boolean won = false;
    private volatile boolean gameOver = false;
    private CollisionMap collisionMap;

    // --- Unicité du pot de miel ---
    private volatile long stopMvtUntil = 0; // timestamp jusqu'auquel le joueur est immobilisé (0.5s après vol)
    private volatile ArrayList<DonneesJoueur> autresJoueurs = new ArrayList<>(); // snapshot DB des autres joueurs
    private GestionnaireJoueurs gestionnaire; // référence pour appeler volerMiel() en DB
    private int joueurId; // identifiant DB du joueur local
    

    public Player(
        double spawnX,
        double spawnY,
        double speed,
        Hitbox hitbox,
        Hitbox hiveZone,
        Hitbox spawnZone,
        ArrayList<Monsters> monsters,
        String name
    ) {
        super(new Coordinates(spawnX, spawnY), null, speed, hitbox);
        this.spawn = new Coordinates(spawnX, spawnY);
        this.name = name;
        this.hiveZone = hiveZone;
        this.spawnZone = spawnZone;
        this.monsters = monsters != null ? monsters : new ArrayList<>(); // si la liste de monstres est null, on en crée une vide pour éviter les NullPointerException
        this.toucheGauche = false; 
        this.toucheDroite = false;
        this.toucheBas = false;
        this.toucheHaut=false;

        if (this.hitbox != null) {
            this.hitbox.update(this.position);
        }
    }

    @Override
    public void startMovement() {
        if (running) return;
        running = true;

        movementThread = new Thread(() -> {
            long lastTime = System.currentTimeMillis();

            while (running) {
                long now = System.currentTimeMillis();
                double dt = (now - lastTime) / 1000.0;
                lastTime = now;

                miseAJour(dt,this.collisionMap);
                syncHitbox();
                updateHarvesting(now);
                handleMonsterCollisions(now);
                handlePlayerCollisions(now); // détection contact avec le porteur (collision)
                checkWinCondition();

                try { Thread.sleep(16); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; } // ~60 FPS
            }
        }, "player-movement");

        movementThread.start();
    }
    // on utilise les variables qui sont dans GameConstants.java pour unifier le code. 
     public void rendu(Graphics2D contexte, int displayWidth, int displayHeight) {
        contexte.drawImage(this.getImage(), GameConstants.LOCAL_PLAYER_SCREEN_X, GameConstants.LOCAL_PLAYER_SCREEN_Y, displayWidth, displayHeight, null);
    }

    @Override
    public void stopMovement() {
        // On coupe la boucle de mouvement et on reveille le thread s'il dort encore.
        running = false;
        if (movementThread != null) {
            movementThread.interrupt();
        }
    }


    
    public void miseAJour(double dt, CollisionMap map) {
        // Si le joueur vient de se faire voler le miel, on stop son mouvement pendant 0.5 seconde pour lui laisser le temps de réagir et éviter les vols en chaîne instantanés
        if (System.currentTimeMillis() < stopMvtUntil) return;
        int col = (int)(this.position.getX() / TILE_SIZE); // colonne de tuile
        int row = (int)(this.position.getY() / TILE_SIZE); // ligne de tuile
        double speedFactor = map.getSpeedFactor(col, row); // facteur de vitesse ralenti ou pas en fonction des tuiles (tuile 414)
        if (this.toucheGauche){
            double x = this.getPosition().getX();
            double newX= x - (1*this.speed *speedFactor * TILE_SIZE * dt);
            col = (int)(newX / TILE_SIZE);
            if (!map.isMur(col,row)){ // tuiles 415
                this.position.setX(newX);
            }
        }
        if (this.toucheDroite){
            double x = this.getPosition().getX();
            double newX= x+ (1*this.speed * speedFactor * TILE_SIZE * dt);
            col = (int)(newX / TILE_SIZE);
            if (!map.isMur(col,row)){
                this.position.setX(newX);
            }
        }
        if (this.toucheBas){
            double y = this.getPosition().getY();
            double newY = y+ (1*this.speed* speedFactor * TILE_SIZE * dt);
             row = (int)(newY / TILE_SIZE);
            if (!map.isMur(col,row)){
                this.position.setY(newY);
            }
        }
        if (this.toucheHaut){
            double y = this.getPosition().getY();
            double newY = y - (1*this.speed * speedFactor * TILE_SIZE * dt);
             row = (int)(newY / TILE_SIZE);
            if (!map.isMur(col,row)){
                this.position.setY(newY);
            }
        }
//        if (this.getPosition().getX()> GameConstants.SCREEN_WIDTH - GameConstants.PLAYER_SIZE){// collision avec le bord droit de la scene, taille de la hitbox
//            this.position.setX( GameConstants.SCREEN_WIDTH - GameConstants.PLAYER_SIZE);
//        }
//        if (this.getPosition().getX()<0){// collision avec le bord gauche de la scene
//            this.position.setX(0);
//        }   
//        if(this.getPosition().getY()> GameConstants.SCREEN_HEIGHT - GameConstants.PLAYER_SIZE){  // collision avec le bord bas de la scene
//            this.position.setY(GameConstants.SCREEN_HEIGHT - GameConstants.PLAYER_SIZE);
//        }
//        if (this.getPosition().getY()<0){// collision avec le bord haut de la scene
//            this.position.setY(0); 
//        }
 // les lignes précédentes étaient au début quand on avait pas les collisions
        }
    
    public void setCollisionMap(CollisionMap map) {
        this.collisionMap = map;
    }

    // --- Setters miel ---

    // Reçoit le snapshot DB des autres joueurs à chaque cycle de sync (depuis FenetreDeJeu). 
    public void setAutresJoueurs(ArrayList<DonneesJoueur> joueurs) {
        this.autresJoueurs = joueurs != null ? joueurs : new ArrayList<>();
    }

    //Injecte le gestionnaire DB et l'id local pour permettre l'appel volerMiel(). 
    public void setGestionnaire(GestionnaireJoueurs gestionnaire, int joueurId) {
        this.gestionnaire = gestionnaire;
        this.joueurId = joueurId;
    }

    /**
     * Appelé par FenetreDeJeu quand le thread de synchronisation détecte que le miel a été volé en DB.
     * Retire le miel localement et applique le stop mouvement de 0.5 seconde.
     */
    public void onMielVole() {
        hasHoney = false;
        isHarvesting = false;
        harvestStartTime = 0;
        stopMvtUntil = System.currentTimeMillis() + 500;
    }
        


    private void syncHitbox() {
        if (hitbox == null) return;
        synchronized (position) {
            hitbox.update(position);
        }
    }

    private void updateHarvesting(long now) {
        if (hiveZone == null) return;
        // On ne peut pas récolter si un autre joueur porte déjà le miel
        for (DonneesJoueur j : autresJoueurs) {
            if (j.hasHoney) {
                isHarvesting = false;
                harvestStartTime = 0;
                return;
            }
        }
        if (overlaps(hitbox, hiveZone)) {
            if (!isHarvesting) {
                isHarvesting = true;
                harvestStartTime = now;
            } else if (now - harvestStartTime >= 3000) {
                
                // évite que deux joueurs qui terminent leur timer en même temps aient tous les deux le miel
                try {
                    if (gestionnaire != null && gestionnaire.recolterMiel(joueurId)) {
                        hasHoney = true;
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                isHarvesting = false;
                harvestStartTime = 0;
            }
        } else {
            isHarvesting = false;
            harvestStartTime = 0;
        }
    }

    private void handleMonsterCollisions(long now) {
        if (monsters == null || now < invincibleUntil) return;
        if (spawnZone != null && overlaps(hitbox, spawnZone)) return;
        for (Monsters monster : monsters) {
            if (overlaps(hitbox, monster.getHitbox())) {
                lives = Math.max(0, lives - 1);
                // Si on avait le miel, on le remet à 0 explicitement en DB pour que le pot réapparaisse
                if (hasHoney && gestionnaire != null) {
                    try { gestionnaire.perdreLeHmiel(joueurId); } catch (SQLException ex) { ex.printStackTrace(); }
                }
                hasHoney = false;
                isHarvesting = false;
                harvestStartTime = 0;

                synchronized (position) {
                    position.setX(spawn.getX());
                    position.setY(spawn.getY());
                }
                syncHitbox();

                invincibleUntil = now + 1000;

                if (lives == 0) {
                    gameOver = true;
                    running = false;
                }
                break;
            }
        }
    }

    /**
     * Vérifie si le joueur local entre en contact avec le porteur du miel.
     * Si oui, tente un vol atomique en DB. En cas de succès :
     * - le joueur local prend le miel
     * - il devient invincible pendant 1 seconde
     * Le porteur sera stuné côté FenetreDeJeu via onMielVole() quand le sync DB détectera le changement.
     */
    private void handlePlayerCollisions(long now) {
        // Pas de vol si on a déjà le miel, si on est invincible, ou si le gestionnaire n'est pas injecté
        if (hasHoney || now < invincibleUntil || gestionnaire == null) return;
        ArrayList<DonneesJoueur> snapshot = autresJoueurs;
        for (DonneesJoueur j : snapshot) {
            if (!j.hasHoney) continue;
            // Le porteur est protégé sur sa propre zone de spawn : vol impossible là-bas
            Hitbox porteurSpawnZone = new Hitbox(
                new Coordinates(j.spawnX, j.spawnY),
                GameConstants.SPAWN_ZONE_SIZE, GameConstants.SPAWN_ZONE_SIZE
            );
            Hitbox porteurHitbox = new Hitbox(
                new Coordinates(j.x, j.y),
                GameConstants.PLAYER_SIZE, GameConstants.PLAYER_SIZE
            );
            if (overlaps(porteurHitbox, porteurSpawnZone)) continue; // porteur intouchable sur son spawn
            if (!overlaps(hitbox, porteurHitbox)) continue; // pas de contact
            try {
                boolean success = gestionnaire.volerMiel(joueurId, j.id);
                if (success) {
                    hasHoney = true;
                    invincibleUntil = now + 1000; // 1 seconde d'invincibilité après le vol
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            break; // un seul vol par tick
        }
    }

    private void checkWinCondition() {
        if (spawnZone == null) return;
        if (hasHoney && overlaps(hitbox, spawnZone)) {
            hasHoney = false;
            won = true;
            running = false;
        }
    }

    //fonction interne qui calcule si deux hitbox se chevauchent
    private boolean overlaps(Hitbox a, Hitbox b) {
        return a.getX() < b.getX() + b.getWidth()
            && a.getX() + a.getWidth() > b.getX()
            && a.getY() < b.getY() + b.getHeight()
            && a.getY() + a.getHeight() > b.getY();
    }

  


    public double getX() { synchronized (position) { return position.getX(); } }
    public double getY() { synchronized (position) { return position.getY(); } }
//    public double getXTiles() { synchronized (position) { return position.getX()/TILE_SIZE; }}
//    public double getYTiles() { synchronized (position) { return position.getY()/TILE_SIZE; }}
    public double getMaxSpeed() { return speed; }
    public String getName() { return name; }
    public Coordinates getSpawn() { return new Coordinates(spawn.getX(), spawn.getY()); }
    public int getLives() { return lives; }
    public boolean hasHoney() { return hasHoney; }
    public boolean isHarvesting() { return isHarvesting; }
    public long getHarvestStartTime() { return harvestStartTime; }
    public long getInvincibleUntil() { return invincibleUntil; }
    public boolean isRunning() { return running; }
    public boolean isWon() { return won; }
    public boolean isGameOver() { return gameOver; }



    
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

    public void setMovementBounds(int minX, int minY, double maxX, double maxY) {
        this.boundsMinX = minX;
        this.boundsMinY = minY;
        this.boundsMaxX = maxX;
        this.boundsMaxY = maxY;
    }

   
}
