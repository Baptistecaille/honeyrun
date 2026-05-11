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
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author cpoussie
 */
public class CharactersDAO {

    public List<GameCharacter> getAvailableCharacters() throws SQLException {
        String sql = "SELECT * FROM characters WHERE is_taken = 0";
        Connection conn = DriverManager.getConnection(
                "jdbc:mariadb://nemrod.ens2m.fr:3306/2025-2026_s2_vs1_tp1_honey_run",
                "etudiant",
                "YTDTvj9TR3CDYCmP");
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        List<GameCharacter> list = new ArrayList<>();
        while (rs.next()) {
            list.add(new GameCharacter(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getBoolean("is_taken")
            ));
        }
        return list;
    }

    public boolean takeCharacter(int characterId) throws SQLException {
        String sql = "UPDATE characters SET is_taken = 1 WHERE id = ? AND is_taken = 0";
        Connection conn = DriverManager.getConnection(
                "jdbc:mariadb://nemrod.ens2m.fr:3306/2025-2026_s2_vs1_tp1_honey_run",
                "etudiant",
                "YTDTvj9TR3CDYCmP");
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, characterId);
        return stmt.executeUpdate() == 1;
    }
}
