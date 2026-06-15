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


    private final PlayerSQL player; //Joueur connecté
    private Timer timer; //Utilisé pour afficher toutes les T secondes le nombre de joueurs prêts

    public Lobby(PlayerSQL player) {
        this.player = player;
        setContentPane(new BackgroundPanel("Z:/Documents/GitHub/honeyrun/src/Interface/honey_background.png")); // Met une image en arrière-plan grâce à un image téléchargée
        initComponents(); // Initialise les composants
        Font luckiestBase = null; // Définition d'une variable de type police d'écriture
        try {
            luckiestBase = Font.createFont(Font.TRUETYPE_FONT, new File("src/Interface/luckiest-guy/luckiestguy.ttf")); // Importation d'une nouvelle police d'écriture
            } catch (Exception e) {
                throw new RuntimeException(e); // Arrêt du programme si la police ne peut pas être chargée
            }
        // Application de la police
        jLabelStatus.setFont(luckiestBase.deriveFont(24f));
        jLabelTitle.setFont(luckiestBase.deriveFont(36f));
        
        setLocationRelativeTo(null); // Centre la fenêtre sur l'écran

        //Textes des jLabel
        jLabelTitle.setText("Salle d'attente");
        jLabelStatus.setText("Joueurs prêts : 0/4");

        // Mode solo : si le pseudo est "seul", on saute l'attente des autres joueurs.
        // invokeLater est nécessaire car launchGame() appelle dispose() — si on l'appelle
        // directement dans le constructeur, dispose() s'exécute avant setVisible(true)
        // et la fenêtre réapparaît quand même. invokeLater reporte l'exécution après
        // que setVisible(true) soit appelé par Skin, donc dispose() fonctionne correctement
        // et la fenêtre de jeu récupère le focus clavier.
        if ("seul".equalsIgnoreCase(player.getPseudo())) {
            jLabelStatus.setText("Mode solo — lancement immédiat..."); // Affichage d’un message particulier pour le mode solo
            javax.swing.SwingUtilities.invokeLater(() -> launchGame()); // Reporte le lancement du jeu à la fin du traitement graphique en cours
        } else {
            startCheckingPlayers(); // En mode multijoueur, démarrage de la vérification du nombre de joueurs
        }
    }

    // Vérifier le nombre de joueurs prêts

    private int getReadyPlayers() {
        try {
            Connection connexion = DriverManager.getConnection(
                "jdbc:mariadb://nemrod.ens2m.fr:3306/2025-2026_s2_vs1_tp1_honey_run",
                "etudiant",
                "YTDTvj9TR3CDYCmP"
            );  // Connexion à la base de données MariaDB

            PreparedStatement requete = connexion.prepareStatement(
                "SELECT COUNT(*) AS total FROM Characters WHERE Disponibilité=0"
            ); // Requête permettant de compter les joueurs prêts

            ResultSet rs = requete.executeQuery(); // Exécution de la requête SQL
            int total = 0; // Valeur utilisée par défaut si la requête en fonctionne pas

            if (rs.next()) { // Vérifie qu’une ligne de résultat est disponible
                total = rs.getInt("total"); // Stocke le résultat de la requête dans la variable "total"
            } 

            // Fermeture des ressources SQL après leur utilisation
            rs.close();
            requete.close();
            connexion.close();

            return total; // Retourne le nombre de joueurs prêts

        } catch (SQLException e) {
            e.printStackTrace(); // Affiche l'erreur SQL dans la console
            return 0; // Retourne 0 pour empêcher l'arrêt de l'application
        }
    }


    // Lancer un timer qui met à jour l'affichage
    
    private void startCheckingPlayers() {
        timer = new Timer(100, e -> { // Création d'un timer éxécuté toutes les 100 millisecondes
            int ready = getReadyPlayers(); // Stocke le nombre de joueurs prêts dans la variable "ready"
            jLabelStatus.setText("Joueurs prêts : " + ready + "/4"); // Met à jour l'affichage avec le bon nombre de joueurs

            if (ready >= 4) { // Si il y a 4 joueurs (ou plus)
                timer.stop(); // Le timer s'arrête
                startCountdown(); // Lance un compte à rebours
            }
        });

        timer.start(); // Démarre le timer
    }


    // 3. Compte à rebours avant lancement

    private void startCountdown() {
        Timer countdown = new Timer(1000, null); // Création du timer du compte à rebours
        final int[] time = {3}; // On attend 3s

        countdown.addActionListener(e -> { // Ajout de l’action exécutée toutes les secondes
            jLabelStatus.setText("La partie commence dans " + time[0]); // Affichage du temps restant avant le début de la partie
            time[0]--; // Décrémentation du temps restant
            
            if (time[0] < 0) { // Temps restant < 0
                countdown.stop(); // Arrêt du timer du compte à rebours
                launchGame(); // Ouverture de la fenêtre de jeu
            }
        });

        countdown.start(); // Démarrage du compte à rebours
    }


    // Lancer la partie

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
            
            
            ps.setInt(1, player.getId()); // Remplacement du paramètre de la requête par l’identifiant du joueur
            
            ResultSet rs = ps.executeQuery(); // Exécution de la requête

            if (!rs.next()) { // Vérifie si le joueur existe dans la base de données
                JOptionPane.showMessageDialog(this, "Joueur introuvable en base."); // Affiche un message d'erreur
                connexion.close(); // Fermeture de la connexion
                return;
            }

            // skin est stocké en String dans la DB (null possible si non choisi) → conversion sécurisée
            int skin;
            
            try { 
                skin = Integer.parseInt(rs.getString("skin")); // Conversion du numéro de skin en entier
            } catch (NumberFormatException e) { 
                skin = 0; // Valeur par défaut
            }

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


            GestionnaireJoueurs gestionnaire = new GestionnaireJoueurs();
            new FenetreDeJeu(moi, gestionnaire).setVisible(true); // Lance la fenêtre de jeu
            dispose(); // Ferme la salle d'attente

        } catch (SQLException ex) {
            ex.printStackTrace(); // Affichage de l’erreur SQL
            JOptionPane.showMessageDialog(this, "Erreur de connexion : " + ex.getMessage()); // Affiche une information d'erreur à l'utilisateur
        }
    }



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
