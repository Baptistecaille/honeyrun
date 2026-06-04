/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Interface;

import Interface.BackgroundPanel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;
import java.awt.Font;
import java.io.File;

/**
 *
 * @author cpoussie
 */
public class Accueil extends javax.swing.JFrame {
    // Initialise la fenêtre
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Accueil.class.getName());
    

    public Accueil() {
        setContentPane(new BackgroundPanel("Z:/Documents/GitHub/honeyrun/src/Interface/honey_background.png")); 
        initComponents();
        Font luckiestBase = null;

        try {
            luckiestBase = Font.createFont(
                Font.TRUETYPE_FONT,
                new File("src/Interface/luckiest-guy/luckiestguy.ttf")
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    jLabel1.setFont(luckiestBase.deriveFont(64f));
    jLabel2.setFont(luckiestBase.deriveFont(28f));
    jLabel3.setFont(luckiestBase.deriveFont(18f));

    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Segoe UI Black", 0, 24)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Honey Run");
        jLabel1.setPreferredSize(new java.awt.Dimension(200, 75));

        jLabel2.setText("Connectez-vous à l'application....");

        jLabel3.setText("Pseudo");

        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });

        jButton1.setText("Entrer");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(265, 265, 265)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 352, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(315, 315, 315)
                        .addComponent(jLabel3)
                        .addGap(35, 35, 35)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton1)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(278, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 517, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(159, 159, 159))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(102, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(39, 39, 39)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(51, 51, 51)
                .addComponent(jButton1)
                .addGap(104, 104, 104))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        String nom = jTextField2.getText(); //Récupérer le nom entré par l'utilisateur

        if (nom == null) {
            return;
        }

        try {
            Connection connexion = DriverManager.getConnection(
                "jdbc:mariadb://nemrod.ens2m.fr:3306/2025-2026_s2_vs1_tp1_honey_run",
                "etudiant",
                "YTDTvj9TR3CDYCmP"
        );

        // Vérifier combien de joueurs existent
        PreparedStatement compteur = connexion.prepareStatement(
            "SELECT COUNT(*) AS total FROM `character`"
        );
        ResultSet countRs = compteur.executeQuery();
        countRs.next();
        
        int total = countRs.getInt("total");
        int spawnX = 0;
        int spawnY = 0;

        switch (total) {
            case 0: spawnX = 95; spawnY = 95; break;
            case 1: spawnX = 1825; spawnY = 993; break;
            case 2: spawnX = 1825; spawnY = 95; break;
            case 3: spawnX = 95; spawnY = 993; break;
            default:
                JOptionPane.showMessageDialog(this, "La partie est déjà pleine !");
                connexion.close();
                return;
        }
        int playerId;

        if (total < 4) {
            // Créer une nouvelle ligne vide
            PreparedStatement nv = connexion.prepareStatement(
                "INSERT INTO `character` (pseudo, spawnX, spawnY, X, Y, lifes, hasWin, hasHoney, skin) VALUES (?, ?, ?, ?, ?, 3, 0, 0, NULL)",
                Statement.RETURN_GENERATED_KEYS
            );
        
        nv.setString(1, nom);
        nv.setInt(2, spawnX);
        nv.setInt(3, spawnY);
        nv.setInt(4, spawnX); // position actuelle = spawn
        nv.setInt(5, spawnY);
        nv.executeUpdate();

    ResultSet generatedKeys = nv.getGeneratedKeys();
    if (generatedKeys.next()) {
        playerId = generatedKeys.getInt(1);
    } else {
        throw new SQLException("Impossible de récupérer l'ID du nouveau joueur.");
    }

    } else {
        JOptionPane.showMessageDialog(this, "La partie est déjà pleine !");
        connexion.close();
        return;
        }

// Maintenant mettre le pseudo dans la ligne créée
        PreparedStatement update = connexion.prepareStatement(
        "UPDATE `character` SET pseudo = ? WHERE id = ?"
        );
        update.setString(1, nom);
        update.setInt(2, playerId);

        //Assigner pseudo à cette ligne

        update.setString(1, nom);
        update.setInt(2, playerId);
        update.executeUpdate();

        connexion.close();
        

        // Créer un PlayerSQL pour le passer à Skin
        PlayerSQL p = new PlayerSQL(playerId, nom, null);

        // Ouvrir la fenêtre de choix du skin
        Skin skin = new Skin(p);
        skin.setVisible(true);
        this.dispose();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur SQL : " + e.getMessage());
    
        }

        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed


    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Accueil().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables
}
