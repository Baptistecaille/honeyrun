package Avatar;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import multiplayer.DonneesJoueur;
import multiplayer.DonneesMonstre;
import multiplayer.GestionnaireJoueurs;
import multiplayer.GestionnairesMonstres;

/**
 * Exemple de fenetre de jeu en utilisant uniquement des commandes
 *
 * @author Anaïs Lamas, Baptiste Caillerie, Eloan Lehec, Clémentine Poussier
 */
public class FenetreDeJeu extends JFrame implements ActionListener, KeyListener {

    private BufferedImage framebuffer;
    private Graphics2D contexte;
    private JLabel jLabel1;
    private Jeu jeu;
    private Timer timer;
    private GestionnaireJoueurs gestionnaire;
    private int joueurId;
    private volatile boolean partieFinie;
    private volatile ArrayList<DonneesJoueur> autresJoueurs;
    private GestionnairesMonstres gestionnaireMonstres;
    private List<DonneesMonstre> referencesMonstres;
    private BufferedImage[] spritesParSkin;


    public FenetreDeJeu(DonneesJoueur moi, GestionnaireJoueurs gestionnaire) {
        this.setSize(GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.jLabel1 = new JLabel();
        this.jLabel1.setPreferredSize(new java.awt.Dimension(GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT));
        this.setContentPane(this.jLabel1);
        this.pack();
        this.setFocusable(true);
        this.setFocusTraversalKeysEnabled(false);

        this.framebuffer = new BufferedImage(this.jLabel1.getWidth(), this.jLabel1.getHeight(), BufferedImage.TYPE_INT_ARGB);
        this.jLabel1.setIcon(new ImageIcon(framebuffer));
        this.contexte = this.framebuffer.createGraphics();

        this.jeu = new Jeu(moi.spawnX, moi.spawnY, moi.nom, moi.avatar);
        // donne au joueur local accès à la base de données pour pouvoir voler le miel
        this.jeu.getPlayer().setGestionnaire(gestionnaire, moi.id);

        this.gestionnaire = gestionnaire;
        this.joueurId = moi.id;
        this.gestionnaireMonstres = new GestionnairesMonstres();
        this.referencesMonstres = new ArrayList<>();
        try {
            this.referencesMonstres = gestionnaireMonstres.initialiser();
            this.jeu.appliquerDonneesMonstres(referencesMonstres);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        this.partieFinie = false;
        this.autresJoueurs = new ArrayList<>();
        this.spritesParSkin = chargerSpritesJoueurs();


        this.partieFinie = false;
        this.autresJoueurs = new ArrayList<>();
        this.spritesParSkin = chargerSpritesJoueurs();

        // Supprime la ligne du joueur en base même en cas de crash ou de fermeture brutale de la fenetre, où arreter().
        final int idPourHook = moi.id;
        final GestionnaireJoueurs gestionnaireHook = gestionnaire;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { gestionnaireHook.deconnecter(idPourHook); } catch (Exception ignored) {}
        }, "cleanup-hook"));

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                arreter();
            }
        });

        this.timer = new Timer(GameConstants.RENDER_TIMER_DELAY_MS, this);
        this.timer.start();

        this.addKeyListener(this);
        SwingUtilities.invokeLater(new Runnable() { // Runnable sert à différer l'exécution du code jusqu'à ce que la fenêtre soit affichée, pour que requestFocusInWindow fonctionne.
            @Override
            public void run() {
                requestFocusInWindow();
            }
        });

        demarrerThreadSync();
    }

    private BufferedImage redimensionner(BufferedImage img, int largeur, int hauteur) {
        BufferedImage out = new BufferedImage(largeur, hauteur, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = out.createGraphics();
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                             java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(img, 0, 0, largeur, hauteur, null);
        g2d.dispose();
        return out;
    }

    private BufferedImage[] chargerSpritesJoueurs() {
        String[] noms = {null, "Araignee", "Mantereligieuse", "Scarabe", "Criquet"};
        BufferedImage[] sprites = new BufferedImage[5];

        for (int skin = 1; skin <= 4; skin++) {
            try {
                java.net.URL resource = getClass().getResource("/resources/" + noms[skin] + ".png");
                if (resource != null) {
                    BufferedImage img = ImageIO.read(resource);
                    if (img != null) {
                        sprites[skin] = redimensionner(img, GameConstants.SPRITE_SIZE, GameConstants.SPRITE_SIZE);
                        continue;
                    }
                }

                File fallback = new File("src/resources/" + noms[skin] + ".png");
                if (fallback.exists()) {
                    BufferedImage img = ImageIO.read(fallback);
                    if (img != null) {
                        sprites[skin] = redimensionner(img, GameConstants.SPRITE_SIZE, GameConstants.SPRITE_SIZE);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return sprites;
    }

    /**
 * Rend tous les avatars disponibles à la fin de la partie.
 */
    private void reinitialiserDisponibilites() {

        try (
            Connection connexion = DriverManager.getConnection(
                "jdbc:mariadb://nemrod.ens2m.fr:3306/2025-2026_s2_vs1_tp1_honey_run",
                "etudiant",
                "YTDTvj9TR3CDYCmP"
            );

            PreparedStatement requete = connexion.prepareStatement(
                "UPDATE Characters SET Disponibilité = 1"
            )
        ) {
            requete.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    private void arreter() {
        partieFinie = true;

        jeu.stopMonstres();
        jeu.getPlayer().stopMovement();
        try { gestionnaire.deconnecter(joueurId); } catch (SQLException ex) { ex.printStackTrace(); }
        dispose();
        System.exit(0);
    }

    private synchronized boolean marquerPartieFinie() {
        if (partieFinie) {
            return false;
        }
        partieFinie = true;
        return true;
    }

    private DonneesJoueur creerDonneesJoueurLocal() {
        Player player = jeu.getPlayer();
        return new DonneesJoueur(
            joueurId,
            player.getName(),
            player.getX(),
            player.getY(),
            player.getSpawn().getX(),
            player.getSpawn().getY(),
            0,
            player.hasHoney(),
            player.isWon(),
            player.getLives()
        );
    }

    private boolean estClientAutoritaireMonstres(ArrayList<DonneesJoueur> joueursSynchronises) {
        int idMinimum = joueurId;
        for (DonneesJoueur joueur : joueursSynchronises) {
            if (joueur.id < idMinimum) {
                idMinimum = joueur.id;
            }
        }
        return joueurId == idMinimum;
    }

    private void synchroniserMonstres(ArrayList<DonneesJoueur> joueursSynchronises) throws SQLException {
        if (gestionnaireMonstres == null) {
            return;
        }
        if (referencesMonstres == null || referencesMonstres.size() != GestionnairesMonstres.NOMBRE_MONSTRES) {
            referencesMonstres = gestionnaireMonstres.initialiser();
            jeu.appliquerDonneesMonstres(referencesMonstres);
        }

        boolean autoritaire = estClientAutoritaireMonstres(joueursSynchronises);
        jeu.setMonstresAutoritaires(autoritaire);
        if (autoritaire) {
            gestionnaireMonstres.mettreAJourPositions(jeu.creerDonneesMonstres(referencesMonstres));
        } else {
            referencesMonstres = gestionnaireMonstres.lireTousLesMonstres();
            jeu.appliquerDonneesMonstres(referencesMonstres);
        }
    }

    private void demarrerThreadSync() {
        new Thread(new Runnable() { // Runnable permet de définir le code à exécuter dans le thread sans avoir à créer une classe séparée.
            @Override
            public void run() {
                while (!partieFinie) {
                    try {
                        // On écrit notre état en DB EN PREMIER, puis on lit.
                        // Si on lit avant d'écrire, un hasHoney=true fraîchement récolté n'est pas encore en DB,
                        // et la détection de vol ci-dessous appelle onMielVole() à tort.
                        gestionnaire.mettreAJourPosition(
                            joueurId,
                            jeu.getPlayer().getX(),
                            jeu.getPlayer().getY(),
                            jeu.getPlayer().getLives()
                        );

                        ArrayList<DonneesJoueur> joueursSynchronises = new ArrayList<>(gestionnaire.lireTousLesJoueurs());
                        autresJoueurs = joueursSynchronises;
                        synchroniserMonstres(joueursSynchronises);

                        // Si la DB indique qu'on n'a plus le miel alors qu'on croyait l'avoir → vol détecté
                        for (DonneesJoueur j : joueursSynchronises) {
                            if (j.id == joueurId && !j.hasHoney && jeu.getPlayer().hasHoney()) {
                                jeu.getPlayer().onMielVole();
                                break;
                            }
                        }

                        // On transmet la liste des joueurs au Jeu et au Player pour l'affichage et les collisions
                        jeu.setAutresJoueurs(joueursSynchronises);
                        ArrayList<DonneesJoueur> autresSeulement = new ArrayList<>();
                        for (DonneesJoueur j : joueursSynchronises) {
                            if (j.id != joueurId) autresSeulement.add(j);
                        }
                        jeu.getPlayer().setAutresJoueurs(autresSeulement);

                        ArrayList<DonneesJoueur> joueursPourMonstres = new ArrayList<>();
                        for (int i = 0; i < joueursSynchronises.size(); i++) {
                            DonneesJoueur joueur = joueursSynchronises.get(i);
                            if (joueur.id != joueurId) {
                                joueursPourMonstres.add(joueur);
                            }
                        }
                        joueursPourMonstres.add(creerDonneesJoueurLocal());
                        jeu.mettreAJourJoueursPourMonstres(joueursPourMonstres);

                        if (jeu.getPlayer().isWon() && marquerPartieFinie()) {
                            gestionnaire.signalerVictoire(joueurId);
                            gestionnaire.deconnecter(joueurId);
                            reinitialiserDisponibilites();
                            gestionnaire.reinitialiser();
                            if (gestionnaireMonstres != null) gestionnaireMonstres.reinitialiser();
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    JOptionPane.showMessageDialog(FenetreDeJeu.this, "Vous avez gagné !");
                                    arreter();
                                }
                            });
                        }

                        if (!partieFinie) {
                            String gagnant = gestionnaire.detecterVictoire();
                            if (gagnant != null && marquerPartieFinie()) {
                                final String nomGagnant = gagnant; // final nécessaire pour l'utiliser dans le Runnable
                                gestionnaire.deconnecter(joueurId);
                                reinitialiserDisponibilites(); // Remet les disponibilités à 1
                                gestionnaire.reinitialiser();
                                if (gestionnaireMonstres != null) gestionnaireMonstres.reinitialiser();
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        JOptionPane.showMessageDialog(FenetreDeJeu.this, nomGagnant + " a gagné !");
                                        arreter();
                                    }
                                });
                            }
                        }

                        Thread.sleep(16); // ~60 FPS
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }, "db-sync").start();
    }

    private void rendreAutresJoueurs() {
        ArrayList<DonneesJoueur> joueurs = autresJoueurs;
        contexte.setFont(new Font("SansSerif", Font.BOLD, 12));
        for (DonneesJoueur j : joueurs) {
            if (j.id == joueurId) continue;
            int screenX = jeu.worldToScreenX(j.x);
            int screenY = jeu.worldToScreenY(j.y);
            BufferedImage sprite = null;
            if (j.avatar >= 1 && j.avatar < spritesParSkin.length) {
                sprite = spritesParSkin[j.avatar];
            }
            if (sprite != null) {
                contexte.drawImage(sprite, screenX, screenY, GameConstants.SPRITE_SIZE, GameConstants.SPRITE_SIZE, null);
            } else {
                contexte.setColor(Color.MAGENTA);
                contexte.fillRect(screenX, screenY, GameConstants.SPRITE_SIZE, GameConstants.SPRITE_SIZE);
            }
            if (j.nom != null) {
                contexte.setColor(Color.BLACK);
                contexte.drawString(j.nom, screenX, screenY - 4);
            }
            // Si ce joueur porte le miel, on affiche le pot en mini au-dessus de son sprite
            if (j.hasHoney) {
                BufferedImage honeyImg = jeu.getHoneySprite();
                if (honeyImg != null) {
                    contexte.drawImage(honeyImg,
                        screenX + GameConstants.SPRITE_SIZE / 2 - 15,
                        screenY - 35,
                        30, 30, null);
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.contexte.setBackground(new java.awt.Color(0, 0, 0, 0));
        this.contexte.clearRect(0, 0, this.framebuffer.getWidth(), this.framebuffer.getHeight());
        this.jeu.miseAJour();
        this.jeu.rendu(this.contexte, this.framebuffer.getWidth(), this.framebuffer.getHeight());
        rendreAutresJoueurs();
        this.jLabel1.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Interface.Accueil().setVisible(true);
            }
        });
    }

    @Override
    public void keyTyped(KeyEvent evt) {
    }

    @Override
    public void keyPressed(KeyEvent evt) {
        // Flèches et ZQSD pilotent le joueur local.
        if (evt.getKeyCode() == KeyEvent.VK_RIGHT || evt.getKeyCode() == KeyEvent.VK_D) {
            this.jeu.getPlayer().setToucheDroite(true);
        }
        if (evt.getKeyCode() == KeyEvent.VK_LEFT || evt.getKeyCode() == KeyEvent.VK_Q) {
            this.jeu.getPlayer().setToucheGauche(true);
        }
        if (evt.getKeyCode() == KeyEvent.VK_DOWN || evt.getKeyCode() == KeyEvent.VK_S) {
            this.jeu.getPlayer().setToucheBas(true);
        }
        if (evt.getKeyCode() == KeyEvent.VK_UP || evt.getKeyCode() == KeyEvent.VK_Z) {
            this.jeu.getPlayer().setToucheHaut(true);
        }
    }

    @Override
    public void keyReleased(KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_RIGHT || evt.getKeyCode() == KeyEvent.VK_D) {
            this.jeu.getPlayer().setToucheDroite(false);
        }
        if (evt.getKeyCode() == KeyEvent.VK_LEFT || evt.getKeyCode() == KeyEvent.VK_Q) {
            this.jeu.getPlayer().setToucheGauche(false);
        }
        if (evt.getKeyCode() == KeyEvent.VK_DOWN || evt.getKeyCode() == KeyEvent.VK_S) {
            this.jeu.getPlayer().setToucheBas(false);
        }
        if (evt.getKeyCode() == KeyEvent.VK_UP || evt.getKeyCode() == KeyEvent.VK_Z) {
            this.jeu.getPlayer().setToucheHaut(false);
        }
    }

}

  
