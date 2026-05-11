package Avatar;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


import Tools.Coordinates;
import Tools.Hitbox;
import java.awt.Graphics2D;

public class Player extends Avatar {
    private boolean toucheGauche, toucheDroite, toucheHaut, toucheBas;
    private int score;

    private final String name;
    private final Coordinates spawn;

    private volatile int lives = 3;
    private volatile boolean hasHoney = false;
    private volatile boolean isHarvesting = false;
    private volatile long harvestStartTime = 0;

    private final Hitbox hiveZone, spawnZone;
    private final ArrayList<Monsters> monsters;


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
//                updateHarvesting(now);
//                handleMonsterCollisions(now);
//                checkWinCondition();

                try { Thread.sleep(16); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; } // ~60 FPS
            }
        }, "player-movement");

        movementThread.start();
    }
    
     public void rendu(Graphics2D contexte) {
        contexte.drawImage(this.getImage(), 960, 544 , null);
    }

    @Override
    public void stopMovement() {
        running = false;
        if (movementThread != null) {
            movementThread.interrupt();
        }
    }


    public void miseAJour(double dt) {
        if (this.toucheGauche){
            double x = this.getPosition().getX();
            x-= (1*this.speed * dt);
            synchronized (position){
                position.setX(x);}
        }
        if (this.toucheDroite){
            double x = this.getPosition().getX();
            x+= (1*this.speed * dt);
            synchronized (position){
                position.setX(x);}
        }
        if (this.toucheBas){
            double y = this.getPosition().getY();
            y+= (1*this.speed * dt);
            synchronized (position){
                position.setY(y);}
        }
        if (this.toucheHaut){
            double y = this.getPosition().getY();
            y-= (1*this.speed*dt);
            synchronized (position){
                position.setY(y);}
        }
        if (this.getPosition().getX()> 1920 - this.getImage().getWidth()){// collision avec le bord droit de la scene
            synchronized (position){
                position.setX( 1920 - this.getImage().getWidth());
            }
        }
        if (this.getPosition().getX()<0){// collision avec le bord gauche de la scene
            synchronized (position){
                position.setX(0);}
        }   
        if(this.getPosition().getY()> 1088 - this.getImage().getHeight()){  // collision avec le bord bas de la scene
            synchronized (position){
                position.setY(1088 - this.getImage().getHeight());}
           
        }
        if (this.getPosition().getY()<0){// collision avec le bord haut de la scene
            synchronized (position){
                position.setY(0);}
                
        }
        System.out.println("X: " + getX());
        
       // if isAccessible
       
        }
        


    private void syncHitbox() {
        synchronized (position) {
            hitbox.update(position);
        }
    }
//
//    private void updateHarvesting(long now) {
//        if (overlaps(hitbox, hiveZone)) {
//            if (!isHarvesting) {
//                isHarvesting = true;
//                harvestStartTime = now;
//            } else if (now - harvestStartTime >= 3000) { // 3s to harvest
//                hasHoney = true;
//                isHarvesting = false;
//                harvestStartTime = 0;
//            }
//        } else {
//            isHarvesting = false;
//            harvestStartTime = 0;
//        }
//    }

//    private void handleMonsterCollisions(long now) {
//
//        for (Monsters monster : monsters) {
//
//            if (overlaps(hitbox, monster.getHitbox())) {
//                lives = Math.max(0, lives - 1);
//                hasHoney = false;
//                isHarvesting = false;
//                harvestStartTime = 0;
//
//                synchronized (position) { // redirect to spawn when player is hit by a monster
//                    position.setX(spawn.getX());
//                    position.setY(spawn.getY());
//                }
//                syncHitbox();
//
//                invincibleUntil = now + 1000;
//
//                if (lives == 0) {
//                    gameOver = true;
//                    running = false;
//                }
//                break;
//            }
//        }
//    }

//    private void checkWinCondition() {
//
//        if (overlaps(hitbox, spawnZone)) {
//            hasHoney = false;
//            won = true;
//            running = false;
//        }
//    }

//    private boolean overlaps(Hitbox a, Hitbox b) {
//        return a.getX() < b.getX() + b.getWidth()
//            && a.getX() + a.getWidth() > b.getX()
//            && a.getY() < b.getY() + b.getHeight()
//            && a.getY() + a.getHeight() > b.getY();
//    }


<<<<<<< HEAD
=======
            if (overlaps(hitbox, monster.getHitbox())) {
                lives = Math.max(0, lives - 1);
                hasHoney = false;
                isHarvesting = false;
                harvestStartTime = 0;

                synchronized (position) { // redirect to spawn when player is hit by a monster
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

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
>>>>>>> 02ad3fc234778f44982870483a89ab4b3a272ee2

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
    
}
