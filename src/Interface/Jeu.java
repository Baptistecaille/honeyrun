package Interface;

import java.awt.Graphics2D;


/**
 * Exemple de classe jeu
 *
 * @author guillaume.laurent
 */
public class Jeu {

    private Map map;
    private Monsters monsters;
    private Player player;
    private Avatar avatar;

    public Jeu(String pseudo) {
        this.map = new Map();
        this.monsters = new Monsters(Avatar);
        this.player = new Player(Avatar, Spawn, Name);
        this.avatar = new Avatar(Coordinates, Image, Speed, Hitbox);
    }

    public void miseAJour() {
        this.map.miseAJour();
        this.monsters.miseAJour();
        this.player.miseAJour();
        this.avatar.miseAJour();
    }

    public void rendu(Graphics2D contexte) {
        this.map.rendu(contexte);
        this.monsters.rendu(contexte);
        this.player.rendu(contexte);
        this.avatar.rendu(contexte);
    }
    
    public boolean estTermine() {
        return false;
    }

    public Avatar getAvatar() {
        return avatar;
    }
    
    

}
