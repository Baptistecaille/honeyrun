package reseau;

import java.io.Serializable;
import java.util.List;

public class EtatJeu implements Serializable {

    private static final long serialVersionUID = 1L; // 1L pour la version de serialisation

    public List<EtatJoueur> joueurs;
    public List<EtatMonstre> monstres;

    public EtatJeu(List<EtatJoueur> joueurs, List<EtatMonstre> monstres) {
        this.joueurs = joueurs;
        this.monstres = monstres;
    }
}
