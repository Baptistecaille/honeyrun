package reseau;

import java.io.Serializable;

public class EtatMonstre implements Serializable {

    private static final long serialVersionUID = 1L;

    public int id;
    public int x;
    public int y;

    public EtatMonstre(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }
}
