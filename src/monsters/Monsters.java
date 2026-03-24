package monsters;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

    // Movement threads
    private Set<Integer> pressedKeys = Collections.synchronizedSet(new HashSet<>());
    private boolean running = false;
    private Thread movementThread;
    private Thread hitboxThread;

    // Constructor
    public Monsters(double x, double y, double maxSpeed, Hitbox hitbox) {
        this.position = new Coordinates(x, y);
        this.maxSpeed = maxSpeed;
        this.hitbox = hitbox;
    }

    /**
     * Starts two threads:
     *  - movementThread: updates the monster position at ~60 fps based on held keys.
     *  - hitboxThread:   keeps the hitbox in sync with the monster position, in parallel.
     */
    public void startMovement() {
        running = true;

        // Thread 1 – update monster position based on pressed keys
        movementThread = new Thread(() -> {
            long lastTime = System.currentTimeMillis();
            while (running) {
                // Calculate time delta for smoother movement
                long now = System.currentTimeMillis();
                double dt = (now - lastTime) / 1000.0; // seconds elapsed
                lastTime = now;

                double dx = 0, dy = 0;
                if (pressedKeys.contains(KeyEvent.VK_UP)    || pressedKeys.contains(KeyEvent.VK_W)) dy -= maxSpeed;
                if (pressedKeys.contains(KeyEvent.VK_DOWN)  || pressedKeys.contains(KeyEvent.VK_S)) dy += maxSpeed;
                if (pressedKeys.contains(KeyEvent.VK_LEFT)  || pressedKeys.contains(KeyEvent.VK_A)) dx -= maxSpeed;
                if (pressedKeys.contains(KeyEvent.VK_RIGHT) || pressedKeys.contains(KeyEvent.VK_D)) dx += maxSpeed;

                vx = dx;
                vy = dy;

                // Update heading (normalized direction vector)
                double speed = Math.hypot(vx, vy); // hypot calculates sqrt(sum of squares)
                if (speed > 0) {
                    hx = vx / speed;
                    hy = vy / speed;
                }

                synchronized (position) {
                    position.setX(position.getX() + vx * dt); // Using time delta previously calculated 
                    position.setY(position.getY() + vy * dt);
                }

                try { Thread.sleep(16); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
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

    // TODO: Define the next random movement made by the monster 


}