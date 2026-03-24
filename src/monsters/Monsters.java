package monsters;

import java.awt.Color;

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
    

    // Constructor
    public Monsters(double x, double y, double maxSpeed, Hitbox hitbox) {
        this.position = new Coordinates(x, y);
        this.maxSpeed = maxSpeed;
        this.hitbox = hitbox;

        // update the position of the monster and, in parallel using Thread, the position of the hitbox according to the key pressed by the user (up, down, left, right)
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