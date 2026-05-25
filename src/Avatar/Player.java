package Avatar;
import java.util.ArrayList;
import Tools.Coordinates;
import Tools.Hitbox;
import java.awt.Graphics2D;

public class Player extends Avatar {
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

    private double boundsMinX = 0, boundsMinY = 0;
    private double boundsMaxX = 1920, boundsMaxY = 1088;


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
        this.monsters = monsters != null ? monsters : new ArrayList<>();
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
    
     public void rendu(Graphics2D contexte, int displayWidth, int displayHeight) {
        contexte.drawImage(this.getImage(), 960, 480, displayWidth, displayHeight, null);
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
        double newX, newY;
        synchronized (position) {
            newX = position.getX();
            newY = position.getY();
        }

        if (this.toucheGauche)  newX -= this.speed * dt;
        if (this.toucheDroite)  newX += this.speed * dt;
        if (this.toucheBas)     newY += this.speed * dt;
        if (this.toucheHaut)    newY -= this.speed * dt;

        newX = Math.max(boundsMinX, Math.min(boundsMaxX, newX));
        newY = Math.max(boundsMinY, Math.min(boundsMaxY, newY));

        synchronized (position) {
            position.setX(newX);
            position.setY(newY);
        }
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

    public void onKeyPressed(int keyCode) {
        switch (keyCode) {
            case java.awt.event.KeyEvent.VK_LEFT,  java.awt.event.KeyEvent.VK_Q -> setToucheGauche(true);
            case java.awt.event.KeyEvent.VK_RIGHT, java.awt.event.KeyEvent.VK_D -> setToucheDroite(true);
            case java.awt.event.KeyEvent.VK_UP,    java.awt.event.KeyEvent.VK_Z -> setToucheHaut(true);
            case java.awt.event.KeyEvent.VK_DOWN,  java.awt.event.KeyEvent.VK_S -> setToucheBas(true);
        }
    }

    public void onKeyReleased(int keyCode) {
        switch (keyCode) {
            case java.awt.event.KeyEvent.VK_LEFT,  java.awt.event.KeyEvent.VK_Q -> setToucheGauche(false);
            case java.awt.event.KeyEvent.VK_RIGHT, java.awt.event.KeyEvent.VK_D -> setToucheDroite(false);
            case java.awt.event.KeyEvent.VK_UP,    java.awt.event.KeyEvent.VK_Z -> setToucheHaut(false);
            case java.awt.event.KeyEvent.VK_DOWN,  java.awt.event.KeyEvent.VK_S -> setToucheBas(false);
        }
    }
}
