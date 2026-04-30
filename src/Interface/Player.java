/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Interface;

import java.awt.Color;
import java.awt.Graphics2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author cpoussie
 */
public class Player {

    private int id;
    private boolean toucheHaut, toucheBas, toucheDroite, toucheGauche;
    private Avatar avatar;
    private Coordinates spawn;
    private String name;
    protected Map map;
    private int x;
    private int y;
    private boolean hasHoney;
    private boolean hasWon;
    private int lives = 3;

    public void miseAJour() {

        if (this.toucheHaut) {
            System.out.println("Déplacement de " + this.name + " vers le haut");

            // ajouter une requete ici...
        }
        if (this.toucheBas) {
            System.out.println("Déplacement de " + this.name + " vers le bas");

            // ajouter une requete ici...
        }
        if (this.toucheDroite) {
            System.out.println("Déplacement de " + this.name + " vers la droite");

            // ajouter une requete ici...
        }
        if (this.toucheGauche) {
            System.out.println("Déplacement de " + this.name + " vers la gauche");

            // ajouter une requete ici...
        }
        
        this.toucheHaut = false;
        this.toucheBas = false;
        this.toucheDroite = false;
        this.toucheGauche = false;
    }
    
    public void rendu(Graphics2D contexte) {
        try {
            Connection connexion = SingletonJDBC.getInstance().getConnection();

            PreparedStatement requete = connexion.prepareStatement("SELECT latitude, longitude FROM dresseurs WHERE pseudo = ?");
            requete.setString(1, name);
            ResultSet resultat = requete.executeQuery();
            if (resultat.next()) {
                double latitude = resultat.getDouble("latitude");
                double longitude = resultat.getDouble("longitude");
                //System.out.println(pseudo + " = (" + latitude + "; " + longitude + ")");

                int x = map.longitudeEnPixel(longitude);
                int y = map.latitudeEnPixel(latitude);
                contexte.setColor(Color.red);
                contexte.drawOval(x - 7, y - 7, 14, 14);
                //contexte.drawString(pseudo, x + 8, y - 8);
            }
            requete.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void setToucheHaut(boolean etat) {
        this.toucheHaut = etat;
    }

    public void setToucheBas(boolean etat) {
        this.toucheBas = etat;
    }

    public void setToucheGauche(boolean etat) {
        this.toucheGauche = etat;
    }

    public void setToucheDroite(boolean etat) {
        this.toucheDroite = etat;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public boolean isHasHoney() { return hasHoney; }
    public void setHasHoney(boolean hasHoney) { this.hasHoney = hasHoney; }

    public boolean isHasWon() { return hasWon; }
    public void setHasWon(boolean hasWon) { this.hasWon = hasWon; }

    public int getLives() { return lives; }
    public void setLives(int lives) { this.lives = lives; }

}
