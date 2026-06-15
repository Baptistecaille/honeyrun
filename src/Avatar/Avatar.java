package Avatar;

import Tools.Coordinates;
import Tools.Hitbox;
import java.awt.image.BufferedImage;
import java.util.Objects;

public abstract class Avatar {

    protected Coordinates position;
    protected BufferedImage image;
    protected double speed;
    protected Hitbox hitbox;

    public Avatar(Coordinates position, BufferedImage image, double speed, Hitbox hitbox) {
        Objects.requireNonNull(position, "position must not be null");
        this.position = position;
        this.image = image;
        this.speed = speed;
        this.hitbox = hitbox;
    }

    public Coordinates getPosition() {
        return position;
    }

    public void setPosition(Coordinates position) {
        Objects.requireNonNull(position, "position must not be null");
        this.position.setX(position.getX());
        this.position.setY(position.getY());
        if (this.hitbox != null) {
            this.hitbox.update(this.position);
        }
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public double getSpeed() {
        return speed;
    }

    public Hitbox getHitbox() {
        return hitbox;
    }

    public abstract void startMovement();

    public abstract void stopMovement();
}
