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
    public Avatar(Coordinates position, Hitbox hitbox, BufferedImage image, double speed) {
        this.position = position;
        this.hitbox = hitbox;
        this.image = image;
        this.speed = speed;
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
