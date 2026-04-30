package Tools;
import Tools.Coordinates;
import Tools.Hitbox;
import java.awt.image.BufferedImage; // image

public class Avatar {
    
    // Attributes
    private Coordinates position;
    private Hitbox hitbox;
    private BufferedImage image;
    private double speed;

    // Constructor
    public Avatar(Coordinates position, BufferedImage image, double speed) {
        this.position = position;
        this.image = image;
        this.hitbox = new Hitbox( this.position, this.image.getTileWidth(), this.image.getHeight());
        this.speed = speed;
    }

    public BufferedImage getImage() {
        return image;
    }

    public double getSpeed() {
        return speed;
    }

    // Getters and setters
    public Coordinates getPosition() {
        return this.position;
    }
    public Hitbox getHitbox() {
        return this.hitbox;
    }
    public void setPosition(Coordinates position) {
        this.position = position;
        this.hitbox.update(position);
    }


}
