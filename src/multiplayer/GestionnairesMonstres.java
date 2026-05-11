package multiplayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GestionnairesMonstres {

    public static final int NOMBRE_MONSTRES = 3;

    private static final double[] SPAWN_X = {380, 150, 600};
    private static final double[] SPAWN_Y = {280, 150, 400};

    private final Connection connexion;

    public GestionnairesMonstres() {
        this.connexion = SingletonJDBC.getInstance().getConnection();
    }

    /**
     * Si la table est vide, insère NOMBRE_MONSTRES lignes avec les positions de spawn.
     * Dans tous les cas, retourne la liste complète lue depuis la DB.
     */
    public List<DonneesMonstre> initialiser() throws SQLException {
        int count;
        try (PreparedStatement ps = connexion.prepareStatement("SELECT COUNT(*) FROM monstres");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            count = rs.getInt(1);
        }
        if (count == 0) {
            try (PreparedStatement ps = connexion.prepareStatement(
                    "INSERT INTO monstres (x, y) VALUES (?, ?)")) {
                for (int i = 0; i < NOMBRE_MONSTRES; i++) {
                    ps.setDouble(1, SPAWN_X[i]);
                    ps.setDouble(2, SPAWN_Y[i]);
                    ps.executeUpdate();
                }
            }
        }
        return lireTousLesMonstres();
    }

    /**
     * Met à jour x et y de chaque monstre dans la DB (appelé par le client autoritaire).
     */
    public void mettreAJourPositions(List<DonneesMonstre> monstres) throws SQLException {
        if (monstres == null || monstres.isEmpty()) return;
        try (PreparedStatement ps = connexion.prepareStatement(
                "UPDATE monstres SET x=?, y=? WHERE id=?")) {
            for (DonneesMonstre m : monstres) {
                ps.setDouble(1, m.x);
                ps.setDouble(2, m.y);
                ps.setInt(3, m.id);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Lit toutes les lignes de la table monstres (appelé par les clients non-autoritaires).
     */
    public List<DonneesMonstre> lireTousLesMonstres() throws SQLException {
        List<DonneesMonstre> monstres = new ArrayList<>();
        try (PreparedStatement ps = connexion.prepareStatement(
                "SELECT id, x, y FROM monstres")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                monstres.add(new DonneesMonstre(
                    rs.getInt("id"),
                    rs.getDouble("x"),
                    rs.getDouble("y")
                ));
            }
        }
        return monstres;
    }

    /**
     * Supprime tous les monstres (appelé en fin de partie, comme reinitialiser() de GestionnaireJoueurs).
     */
    public void reinitialiser() throws SQLException {
        try (PreparedStatement ps = connexion.prepareStatement("DELETE FROM monstres")) {
            ps.executeUpdate();
        }
    }
}
