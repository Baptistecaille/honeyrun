package monsters;

import java.awt.Color;
import java.util.Random;

public class Monsters {

    // Attributes

    // position
    private Coordinates position;
    // speed
    private double vx, vy; // in pixels per second
    private double maxSpeed; // in pixels per second
    // Heading: normalized vector of the direction of movement
    private double hx, hy;
    // Color
    private Color color;
    //hitbox
    private Hitbox hitbox;

    // Movement bounds (e.g. game window)
    private double minX = 0.0;
    private double minY = 0.0;
    private double maxX = Double.POSITIVE_INFINITY;
    private double maxY = Double.POSITIVE_INFINITY;

    // Movement threads
    private boolean running = false;
    private Thread movementThread;
    private Thread hitboxThread;
    private final Random random = new Random();

    // Constructor
    public Monsters(double x, double y, double maxSpeed, Hitbox hitbox) {
        this.position = new Coordinates(x, y);
        this.maxSpeed = maxSpeed;
        this.hitbox = hitbox;
    }

    /**
     * Starts two threads to parallelise movement and hitbox updates:
     *  - movementThread: updates the monster position at around 60 fps with random directions.
     *  - hitboxThread:   keeps the hitbox in sync with the monster position, in parallel.
     */
    public void startMovement() {
        running = true;

        // Thread 1 – update monster position with random direction changes every 3s
        movementThread = new Thread(() -> {
            long lastTime = System.currentTimeMillis();
            long lastDirectionChange = lastTime;
            chooseRandomDirection();

            while (running) {
                // Calculate time delta for smoother movement
                long now = System.currentTimeMillis();
                double dt = (now - lastTime) / 1000.0; // seconds elapsed
                lastTime = now;

                if (now - lastDirectionChange >= 3000) {
                    chooseRandomDirection();
                    lastDirectionChange = now;
                }

                // Update heading (normalized direction vector)
                double speed = Math.hypot(vx, vy); // hypot calculates sqrt(sum of squares)
                if (speed > 0) {
                    hx = vx / speed;
                    hy = vy / speed;
                }

                synchronized (position) {
                    position.setX(position.getX() + vx * dt); // Using time delta previously calculated 
                    position.setY(position.getY() + vy * dt); // Using time delta previously calculated 

                    // Bounce on horizontal edges
                    if (position.getX() < minX) {
                        position.setX(minX);
                        vx = Math.abs(vx);
                    } else if (position.getX() > maxX) {
                        position.setX(maxX);
                        vx = -Math.abs(vx);
                    }

                    // Bounce on vertical edges
                    if (position.getY() < minY) {
                        position.setY(minY);
                        vy = Math.abs(vy);
                    } else if (position.getY() > maxY) {
                        position.setY(maxY);
                        vy = -Math.abs(vy);
                    }
                }

                try { Thread.sleep(16); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; } // sleep for 16ms to match 60 fps
            }
        }, "monster-movement");

        // Thread 2 – sync hitbox position to monster position, in parallel
        hitboxThread = new Thread(() -> {
            while (running) {
                double x, y;
                synchronized (position) {
                    x = position.getX();
                    y = position.getY();
                }
                hitbox.update(this.position); // Update hitbox position to match monster position

                //try { Thread.sleep(5); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
            }
        }, "monster-hitbox");

        movementThread.start();
        hitboxThread.start();
    }

    /** Stops both movement threads. */
    public void stopMovement() {
        running = false;
    }

    private void chooseRandomDirection() {
        // Choose one of the 8 cardinal/diagonal directions.
        int[][] dirs = {
            { 1, 0}, {-1, 0}, {0, 1}, {0,-1},
            { 1, 1}, { 1,-1}, {-1, 1}, {-1,-1}
        };

        int[] d = dirs[random.nextInt(dirs.length)];
        double mag = Math.hypot(d[0], d[1]);
        vx = (d[0] / mag) * maxSpeed;
        vy = (d[1] / mag) * maxSpeed;
    }

    // Getters and setters
    public double getX() {
        return position.getX();
    }
    public double getY() {
        return position.getY();
    }
    public double getVx() {
        return vx;
    }
    public double getVy() {
        return vy;
    }
    public double getMaxSpeed() {
        return maxSpeed;
    }
    public double getHx() {
        return hx;
    }
    public double getHy() {
        return hy;
    }
    public Color getColor() {
        return color;
    }
    public Hitbox getHitbox() {
        return hitbox;
    }
    public void setX(double x) {
        this.position.setX(x);
    }
    public void setY(double y) {
        this.position.setY(y);
    }
    public void setVx(double vx) {
        this.vx = vx;
    }
    public void setVy(double vy) {
        this.vy = vy;
    }
    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }
    public void setHx(double hx) {
        this.hx = hx;
    }
    public void setHy(double hy) {
        this.hy = hy;
    }
    public void setColor(Color color) {
        this.color = color;
    }
    public void setHitbox(Hitbox hitbox) {
        this.hitbox = hitbox;
    }

    public void setMovementBounds(double minX, double minY, double maxX, double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);
    }
}