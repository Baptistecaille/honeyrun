/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interface;

import java.awt.Color;
import java.awt.Graphics2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import Interface.SingletonJDBC;
import java.awt.image.BufferedImage;

/**
 * Exemple de classe avatar
 *
 * @author guillaume.laurent
 */
public class Avatar {

    private Coordinates Coordinates;
    private BufferedImage Image;
    private Double Speed;
    private Hitbox Hitbox;

    public Avatar(Coordinates coordinates, BufferedImage image, Double speed, Hitbox hitbox) {
        this.Coordinates = coordinates;
        this.Image = image;
        this.Speed = speed;
        this.Hitbox = hitbox;

    }

    public void setCoordinates(Coordinates Coordinates) {
        this.Coordinates = Coordinates;
    }

    public Coordinates getCoordinates() {
        return Coordinates;
    }
}