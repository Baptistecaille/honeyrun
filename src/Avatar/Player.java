package Avatar;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import Tools.Coordinates;
import Tools.Hitbox;

public class Player extends Avatar {

    private final String name;
    private final Coordinates spawn;

    private volatile int lives = 3;
    private volatile boolean hasHoney = false;
    private volatile boolean isHarvesting = false;
    private volatile long harvestStartTime = 0;

    private final Hitbox hiveZone, spawnZone;
    private final ArrayList<Monsters> monsters;

    private final Set<Integer> keysPressed = Collections.synchronizedSet(new HashSet<>()); 

    private double minX = 0.0; // Set bounderies for the player not to move outside the window or play area.
    private double minY = 0.0;
    private double maxX = Double.POSITIVE_INFINITY;
    private double maxY = Double.POSITIVE_INFINITY;

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

                updatePosition(dt);
                syncHitbox();
                updateHarvesting(now);
                handleMonsterCollisions(now);
                checkWinCondition();

                try { Thread.sleep(16); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
            }
        }, "player-movement");

        movementThread.start();
    }

    @Override
    public void stopMovement() {
        running = false;
        if (movementThread != null) {
            movementThread.interrupt();
        }
    }

    public void onKeyPressed(int keyCode) {
        if (isMovementKey(keyCode)) keysPressed.add(keyCode);
    }

    public void onKeyReleased(int keyCode) {
        if (isMovementKey(keyCode)) keysPressed.remove(keyCode);
    }

    private boolean isMovementKey(int keyCode) {
        return keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT
            || keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN;
    }

    private void updatePosition(double dt) {
        double dirX = 0.0;
        double dirY = 0.0;

        synchronized (keysPressed) {
            if (keysPressed.contains(KeyEvent.VK_LEFT))  dirX -= 1.0;
            if (keysPressed.contains(KeyEvent.VK_RIGHT)) dirX += 1.0;
            if (keysPressed.contains(KeyEvent.VK_UP))    dirY -= 1.0;
            if (keysPressed.contains(KeyEvent.VK_DOWN))  dirY += 1.0;
        }

        if (dirX != 0.0 || dirY != 0.0) {
            double magnitude = Math.hypot(dirX, dirY);
            dirX /= magnitude;
            dirY /= magnitude;
        }

        synchronized (position) {
            double newX = position.getX() + (dirX * speed * dt);
            double newY = position.getY() + (dirY * speed * dt);
            position.setX(clamp(newX, minX, maxX));
            position.setY(clamp(newY, minY, maxY));
        }
    }

    private void syncHitbox() {
        if (hitbox == null) return;
        synchronized (position) {
            hitbox.update(position);
        }
    }

    private void updateHarvesting(long now) {
        if (hasHoney || hitbox == null || hiveZone == null) {
            isHarvesting = false;
            harvestStartTime = 0;
            return;
        }

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
        if (hitbox == null || now <= invincibleUntil) return;

        for (Monsters monster : monsters) {
            if (monster == null || monster.getHitbox() == null) continue;

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
        if (gameOver || !hasHoney || hitbox == null || spawnZone == null) return;

        if (overlaps(hitbox, spawnZone)) {
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

    public void setMovementBounds(double minX, double minY, double maxX, double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);

        synchronized (position) {
            position.setX(clamp(position.getX(), this.minX, this.maxX));
            position.setY(clamp(position.getY(), this.minY, this.maxY));
        }
        syncHitbox();
    }
}
