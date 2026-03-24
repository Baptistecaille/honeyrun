package monsters;

public class Hitbox {
    
    // Attributes
    private double x, y; // position of the rectangle of the hitbox
    private double width, height; // dimensions of the rectangle of the hitbox


    // Constructor
    public Hitbox(double x, double y, double width, double height) {


        this.x = x; 
        this.y = y;
        this.width = width;
        this.height = height;


        // TODO: create a function named update to update the position of the hitbox according to the position of the monster


    }

    // Getters and setters
    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public double getWidth() {
        return width;
    }
    public double getHeight() {
        return height;
    }
    public void setX(double x) {
        this.x = x;
    }
    public void setY(double y) {
        this.y = y;
    }
    public void setWidth(double width) {
        this.width = width;
    }
    public void setHeight(double height) {
        this.height = height;
    }

    // Functions
    public void update(double newX, double newY) {
        this.x = newX;
        this.y = newY;
    }


}
