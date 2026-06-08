package Avatar;

import TileMapping.CollisionMap;
import Tools.Coordinates;
import Tools.Hitbox;
import multiplayer.DonneesJoueur;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MonstersCollisionTest {

    private static CollisionMap grilleSeulementDroiteLibre() {
        int[][] g = {
            {415, 415, 415},
            {415, 413, 413},
            {415, 415, 415}
        };
        return new CollisionMap(g);
    }

    private static CollisionMap grilleLente() {
        int[][] g = {
            {415, 415, 415},
            {415, 414, 413},
            {415, 415, 415}
        };
        return new CollisionMap(g);
    }

    private static CollisionMap grilleMurEnFace() {
        int[][] g = {
            {415, 415, 415, 415},
            {415, 413, 415, 413},
            {415, 415, 415, 415}
        };
        return new CollisionMap(g);
    }

    private Monsters creerMonstre(double x, double y) {
        Hitbox hb = new Hitbox(new Coordinates(x, y), 32.0, 32.0);
        Monsters m = new Monsters(x, y, 2.0, hb);
        m.setMovementBounds(0, 0, 300, 300);
        return m;
    }

    @Test
    void monstre_evite_les_murs_en_marche_aleatoire() {
        Monsters m = creerMonstre(32.0, 32.0);
        m.setCollisionMap(grilleSeulementDroiteLibre());
        m.miseAJour(1.0);
        assertEquals(64.0, m.getX(), 0.001);
        assertEquals(32.0, m.getY(), 0.001);
    }

    @Test
    void monstre_bloque_sur_mur_pendant_poursuite() {
        Monsters m = creerMonstre(32.0, 32.0);
        m.setCollisionMap(grilleMurEnFace());
        m.setChaseSpeed(2.0);
        DonneesJoueur cible = new DonneesJoueur(1, "test", 96.0, 32.0, 0, 0, 1, true, false, 3);
        m.setJoueurs(new ArrayList<>(List.of(cible)));
        m.miseAJour(1.0);
        assertEquals(32.0, m.getX(), 0.001);
        assertEquals(32.0, m.getY(), 0.001);
    }

    @Test
    void tuile_lente_divise_la_vitesse_par_2() {
        Monsters m = creerMonstre(32.0, 32.0);
        m.setCollisionMap(grilleLente());
        // speed=2.0, factor=0.5 → secondsPerTile = 1/(2*0.5) = 1.0s
        // dt=0.6 < 1.0 → should NOT move
        m.miseAJour(0.6);
        assertEquals(32.0, m.getX(), 0.001, "ne doit pas bouger avec dt=0.6 sur tuile lente");
        assertEquals(32.0, m.getY(), 0.001);
        // dt=0.5 more → total 1.1 ≥ 1.0 → should move to (64, 32)
        m.miseAJour(0.5);
        assertEquals(64.0, m.getX(), 0.001, "doit avoir bougé après 1.1s sur tuile lente");
        assertEquals(32.0, m.getY(), 0.001);
    }
}