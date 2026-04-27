package reseau;

import java.io.Serializable;

public class MessageEntree implements Serializable {

    private static final long serialVersionUID = 1L;

    public int joueurId;
    public boolean toucheHaut;
    public boolean toucheBas;
    public boolean toucheGauche;
    public boolean toucheDroite;

    public MessageEntree(int joueurId, boolean haut, boolean bas, boolean gauche, boolean droite) {
        this.joueurId = joueurId;
        this.toucheHaut = haut;
        this.toucheBas = bas;
        this.toucheGauche = gauche;
        this.toucheDroite = droite;
    }
}
