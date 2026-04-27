package Interface;

import reseau.EtatJeu;
import reseau.EtatJoueur;
import reseau.EtatMonstre;
import reseau.MessageEntree;
import reseau.ServeurTCP;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public class Jeu {

    private Map map;
    private Monsters monsters;
    private List<Player> joueurs;
    private Avatar avatar;
    private ServeurTCP serveurTCP;

    public Jeu(String pseudo) {
        this.map = new Map();
        this.monsters = new Monsters(Avatar);
        this.joueurs = new ArrayList<>();
        Player joueurLocal = new Player(Avatar, Spawn, Name);
        joueurLocal.setId(0);
        this.joueurs.add(joueurLocal);
        this.avatar = new Avatar(Coordinates, Image, Speed, Hitbox);
    }

    public void setServeurTCP(ServeurTCP serveurTCP) {
        this.serveurTCP = serveurTCP;
    }

    public void miseAJour() {
        this.map.miseAJour();
        this.monsters.miseAJour();
        for (Player joueur : new ArrayList<>(joueurs)) {
            joueur.miseAJour();
        }
        this.avatar.miseAJour();

        if (serveurTCP != null) {
            serveurTCP.broadcast(construireEtatJeu());
        }
    }

    public void rendu(Graphics2D contexte) {
        this.map.rendu(contexte);
        this.monsters.rendu(contexte);
        for (Player joueur : new ArrayList<>(joueurs)) {
            joueur.rendu(contexte);
        }
        this.avatar.rendu(contexte);
    }

    public EtatJeu construireEtatJeu() {
        List<EtatJoueur> etatsJoueurs = new ArrayList<>();
        for (Player joueur : new ArrayList<>(joueurs)) {
            etatsJoueurs.add(new EtatJoueur(
                joueur.getId(),
                joueur.getX(),
                joueur.getY(),
                joueur.isHasHoney(),
                joueur.isHasWon(),
                joueur.getLives()
            ));
        }
        // Les monstres seront enrichis quand Monsters exposera sa liste
        List<EtatMonstre> estatsMonstres = new ArrayList<>();
        return new EtatJeu(etatsJoueurs, estatsMonstres);
    }

    public void appliquerEtat(EtatJeu etat) {
        // Remplace l'état local par l'état reçu du serveur (mode client uniquement)
        joueurs.clear();
        for (EtatJoueur ej : etat.joueurs) {
            Player p = new Player();
            p.setId(ej.id);
            p.setX(ej.x);
            p.setY(ej.y);
            p.setHasHoney(ej.hasHoney);
            p.setHasWon(ej.hasWon);
            p.setLives(ej.lives);
            joueurs.add(p);
        }
    }

    public void appliquerEntree(MessageEntree msg) {
        for (Player joueur : joueurs) {
            if (joueur.getId() == msg.joueurId) {
                joueur.setToucheHaut(msg.toucheHaut);
                joueur.setToucheBas(msg.toucheBas);
                joueur.setToucheGauche(msg.toucheGauche);
                joueur.setToucheDroite(msg.toucheDroite);
                return;
            }
        }
    }

    public void retirerJoueur(int id) {
        joueurs.removeIf(j -> j.getId() == id);
    }

    public void ajouterJoueur(Player joueur) {
        joueurs.add(joueur);
    }

    public boolean estTermine() {
        return false;
    }

    public Avatar getAvatar() {
        return avatar;
    }
}
