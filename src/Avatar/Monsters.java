package Avatar;

import java.awt.Color;
import java.util.Random;

import Tools.Coordinates;
import Tools.Hitbox;

public class Monsters extends Avatar {

    private static final double TILE_SIZE = 32.0;

    private Coordinates velocity;
    private Coordinates heading;
    private Color color;
    private double stepAccumulator = 0.0;

    private double minX = 0.0; // a revoir 
    private double minY = 0.0;
    private double maxX = Double.POSITIVE_INFINITY;
    private double maxY = Double.POSITIVE_INFINITY;

    private volatile boolean running = false;
    private Thread movementThread;
    private final Random random = new Random();

    public Monsters(double x, double y, double speed, Hitbox hitbox) {
        super(new Coordinates(snapToTileValue(x), snapToTileValue(y)), null, speed, hitbox);
        this.velocity = new Coordinates(0, 0);
        this.heading = new Coordinates(0, 0);
        if (this.hitbox != null) {
            this.hitbox.update(this.position);
        }
    }

    @Override
    public void startMovement() {
        if (running) {
            return;
        }
        running = true;

        movementThread = new Thread(() -> {
            long lastTime = System.currentTimeMillis();

            while (running) {
                long now = System.currentTimeMillis();
                double dt = (now - lastTime) / 1000.0;
                lastTime = now;

                miseAJour(dt);
                syncHitbox();

                try { Thread.sleep(16); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; } // ~60 FPS
            }
        }, "monster-movement");

        movementThread.start();
    }

    @Override
    public void stopMovement() {
        // Les deux threads du monstre s'arretent ensemble pour eviter qu'ils continuent a tourner apres la partie.
        running = false;
        if (movementThread != null) {
            movementThread.interrupt();
        }
    }

    public void miseAJour(double dt) {
        if (speed <= 0) {
            return;
        }

        stepAccumulator += dt;
        double secondsPerTile = TILE_SIZE / speed;
        if (stepAccumulator < secondsPerTile) {
            velocity.setX(0);
            velocity.setY(0);
            return;
        }

        stepAccumulator -= secondsPerTile;
        synchronized (position) {
            chooseRandomDirection();
        }
    }

    private void syncHitbox() {
        if (hitbox != null) {
            synchronized (position) {
                hitbox.update(position);
            }
        }
    }

    private void chooseRandomDirection() {
        int[][] dirs = {
            { 1, 0}, {-1, 0}, {0, 1}, {0,-1}
        };
        int startIndex = random.nextInt(dirs.length);

        for (int i = 0; i < dirs.length; i++) {
            int[] d = dirs[(startIndex + i) % dirs.length];
            double nextX = position.getX() + d[0] * TILE_SIZE;
            double nextY = position.getY() + d[1] * TILE_SIZE;

            if (nextX >= minX && nextX <= maxX && nextY >= minY && nextY <= maxY) {
                position.setX(nextX);
                position.setY(nextY);
                heading.setX(d[0]);
                heading.setY(d[1]);
                velocity.setX(d[0] * TILE_SIZE);
                velocity.setY(d[1] * TILE_SIZE);
                return;
            }
        }

        velocity.setX(0);
        velocity.setY(0);
    }

    public double getX() { synchronized (position) { return position.getX(); } }
    public double getY() { synchronized (position) { return position.getY(); } }
    public double getVx() { return velocity.getX(); }
    public double getVy() { return velocity.getY(); }
    public double getHx() { return heading.getX(); }
    public double getHy() { return heading.getY(); }
    public double getMaxSpeed() { return speed; }
    public Color getColor() { return color; }

    public void setX(double x) { synchronized (position) { position.setX(x); } }
    public void setY(double y) { synchronized (position) { position.setY(y); } }
    public void setPosition(double x, double y) {
        synchronized (position) {
            position.setX(snapToTile(x));
            position.setY(snapToTile(y));
            stepAccumulator = 0.0;
            if (hitbox != null) {
                hitbox.update(position);
            }
        }
    }
    public void setVx(double vx) { velocity.setX(vx); }
    public void setVy(double vy) { velocity.setY(vy); }
    public void setHx(double hx) { heading.setX(hx); }
    public void setHy(double hy) { heading.setY(hy); }
    public void setMaxSpeed(double maxSpeed) { this.speed = maxSpeed; }
    public void setColor(Color color) { this.color = color; }

    public void setMovementBounds(double minX, double minY, double maxX, double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);
    }

    private double snapToTile(double value) {
        return snapToTileValue(value);
    }

    private static double snapToTileValue(double value) {
        return Math.round(value / TILE_SIZE) * TILE_SIZE;
    }
}
