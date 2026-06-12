package multiplayer;

import Avatar.GameConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

/**
 * Centralise toutes les opérations MySQL liées au multijoueur.
 * Chaque client appelle cette classe pour se connecter, mettre à jour
 * sa position, lire les autres joueurs, détecter la victoire et se déconnecter.
 */
public class GestionnaireJoueurs {

    // Coordonnées de spawn fixes par numéro d'avatar sur la carte 60x34 tuiles
    private static final double[] SPAWN_X = {95, 1825, 95,  1825}; // to modify accoording to the map area
    private static final double[] SPAWN_Y = {95, 95,  993, 993}; // to modify accoording to the map area
    private static final String VERROU_MIEL = "honeyrun_unique_honey";

    private final Connection connexion;

    public GestionnaireJoueurs() {
        this.connexion = SingletonJDBC.getInstance().getConnection();
    }

    /**
     * Connecte un joueur : choisit un avatar disponible, insère sa ligne en DB
     * et retourne ses données initiales.
     *
     * @return DonneesJoueur du joueur créé, ou null si la partie est pleine
     */
    public DonneesJoueur connecter(String pseudo) throws SQLException {
        Set<Integer> avatarsPris = new HashSet<>();
        try (PreparedStatement ps = connexion.prepareStatement("SELECT skin FROM `character`")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                try { avatarsPris.add(Integer.valueOf(rs.getString("skin"))); } catch (NumberFormatException ignored) {}
            }
        }

        int avatarChoisi = -1;
        for (int i = 1; i <= 4; i++) {
            if (!avatarsPris.contains(i)) {
                avatarChoisi = i;
                break;
            }
        }

        if (avatarChoisi == -1) {
            JOptionPane.showMessageDialog(null, "Partie pleine (4 joueurs max).");
            return null;
        }

        double spawnX = SPAWN_X[avatarChoisi - 1];
        double spawnY = SPAWN_Y[avatarChoisi - 1];
        double tuileSpawnX = convertirCoordonneeEnTuile(spawnX);
        double tuileSpawnY = convertirCoordonneeEnTuile(spawnY);

        try (PreparedStatement ps = connexion.prepareStatement(
                "INSERT INTO `character` (pseudo, spawnX, spawnY, X, Y, hasWin, hasHoney, skin, lifes) "
                + "VALUES (?, ?, ?, ?, ?, 0, 0, ?, 3)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, pseudo);
            ps.setDouble(2, spawnX);
            ps.setDouble(3, spawnY);
            ps.setDouble(4, tuileSpawnX);
            ps.setDouble(5, tuileSpawnY);
            ps.setString(6, String.valueOf(avatarChoisi));
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            keys.next();
            int id = keys.getInt(1);

            return new DonneesJoueur(id, pseudo, spawnX, spawnY, spawnX, spawnY,
                                     avatarChoisi, false, false, 3);
        }
    }

    /**
     * Écrit la position et l'état du joueur local en DB.
     */
    // hasHoney n'est plus écrit ici : seuls recolterMiel(), volerMiel() et perdreLeHmiel() modifient cette colonne
    // (évite que le thread de sync écrase un vol en réécrivant l'ancien état local)
    public void mettreAJourPosition(int id, double tuileX, double tuileY, int lifes)
            throws SQLException {
        try (PreparedStatement ps = connexion.prepareStatement(
                "UPDATE `character` SET X=?, Y=?, lifes=? WHERE id=?")) {
            ps.setDouble(1, tuileX);
            ps.setDouble(2, tuileY);
            ps.setInt(3, lifes);
            ps.setInt(4, id);
            ps.executeUpdate();
        }
    }

    private double convertirCoordonneeEnTuile(double valeur) {
        return Math.round(valeur / GameConstants.TILE_SIZE);
    }

    private double convertirTuileEnCoordonnee(double tuile) {
        return tuile * GameConstants.TILE_SIZE;
    }

    // Remet hasHoney à 0 quand un monstre touche le porteur (appelé explicitement depuis Player)
    public void perdreLeHmiel(int id) throws SQLException {
        boolean verrouPris = obtenirVerrouMiel();
        if (!verrouPris) {
            throw new SQLException("Impossible d'obtenir le verrou du miel.");
        }
        try {
            try (PreparedStatement ps = connexion.prepareStatement(
                    "UPDATE `character` SET hasHoney=0 WHERE id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        } finally {
            relacherVerrouMiel();
        }
    }

    /**
     * Lit toutes les lignes de la table joueur.
     */
    public List<DonneesJoueur> lireTousLesJoueurs() throws SQLException {
        List<DonneesJoueur> joueurs = new ArrayList<>();
        try (PreparedStatement ps = connexion.prepareStatement(
                "SELECT id, pseudo, X, Y, spawnX, spawnY, skin, hasHoney, hasWin, lifes FROM `character`")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int skin;
                try { skin = Integer.parseInt(rs.getString("skin")); } catch (NumberFormatException e) { skin = 0; }
                joueurs.add(new DonneesJoueur(
                    rs.getInt("id"),
                    rs.getString("pseudo"),
                    convertirTuileEnCoordonnee(rs.getDouble("X")),
                    convertirTuileEnCoordonnee(rs.getDouble("Y")),
                    rs.getDouble("spawnX"),
                    rs.getDouble("spawnY"),
                    skin,
                    rs.getBoolean("hasHoney"),
                    rs.getBoolean("hasWin"),
                    rs.getInt("lifes")
                ));
            }
        }
        return joueurs;
    }

    /**
     * Retourne le nom du joueur gagnant, ou null si personne n'a encore gagné.
     */
    public String detecterVictoire() throws SQLException {
        try (PreparedStatement ps = connexion.prepareStatement(
                "SELECT pseudo FROM `character` WHERE hasWin = 1 LIMIT 1")) {
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("pseudo") : null;
        }
    }

    private boolean obtenirVerrouMiel() throws SQLException {
        try (PreparedStatement ps = connexion.prepareStatement("SELECT GET_LOCK(?, 2)")) {
            ps.setString(1, VERROU_MIEL);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) == 1;
        }
    }

    private void relacherVerrouMiel() throws SQLException {
        try (PreparedStatement ps = connexion.prepareStatement("SELECT RELEASE_LOCK(?)")) {
            ps.setString(1, VERROU_MIEL);
            ps.executeQuery();
        }
    }

    private boolean existePorteurMiel() throws SQLException {
        try (PreparedStatement ps = connexion.prepareStatement(
                "SELECT COUNT(*) FROM `character` WHERE hasHoney = 1")) {
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private boolean joueurPorteMiel(int id) throws SQLException {
        try (PreparedStatement ps = connexion.prepareStatement(
                "SELECT COUNT(*) FROM `character` WHERE id = ? AND hasHoney = 1")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) == 1;
        }
    }

    private boolean attribuerMielUniquementA(int id) throws SQLException {
        try (PreparedStatement ps = connexion.prepareStatement(
                "UPDATE `character` SET hasHoney = CASE WHEN id = ? THEN 1 ELSE 0 END")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = connexion.prepareStatement(
                "SELECT COUNT(*) FROM `character` WHERE id = ? AND hasHoney = 1")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) == 1;
        }
    }

    /**
     * Attribue le miel au joueur uniquement si personne d'autre ne l'a déjà.
     * Le UPDATE atomique garantit qu'un seul joueur obtient le miel même si deux terminent la récolte en même temps.
     * Retourne true si le miel a bien été attribué, false si quelqu'un d'autre l'a déjà pris.
     */
    public boolean recolterMiel(int id) throws SQLException {
        boolean verrouPris = obtenirVerrouMiel();
        if (!verrouPris) {
            throw new SQLException("Impossible d'obtenir le verrou du miel.");
        }
        try {
            if (existePorteurMiel()) {
                if (joueurPorteMiel(id)) {
                    return attribuerMielUniquementA(id);
                }
                return false;
            }
            return attribuerMielUniquementA(id);
        } finally {
            relacherVerrouMiel();
        }
    }

    /**
     * Tente un vol atomique du miel : retire le miel du porteur (idPorteur) et le donne au voleur (idVoleur).
     * Le UPDATE conditionnel (AND hasHoney=1) empêche deux voleurs simultanés de réussir en même temps.
     * Retourne true si le vol a réussi, false si le porteur n'avait plus le miel (quelqu'un d'autre plus rapide).
     */
    public boolean volerMiel(int idVoleur, int idPorteur) throws SQLException {
        boolean verrouPris = obtenirVerrouMiel();
        if (!verrouPris) {
            throw new SQLException("Impossible d'obtenir le verrou du miel.");
        }
        try {
            if (!joueurPorteMiel(idPorteur)) {
                return false;
            }
            return attribuerMielUniquementA(idVoleur);
        } finally {
            relacherVerrouMiel();
        }
    }

    /**
     * Marque le joueur local comme gagnant.
     */
    public void signalerVictoire(int id) throws SQLException {
        try (PreparedStatement ps = connexion.prepareStatement(
                "UPDATE `character` SET hasWin = 1 WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Supprime toutes les lignes (reset après victoire).
     */
    public void reinitialiser() throws SQLException {
        try (PreparedStatement ps = connexion.prepareStatement("DELETE FROM `character`")) {
            ps.executeUpdate();
        }
    }

    /**
     * Supprime la ligne du joueur local (déconnexion propre).
     */
    public void deconnecter(int id) throws SQLException {
        try (PreparedStatement ps = connexion.prepareStatement(
                "DELETE FROM `character` WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
