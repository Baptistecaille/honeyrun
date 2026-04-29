package multiplayer;

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

    // Coordonnées de spawn fixes par numéro d'avatar (fenêtre 800x600)
    private static final double[] SPAWN_X = {0, 20, 744, 20,  744};
    private static final double[] SPAWN_Y = {0, 20, 20,  544, 544};

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
        try (PreparedStatement ps = connexion.prepareStatement("SELECT avatar FROM joueur")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                avatarsPris.add(rs.getInt("avatar"));
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

        double spawnX = SPAWN_X[avatarChoisi];
        double spawnY = SPAWN_Y[avatarChoisi];

        try (PreparedStatement ps = connexion.prepareStatement(
                "INSERT INTO joueur (nom, spawnx, spawny, x, y, haswin, hashoney, avatar, lifes) "
                + "VALUES (?, ?, ?, ?, ?, 0, 0, ?, 3)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, pseudo);
            ps.setDouble(2, spawnX);
            ps.setDouble(3, spawnY);
            ps.setDouble(4, spawnX);
            ps.setDouble(5, spawnY);
            ps.setInt(6, avatarChoisi);
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
    public void mettreAJourPosition(int id, double x, double y, boolean hasHoney, int lifes)
            throws SQLException {
        try (PreparedStatement ps = connexion.prepareStatement(
                "UPDATE joueur SET x=?, y=?, hashoney=?, lifes=? WHERE id=?")) {
            ps.setDouble(1, x);
            ps.setDouble(2, y);
            ps.setDouble(3, hasHoney ? 1.0 : 0.0);
            ps.setInt(4, lifes);
            ps.setInt(5, id);
            ps.executeUpdate();
        }
    }

    /**
     * Lit toutes les lignes de la table joueur.
     */
    public List<DonneesJoueur> lireTousLesJoueurs() throws SQLException {
        List<DonneesJoueur> joueurs = new ArrayList<>();
        try (PreparedStatement ps = connexion.prepareStatement(
                "SELECT id, nom, x, y, spawnx, spawny, avatar, hashoney, haswin, lifes FROM joueur")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                joueurs.add(new DonneesJoueur(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getDouble("x"),
                    rs.getDouble("y"),
                    rs.getDouble("spawnx"),
                    rs.getDouble("spawny"),
                    rs.getInt("avatar"),
                    rs.getDouble("hashoney") != 0,
                    rs.getBoolean("haswin"),
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
                "SELECT nom FROM joueur WHERE haswin = 1 LIMIT 1")) {
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("nom") : null;
        }
    }

    /**
     * Marque le joueur local comme gagnant.
     */
    public void signalerVictoire(int id) throws SQLException {
        try (PreparedStatement ps = connexion.prepareStatement(
                "UPDATE joueur SET haswin = 1 WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Supprime toutes les lignes (reset après victoire).
     */
    public void reinitialiser() throws SQLException {
        try (PreparedStatement ps = connexion.prepareStatement("DELETE FROM joueur")) {
            ps.executeUpdate();
        }
    }

    /**
     * Supprime la ligne du joueur local (déconnexion propre).
     */
    public void deconnecter(int id) throws SQLException {
        try (PreparedStatement ps = connexion.prepareStatement(
                "DELETE FROM joueur WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}