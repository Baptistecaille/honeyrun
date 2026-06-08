package TileMapping;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CollisionMapTest {

    private static int[][] grille() {
        return new int[][] {
            {415, 413, 414},
            {415, 413, 413},
            {415, 414, 415}
        };
    }

    @Test
    void isMur_retourne_vrai_pour_415() {
        CollisionMap map = new CollisionMap(grille());
        assertTrue(map.isMur(0, 0));
    }

    @Test
    void isMur_retourne_faux_pour_413() {
        CollisionMap map = new CollisionMap(grille());
        assertFalse(map.isMur(1, 0));
    }

    @Test
    void isMur_retourne_faux_pour_414() {
        CollisionMap map = new CollisionMap(grille());
        assertFalse(map.isMur(2, 0));
    }

    @Test
    void isMur_retourne_vrai_hors_limites() {
        CollisionMap map = new CollisionMap(grille());
        assertTrue(map.isMur(-1, 0));
        assertTrue(map.isMur(0, -1));
        assertTrue(map.isMur(10, 0));
        assertTrue(map.isMur(0, 10));
    }

    @Test
    void getSpeedFactor_retourne_0_5_pour_414() {
        CollisionMap map = new CollisionMap(grille());
        assertEquals(0.5, map.getSpeedFactor(2, 0), 0.001);
    }

    @Test
    void getSpeedFactor_retourne_1_0_pour_413() {
        CollisionMap map = new CollisionMap(grille());
        assertEquals(1.0, map.getSpeedFactor(1, 0), 0.001);
    }

    @Test
    void getSpeedFactor_retourne_1_0_pour_415() {
        CollisionMap map = new CollisionMap(grille());
        assertEquals(1.0, map.getSpeedFactor(0, 0), 0.001);
    }

    @Test
    void getSpeedFactor_retourne_1_0_hors_limites() {
        CollisionMap map = new CollisionMap(grille());
        assertEquals(1.0, map.getSpeedFactor(-1, 0), 0.001);
    }
}