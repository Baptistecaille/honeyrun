/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Interface;


import Avatar.FenetreDeJeu;
import java.awt.Font;
import java.io.File;
import java.sql.*;
import javax.swing.Timer;
import javax.swing.JOptionPane;
import multiplayer.DonneesJoueur;
import multiplayer.GestionnaireJoueurs;

public class Lobby extends javax.swing.JFrame {

    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Lobby.class.getName());


    private final PlayerSQL player;
    private Timer timer;

    public Lobby(PlayerSQL player) {
        this.player = player;
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
        jLabelStatus.setFont(luckiestBase.deriveFont(24f));
        jLabelTitle.setFont(luckiestBase.deriveFont(36f));
        setLocationRelativeTo(null);

        jLabelTitle.setText("Salle d'attente");
        jLabelStatus.setText("Joueurs prêts : 0/4");

        // Mode solo : si le pseudo est "seul", on saute l'attente des autres joueurs.
        // invokeLater est nécessaire car launchGame() appelle dispose() — si on l'appelle
        // directement dans le constructeur, dispose() s'exécute avant setVisible(true)
        // et la fenêtre réapparaît quand même. invokeLater reporte l'exécution après
        // que setVisible(true) soit appelé par Skin, donc dispose() fonctionne correctement
        // et la fenêtre de jeu récupère le focus clavier.
        if ("seul".equalsIgnoreCase(player.getPseudo())) {
            jLabelStatus.setText("Mode solo — lancement immédiat...");
            javax.swing.SwingUtilities.invokeLater(() -> launchGame());
        } else {
            startCheckingPlayers();
        }
    }

    // -----------------------------
    // 1. Vérifier le nombre de joueurs prêts
    // -----------------------------
    private int getReadyPlayers() {
        try {
            Connection connexion = DriverManager.getConnection(
                "jdbc:mariadb://nemrod.ens2m.fr:3306/2025-2026_s2_vs1_tp1_honey_run",
                "etudiant",
                "YTDTvj9TR3CDYCmP"
            );

            PreparedStatement requete = connexion.prepareStatement(
                "SELECT COUNT(*) AS total FROM Characters WHERE Disponibilité=0"
            );

            ResultSet rs = requete.executeQuery();
            int total = 0;

            if (rs.next()) {
                total = rs.getInt("total");
            }

            rs.close();
            requete.close();
            connexion.close();

            return total;

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // -----------------------------
    // 2. Lancer un timer qui met à jour l'affichage
    // -----------------------------
    private void startCheckingPlayers() {
        timer = new Timer(1000, e -> {
            int ready = getReadyPlayers();
            jLabelStatus.setText("Joueurs prêts : " + ready + "/4");

            if (ready >= 4) {
                timer.stop();
                startCountdown();
            }
        });

        timer.start();
    }

    // -----------------------------
    // 3. Compte à rebours avant lancement
    // -----------------------------
    private void startCountdown() {
        Timer countdown = new Timer(1000, null);
        final int[] time = {5};

        countdown.addActionListener(e -> {
            jLabelStatus.setText("La partie commence dans " + time[0]);
            time[0]--;

            if (time[0] < 0) {
                countdown.stop();
                launchGame();
            }
        });

        countdown.start();
    }

    // -----------------------------
    // 4. Lancer la partie
    // -----------------------------
    private void launchGame() {
        try {
            // Connexion à la base pour lire les données du joueur enregistrées par Accueil et Skin
            Connection connexion = DriverManager.getConnection(
                "jdbc:mariadb://nemrod.ens2m.fr:3306/2025-2026_s2_vs1_tp1_honey_run",
                "etudiant",
                "YTDTvj9TR3CDYCmP"
            );

            // Récupère la ligne du joueur courant dans la table character
            PreparedStatement ps = connexion.prepareStatement(
                "SELECT id, pseudo, X, Y, spawnX, spawnY, skin, hasHoney, hasWin, lifes FROM `character` WHERE id = ?"
            );
            ps.setInt(1, player.getId());
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Joueur introuvable en base.");
                connexion.close();
                return;
            }

            // skin est stocké en String dans la DB (null possible si non choisi) → conversion sécurisée
            int skin;
            try { skin = Integer.parseInt(rs.getString("skin")); } catch (NumberFormatException e) { skin = 0; }

            // Construit l'objet de données du joueur à partir de la DB
            DonneesJoueur moi = new DonneesJoueur(
                rs.getInt("id"),
                rs.getString("pseudo"),
                rs.getDouble("X"),       // position actuelle (= spawn au démarrage)
                rs.getDouble("Y"),
                rs.getDouble("spawnX"),  // point de réapparition en cas de mort
                rs.getDouble("spawnY"),
                skin,
                rs.getBoolean("hasHoney"),
                rs.getBoolean("hasWin"),
                rs.getInt("lifes")
            );

            connexion.close();

            // Lance la fenêtre de jeu et ferme le lobby
            GestionnaireJoueurs gestionnaire = new GestionnaireJoueurs();
            new FenetreDeJeu(moi, gestionnaire).setVisible(true);
            dispose();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur de connexion : " + ex.getMessage());
        }
    }


    // Variables NetBean

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelStatus = new javax.swing.JLabel();
        jLabelTitle = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabelStatus.setText("jLabel1");

        jLabelTitle.setText("jLabel1");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(407, Short.MAX_VALUE)
                .addComponent(jLabelTitle)
                .addGap(525, 525, 525))
            .addGroup(layout.createSequentialGroup()
                .addGap(286, 286, 286)
                .addComponent(jLabelStatus)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(65, 65, 65)
                .addComponent(jLabelTitle)
                .addGap(136, 136, 136)
                .addComponent(jLabelStatus)
                .addContainerGap(266, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabelStatus;
    private javax.swing.JLabel jLabelTitle;
    // End of variables declaration//GEN-END:variables
}
