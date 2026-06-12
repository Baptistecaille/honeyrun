/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Interface;



import java.awt.Font;
import java.awt.Image;
import java.sql.*;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.Timer;
import java.util.HashMap; 
import java.util.Map;

public class Skin extends javax.swing.JFrame {

    private final PlayerSQL player;
    private Timer timer;

    public Skin(PlayerSQL player) {
        this.player = player; 
        setContentPane(new BackgroundPanel("Z:/Documents/GitHub/honeyrun/src/Interface/honey_background.png")); // Met une image en arrière-plan grâce à une image téléchargée
        initComponents(); // Initialise les composants
        setLocationRelativeTo(null); // Centre la fenêtre sur l'écran
        Font luckiestBase = null;
        try {
            luckiestBase = Font.createFont(Font.TRUETYPE_FONT, new File("src/Interface/luckiest-guy/luckiestguy.ttf")); // Importation d'une nouvelle police d'écriture
            } catch (Exception e) {
                throw new RuntimeException(e); // Arrêt du programme si la police ne peut pas être chargée
            }
        jLabel1.setFont(luckiestBase.deriveFont(38f)); // Application de la police
        loadSkinButtons(); // Appelle la fonction qui charge les skins dans les différents boutons
        checkAvailability(); // Appelle la fonction qui vérifie les skins disponibles
        setupListeners(); // Associe une action à chaque bouton de sélection
        startAvailabilityChecking(); // Vérification répétée tant que la fenêtre skin est ouverte
    }

private void checkAvailability() {

    Map<String, Boolean> disponibilites = new HashMap<>();

    try (
        Connection connexion = DriverManager.getConnection(
            "jdbc:mariadb://nemrod.ens2m.fr:3306/2025-2026_s2_vs1_tp1_honey_run",
            "etudiant",
            "YTDTvj9TR3CDYCmP"
        );

        PreparedStatement requete = connexion.prepareStatement(
            "SELECT nom, Disponibilité FROM Characters"
        );

        ResultSet rs = requete.executeQuery()
    ) {
        // Enregistre la disponibilité de chaque personnage.
        while (rs.next()) {
            disponibilites.put(
                rs.getString("nom"),
                rs.getBoolean("Disponibilité")
            );
        }

        // Un bouton est actif uniquement si son skin est disponible.
        jButton1.setEnabled(
            disponibilites.getOrDefault("Araignee Sans Pitie", false)
        );

        jButton2.setEnabled(
            disponibilites.getOrDefault("Mante Religieuse Tueuse", false)
        );

        jButton3.setEnabled(
            disponibilites.getOrDefault("Scarabee Mal Fame", false)
        );

        jButton4.setEnabled(
            disponibilites.getOrDefault("Criquet Suspect", false)
        );

    } catch (SQLException e) {
        e.printStackTrace();
    }
}

/**
 * Vérifie toutes les secondes quels skins sont encore disponibles.
 * Le Timer remplace une boucle while afin de ne pas bloquer Swing.
 */
private void startAvailabilityChecking() {

    timer = new Timer(1000, e -> checkAvailability());
    timer.start();
}

/**
 * Tente de réserver le skin choisi.
 * La réservation ne fonctionne que si le skin est encore disponible.
 *
 * @param skinName nom du skin sélectionné
 */
private void selectSkin(String skinName) {

    int skinId;

    try (
        Connection connexion = DriverManager.getConnection(
            "jdbc:mariadb://nemrod.ens2m.fr:3306/2025-2026_s2_vs1_tp1_honey_run",
            "etudiant",
            "YTDTvj9TR3CDYCmP"
        )
    ) {
        // Toutes les opérations doivent être validées ensemble.
        connexion.setAutoCommit(false);

        try {
            /*
             * Le skin est réservé uniquement si Disponibilité vaut encore 1.
             * Cette condition empêche deux joueurs de réserver le même skin.
             */
            try (PreparedStatement reserverSkin =
                    connexion.prepareStatement(
                        "UPDATE Characters "
                        + "SET Disponibilité = 0 "
                        + "WHERE nom = ? AND Disponibilité = 1"
                    )) {

                reserverSkin.setString(1, skinName);

                // Nombre de lignes réellement modifiées.
                int lignesModifiees = reserverSkin.executeUpdate();

                /*
                 * Si aucune ligne n'a été modifiée, le skin a été choisi
                 * par un autre joueur juste avant.
                 */
                if (lignesModifiees == 0) {
                    connexion.rollback();

                    JOptionPane.showMessageDialog(
                        this,
                        "Ce personnage vient d'être choisi par un autre joueur."
                    );

                    checkAvailability();
                    return;
                }
            }

            // Récupère l'identifiant du skin réservé.
            try (
                PreparedStatement rechercherId =
                        connexion.prepareStatement(
                            "SELECT id FROM Characters WHERE nom = ?"
                        )
            ) {
                rechercherId.setString(1, skinName);

                try (ResultSet rs = rechercherId.executeQuery()) {

                    if (!rs.next()) {
                        throw new SQLException(
                            "Identifiant du skin introuvable."
                        );
                    }

                    skinId = rs.getInt("id");
                }
            }

            // Associe le skin réservé au joueur courant.
            try (
                PreparedStatement updatePlayer =
                        connexion.prepareStatement(
                            "UPDATE `character` SET skin = ? WHERE id = ?"
                        )
            ) {
                updatePlayer.setInt(1, skinId);
                updatePlayer.setInt(2, player.getId());
                updatePlayer.executeUpdate();
            }

            // Valide définitivement la réservation.
            connexion.commit();

        } catch (SQLException e) {
            connexion.rollback();
            throw e;
        }

    } catch (SQLException e) {
        e.printStackTrace();

        JOptionPane.showMessageDialog(
            this,
            "Erreur lors du choix du personnage : " + e.getMessage()
        );

        return;
    }

    // Met à jour l'objet Java représentant le joueur.
    player.setCharacterId(skinId);

    JOptionPane.showMessageDialog(
        this,
        "Tu as choisi : " + skinName
    );

    // Ouvre la salle d'attente.
    new Lobby(player).setVisible(true);

    // Ferme la fenêtre de sélection.
    dispose();
}

    public void dispose() {

        if (timer != null) {
            timer.stop();
        }

        super.dispose();
    }


    private boolean isSkinAvailable(String skinName) {
        // Ouvre une connexion à la base de données
        try {
            Connection connexion = DriverManager.getConnection(
                "jdbc:mariadb://nemrod.ens2m.fr:3306/2025-2026_s2_vs1_tp1_honey_run",
                "etudiant",
                "YTDTvj9TR3CDYCmP"
            );
            
            // Requête cherchant la siponibilité du skin
            PreparedStatement requete = connexion.prepareStatement(
                "SELECT Disponibilité FROM Characters WHERE nom = ?"
            );
            requete.setString(1, skinName); // Remplace le paramètre de la requête par le nom du skin

            ResultSet rs = requete.executeQuery(); // Exécute la requête

            if (rs.next()) { // Vérifie si un résultat à été trouvé
                boolean dispo = rs.getBoolean("Disponibilité"); // Stocke la valeur du booléen dans la variable "dispo"
                
                // Ferme les ressources SQL utilisées
                rs.close();
                requete.close();
                connexion.close();
                
                return dispo; // Retourne la valeur du booléen
            }

            // Ferme les ressources SQL utilisées
            rs.close();
            requete.close();
            connexion.close();

        } catch (SQLException e) {
            e.printStackTrace(); // Affiche les informations de l’erreur SQL
        }

        return false; // Retourne "false" en cas d'erreur
    }
    
    private int getSkinId(String skinName) {
        // Ouvre une connexion à la base de données
        try {
            Connection connexion = DriverManager.getConnection(
                "jdbc:mariadb://nemrod.ens2m.fr:3306/2025-2026_s2_vs1_tp1_honey_run",
                "etudiant",
                "YTDTvj9TR3CDYCmP"
            );
            
            // Requête cherchant l'identifiant du skin
            PreparedStatement requete = connexion.prepareStatement(
                "SELECT id FROM Characters WHERE nom = ?"
            );
            requete.setString(1, skinName); // Ajoute le nom du skin dans la requête

            ResultSet rs = requete.executeQuery(); // Exécute la requête

            if (rs.next()) { // Vérifie si un résultat a été trouvé
                int id = rs.getInt("id"); // Stocke l'identifiant dans une variable "id"
                
                // Ferme les ressources SQL utilisées
                rs.close();
                requete.close();
                connexion.close();
                return id;
            }
            
            // Ferme les ressources SQL utilisées
            rs.close();
            requete.close();
            connexion.close();

        } catch (SQLException e) {
            e.printStackTrace(); // Affiche les erreurs SQL dans la console
        }

        return -1; // Retourne -1 par défaut
    }


    private void setButtonImage(javax.swing.JButton button, String resourcePath) {
        java.net.URL imgURL = getClass().getResource(resourcePath); // Recherche l’image dans les ressources du projet
        if (imgURL == null) { // Vérifie que le fichier image existe
            System.err.println("Image not found: " + resourcePath); // Affiche un message dans la console si l’image est introuvable
            return;
        }

        ImageIcon icon = new ImageIcon(imgURL); // Création d’une icône à partir de l’image trouvée
        Image scaled = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH); // Redimensionne l’image en 120 par 120 pixels
        button.setIcon(new ImageIcon(scaled)); // Place l’image redimensionnée dans le bouton
        button.setText(""); // Supprime le texte du bouton pour ne conserver que l’image
    }

    private void loadSkinButtons() {
        // Charge les images dans les boutons
        setButtonImage(jButton1, "/resources/Araignee.png");
        setButtonImage(jButton2, "/resources/Mantereligieuse.png");
        setButtonImage(jButton3, "/resources/Scarabe.png");
        setButtonImage(jButton4, "/resources/Criquet.png");

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
        // Associe chaque bouton au signe correspondant (grâce à la fonction ci-dessus)
        jButton1.addActionListener(choose("Araignee Sans Pitie"));
        jButton2.addActionListener(choose("Mante Religieuse Tueuse"));
        jButton3.addActionListener(choose("Scarabee Mal Fame"));
        jButton4.addActionListener(choose("Criquet Suspect"));
    }
    


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
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(45, Short.MAX_VALUE)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(56, 56, 56))
            .addGroup(layout.createSequentialGroup()
                .addGap(176, 176, 176)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 589, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(88, 88, 88)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(48, 48, 48)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE))
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
