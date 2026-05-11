/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Interface;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author cpoussie
 */
public class PlayerDAO {
    



    public PlayerSQL createPlayer(String pseudo) throws SQLException {
        String sql = "INSERT INTO players (pseudo, character_id) VALUES (?, NULL)";
        Connection conn = DriverManager.getConnection(
                "jdbc:mariadb://nemrod.ens2m.fr:3306/2025-2026_s2_vs1_tp1_honey_run",
                "etudiant",
                "YTDTvj9TR3CDYCmP");
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, pseudo);
        stmt.executeUpdate();

        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            return new PlayerSQL(rs.getInt(1), pseudo, null);
        }
        throw new SQLException("Impossible de créer le joueur");
    }

    public void setCharacterForPlayer(int playerId, int characterId) throws SQLException {
        String sql = "UPDATE players SET character_id = ? WHERE id = ?";
        Connection conn = DriverManager.getConnection(
                "jdbc:mariadb://nemrod.ens2m.fr:3306/2025-2026_s2_vs1_tp1_honey_run",
                "etudiant",
                "YTDTvj9TR3CDYCmP");
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, characterId);
        stmt.setInt(2, playerId);
        stmt.executeUpdate();
    }

    public int countReadyPlayers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM players WHERE character_id IS NOT NULL";
        Connection conn = DriverManager.getConnection(
                "jdbc:mariadb://nemrod.ens2m.fr:3306/2025-2026_s2_vs1_tp1_honey_run",
                "etudiant",
                "YTDTvj9TR3CDYCmP");
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        return rs.getInt(1);
    }
}
