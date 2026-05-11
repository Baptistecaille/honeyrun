package Avatar;

import java.awt.Color;
import java.util.Random;

import Tools.Coordinates;
import Tools.Hitbox;

public class Monsters extends Avatar {

    private Coordinates velocity;
    private Coordinates heading;
    private Color color;

    private double minX = 0.0; // a revoir 
    private double minY = 0.0;
    private double maxX = Double.POSITIVE_INFINITY;
    private double maxY = Double.POSITIVE_INFINITY;

    private volatile boolean running = false;
    private Thread movementThread;
    private Thread hitboxThread;
    private final Random random = new Random();

    public Monsters(double x, double y, double speed, Hitbox hitbox) {
        super(new Coordinates(x, y), null, speed, hitbox);
        this.velocity = new Coordinates(0, 0);
        this.heading = new Coordinates(0, 0);
    }

    @Override
    public void startMovement() {
        running = true;

        movementThread = new Thread(() -> {
            long lastTime = System.currentTimeMillis();
            long lastDirectionChange = lastTime;
            chooseRandomDirection();

            while (running) {
                long now = System.currentTimeMillis();
                double dt = (now - lastTime) / 1000.0;
                lastTime = now;

                if (now - lastDirectionChange >= 3000) {
                    chooseRandomDirection();
                    lastDirectionChange = now;
                }

                double currentSpeed = Math.hypot(velocity.getX(), velocity.getY());
                if (currentSpeed > 0) {
                    heading.setX(velocity.getX() / currentSpeed);
                    heading.setY(velocity.getY() / currentSpeed);
                }

                synchronized (position) {
                    position.setX(position.getX() + velocity.getX() * dt);
                    position.setY(position.getY() + velocity.getY() * dt);

                    if (position.getX() < minX) {
                        position.setX(minX);
                        velocity.setX(Math.abs(velocity.getX()));
                    } else if (position.getX() > maxX) {
                        position.setX(maxX);
                        velocity.setX(-Math.abs(velocity.getX()));
                    }

                    if (position.getY() < minY) {
                        position.setY(minY);
                        velocity.setY(Math.abs(velocity.getY()));
                    } else if (position.getY() > maxY) {
                        position.setY(maxY);
                        velocity.setY(-Math.abs(velocity.getY()));
                    }
                }

                try { Thread.sleep(16); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; } // ~60 FPS
            }
        }, "monster-movement");

        hitboxThread = new Thread(() -> {
            while (running) {
                if (hitbox != null) {
                    synchronized (position) {
                        hitbox.update(position);
                    }
                }
                try { Thread.sleep(8); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
            }
        }, "monster-hitbox");

        movementThread.start();
        hitboxThread.start();
    }

    @Override
    public void stopMovement() {
        running = false;
    }

    private void chooseRandomDirection() {
        int[][] dirs = {
            { 1, 0}, {-1, 0}, {0, 1}, {0,-1},
            { 1, 1}, { 1,-1}, {-1, 1}, {-1,-1}
        };
        int[] d = dirs[random.nextInt(dirs.length)];
        double mag = Math.hypot(d[0], d[1]); // should always be 1 or sqrt(2) hypot sum of squares
        velocity.setX((d[0] / mag) * speed);
        velocity.setY((d[1] / mag) * speed);
    }

    public double getX() { return position.getX(); }
    public double getY() { return position.getY(); }
    public double getVx() { return velocity.getX(); }
    public double getVy() { return velocity.getY(); }
    public double getHx() { return heading.getX(); }
    public double getHy() { return heading.getY(); }
    public double getMaxSpeed() { return speed; }
    public Color getColor() { return color; }

    public void setX(double x) { position.setX(x); }
    public void setY(double y) { position.setY(y); }
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
}
