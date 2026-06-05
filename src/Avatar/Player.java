package Avatar;
import java.awt.Graphics2D;
import java.util.ArrayList;

import Tools.Coordinates;
import Tools.Hitbox;

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

                miseAJour(dt);
                syncHitbox();
                updateHarvesting(now);
                handleMonsterCollisions(now);
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


    
    public void miseAJour(double dt) {
        if (this.toucheGauche){
            double x = this.getPosition().getX();
            x-= (1*this.speed * TILE_SIZE * dt);
            this.position.setX(x);
        }
        if (this.toucheDroite){
            double x = this.getPosition().getX();
            x+= (1*this.speed * TILE_SIZE * dt);
            this.position.setX(x);
        }
        if (this.toucheBas){
            double y = this.getPosition().getY();
            y+= (1*this.speed * TILE_SIZE * dt);
            this.position.setY(y);
        }
        if (this.toucheHaut){
            double y = this.getPosition().getY();
            y-= (1*this.speed * TILE_SIZE * dt);
            this.position.setY(y);
        }
        if (this.getPosition().getX()> GameConstants.SCREEN_WIDTH - GameConstants.PLAYER_SIZE){// collision avec le bord droit de la scene, taille de la hitbox
            this.position.setX( GameConstants.SCREEN_WIDTH - GameConstants.PLAYER_SIZE);
        }
        if (this.getPosition().getX()<0){// collision avec le bord gauche de la scene
            this.position.setX(0);
        }   
        if(this.getPosition().getY()> GameConstants.SCREEN_HEIGHT - GameConstants.PLAYER_SIZE){  // collision avec le bord bas de la scene
            this.position.setY(GameConstants.SCREEN_HEIGHT - GameConstants.PLAYER_SIZE);
        }
        if (this.getPosition().getY()<0){// collision avec le bord haut de la scene
            this.position.setY(0); 
        }
 
        
       // if isAccessible
       
        }
        


    private void syncHitbox() {
        if (hitbox == null) return;
        synchronized (position) {
            hitbox.update(position);
        }
    }

    private void updateHarvesting(long now) {
        if (hiveZone == null) return;
        if (overlaps(hitbox, hiveZone)) {
            if (!isHarvesting) {
                isHarvesting = true;
                harvestStartTime = now;
            } else if (now - harvestStartTime >= 3000) {
                hasHoney = true;
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
