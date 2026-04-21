package Tools;

public class Hitbox {
    
    // Attributes
    private double x, y; // position of the rectangle of the hitbox
    private double width, height; // dimensions of the rectangle of the hitbox


    // Constructor
    public Hitbox(Coordinates position, double width, double height) {


        this.x = position.getX();
        this.y = position.getY();
        this.width = width;
        this.height = height;


        // TODO: create a function named update to update the position of the hitbox according to the position of the monster


    }

    // Getters and setters
    public double getX() {
        return this.x;
    }
    public double getY() {
        return this.y;
    }
    public double getWidth() {
        return this.width;
    }
    public double getHeight() {
        return this.height;
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
    public void update(Coordinates newPosition) {
        this.x = newPosition.getX();
        this.y = newPosition.getY();
    }


}
