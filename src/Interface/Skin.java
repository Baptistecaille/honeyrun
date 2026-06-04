/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Interface;



import java.awt.Image;
import java.sql.*;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Skin extends javax.swing.JFrame {

    private final PlayerSQL player;

    public Skin(PlayerSQL player) {
        this.player = player;
        setContentPane(new BackgroundPanel("Z:/Documents/GitHub/honeyrun/src/Interface/honey_background.png"));
        initComponents();
        setLocationRelativeTo(null);

        loadSkinButtons();
        checkAvailability();
        setupListeners();
    }


    private boolean isSkinAvailable(String skinName) {
        try {
            Connection connexion = DriverManager.getConnection(
                "jdbc:mariadb://nemrod.ens2m.fr:3306/2025-2026_s2_vs1_tp1_honey_run",
                "etudiant",
                "YTDTvj9TR3CDYCmP"
            );

            PreparedStatement requete = connexion.prepareStatement(
                "SELECT Disponibilité FROM Characters WHERE nom = ?"
            );
            requete.setString(1, skinName);

            ResultSet rs = requete.executeQuery();

            if (rs.next()) {
                boolean dispo = rs.getBoolean("Disponibilité");
                rs.close();
                requete.close();
                connexion.close();
                return dispo;
            }

            rs.close();
            requete.close();
            connexion.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
    
    private int getSkinId(String skinName) {
        try {
            Connection connexion = DriverManager.getConnection(
                "jdbc:mariadb://nemrod.ens2m.fr:3306/2025-2026_s2_vs1_tp1_honey_run",
                "etudiant",
                "YTDTvj9TR3CDYCmP"
            );

            PreparedStatement requete = connexion.prepareStatement(
                "SELECT id FROM Characters WHERE nom = ?"
            );
            requete.setString(1, skinName);

            ResultSet rs = requete.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                rs.close();
                requete.close();
                connexion.close();
                return id;
            }

            rs.close();
            requete.close();
            connexion.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }


    private void setButtonImage(javax.swing.JButton button, String resourcePath) {
        java.net.URL imgURL = getClass().getResource(resourcePath);
        if (imgURL == null) {
            System.err.println("Image not found: " + resourcePath);
            return;
        }

        ImageIcon icon = new ImageIcon(imgURL);
        Image scaled = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
        button.setIcon(new ImageIcon(scaled));
        button.setText("");
    }

    private void loadSkinButtons() {
        setButtonImage(jButton1, "/resources/Mantereligieuse.png");
        setButtonImage(jButton2, "/resources/Criquet.png");
        setButtonImage(jButton3, "/resources/Araignee.png");
        setButtonImage(jButton4, "/resources/Scarabe.png");

    }


    private void checkAvailability() {
        jButton1.setEnabled(isSkinAvailable("Mante Religieuse Tueuse"));
        jButton2.setEnabled(isSkinAvailable("Criquet Suspect"));
        jButton3.setEnabled(isSkinAvailable("Araignee Sans Pitie"));
        jButton4.setEnabled(isSkinAvailable("Scarabee Mal Fame"));

    }
    
    private ActionListener choose(final String skin) {
        return new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent evt) {
            selectSkin(skin);
        }
    };
}

    private void setupListeners() {
        jButton1.addActionListener(choose("Mante Religieuse Tueuse"));
        jButton2.addActionListener(choose("Criquet Suspect"));
        jButton3.addActionListener(choose("Araignee Sans Pitie"));
        jButton4.addActionListener(choose("Scarabee Mal Fame"));
    }
    
    private void selectSkin(String skinName) {
        JOptionPane.showMessageDialog(this, "Tu as choisi : " + skinName);
        

        try {
            Connection connexion = DriverManager.getConnection(
                "jdbc:mariadb://nemrod.ens2m.fr:3306/2025-2026_s2_vs1_tp1_honey_run",
                "etudiant",
                "YTDTvj9TR3CDYCmP"
            );

            // Marquer le skin comme pris
            PreparedStatement update = connexion.prepareStatement(
                "UPDATE Characters SET Disponibilité = 0 WHERE nom = ?"
            );
            update.setString(1, skinName);
            update.executeUpdate();
            int skinId = getSkinId(skinName);

            // 3) Mettre à jour la table Character (celle de ton screenshot)
            PreparedStatement updatePlayer = connexion.prepareStatement(
                "UPDATE character SET skin = ? WHERE id = ?"
            );
            updatePlayer.setInt(1, skinId);
            updatePlayer.setInt(2, player.getId()); // ton joueur actuel
            updatePlayer.executeUpdate();
            connexion.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        

        // Ouvrir la salle d'attente
        new Lobby(player).setVisible(true);
        dispose();
    }

    // -----------------------------
    // 5. Interface générée par NetBeans
    // -----------------------------


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel1.setText("Choose your character");

        jButton1.setText("jButton1");

        jButton2.setText("jButton2");

        jButton3.setText("jButton3");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("jButton4");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(96, Short.MAX_VALUE)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(150, 150, 150))
            .addGroup(layout.createSequentialGroup()
                .addGap(107, 107, 107)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 243, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(108, 108, 108)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(157, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton3ActionPerformed

    /**
     * @param args the command line arguments
     */
   
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

        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Accueil().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
