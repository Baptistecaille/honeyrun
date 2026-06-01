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
import java.util.HashMap;
import java.util.List; // for mapping avatar types to sprites
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean; // for logging
import java.util.concurrent.atomic.AtomicReference; // for logging
import java.util.logging.Level; // used for thread-safe boolean flag
import java.util.logging.Logger; // used for thread-safe reference to the list of other players

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import multiplayer.DonneesJoueur;
import multiplayer.GestionnaireJoueurs;

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
    private AtomicBoolean partieFinie;
    private AtomicReference<List<DonneesJoueur>> autresJoueurs;
    private Map<Integer, BufferedImage> spritesParSkin;

    public FenetreDeJeu(DonneesJoueur moi, GestionnaireJoueurs gestionnaire) {
        this.setSize(1920, 1088);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.jLabel1 = new JLabel();
        this.jLabel1.setPreferredSize(new java.awt.Dimension(1920, 1088));
        this.setContentPane(this.jLabel1);
        this.pack();
        this.setFocusable(true);
        this.setFocusTraversalKeysEnabled(false);

        this.framebuffer = new BufferedImage(this.jLabel1.getWidth(), this.jLabel1.getHeight(), BufferedImage.TYPE_INT_ARGB);
        this.jLabel1.setIcon(new ImageIcon(framebuffer));
        this.contexte = this.framebuffer.createGraphics();

        this.jeu = new Jeu(moi.spawnX, moi.spawnY, moi.nom);

        this.gestionnaire = gestionnaire;
        this.joueurId = moi.id;
        this.partieFinie = new AtomicBoolean(false);
        this.autresJoueurs = new AtomicReference<>(java.util.Collections.emptyList());
        this.spritesParSkin = chargerSpritesJoueurs();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                arreter();
            }
        });

        this.timer = new Timer(40, this);
        this.timer.start();

        this.addKeyListener(this);
        SwingUtilities.invokeLater(this::requestFocusInWindow);

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

    private Map<Integer, BufferedImage> chargerSpritesJoueurs() {
        String[] noms = {null, "Abeille", "Araignee", "Criquet", "Scarabe"};
        Map<Integer, BufferedImage> map = new HashMap<>();
        for (int skin = 1; skin <= 4; skin++) {
            try {
                var resource = getClass().getResource("/resources/" + noms[skin] + ".png");
                if (resource != null) {
                    BufferedImage img = ImageIO.read(resource);
                    if (img != null) { map.put(skin, redimensionner(img, 96, 96)); continue; }
                }
                File fallback = new File("src/resources/" + noms[skin] + ".png");
                if (fallback.exists()) {
                    BufferedImage img = ImageIO.read(fallback);
                    if (img != null) { map.put(skin, redimensionner(img, 96, 96)); }
                }
            } catch (IOException ex) {
                Logger.getLogger(FenetreDeJeu.class.getName()).log(Level.SEVERE, null, ex); // Get log if image loading fails
            }
        }
        return map;
    }

    private void arreter() {
        partieFinie.set(true);
        jeu.stopMonstres();
        jeu.getPlayer().stopMovement();
        try { gestionnaire.deconnecter(joueurId); } catch (SQLException ex) { ex.printStackTrace(); }
        dispose();
        System.exit(0);
    }

    private void demarrerThreadSync() {
        new Thread(() -> {
            while (!partieFinie.get()) {
                try {
                    gestionnaire.mettreAJourPosition(
                        joueurId,
                        jeu.getPlayer().getX(),
                        jeu.getPlayer().getY(),
                        jeu.getPlayer().hasHoney(),
                        jeu.getPlayer().getLives()
                    );
                    autresJoueurs.set(gestionnaire.lireTousLesJoueurs());

                    if (jeu.getPlayer().isWon() && partieFinie.compareAndSet(false, true)) {
                        gestionnaire.signalerVictoire(joueurId);
                        gestionnaire.deconnecter(joueurId);
                        gestionnaire.reinitialiser();
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(FenetreDeJeu.this, "Vous avez gagné !");
                            arreter();
                        });
                    }

                    if (!partieFinie.get()) {
                        String gagnant = gestionnaire.detecterVictoire();
                        if (gagnant != null && partieFinie.compareAndSet(false, true)) {
                            gestionnaire.deconnecter(joueurId);
                            gestionnaire.reinitialiser();
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(FenetreDeJeu.this, gagnant + " a gagné !");
                                arreter();
                            });
                        }
                    }

                    Thread.sleep(16);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "db-sync").start();
    }

    private void rendreAutresJoueurs() {
        List<DonneesJoueur> joueurs = autresJoueurs.get();
        contexte.setFont(new Font("SansSerif", Font.BOLD, 12));
        for (DonneesJoueur j : joueurs) {
            if (j.id == joueurId) continue;
            int screenX = jeu.worldToScreenX(j.x);
            int screenY = jeu.worldToScreenY(j.y);
            BufferedImage sprite = spritesParSkin.get(j.avatar);
            if (sprite != null) {
                contexte.drawImage(sprite, screenX, screenY, 96, 96, null);
            } else {
                contexte.setColor(Color.MAGENTA);
                contexte.fillRect(screenX, screenY, 96, 96);
            }
            if (j.nom != null) {
                contexte.setColor(Color.BLACK);
                contexte.drawString(j.nom, screenX, screenY - 4);
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
        String pseudo = JOptionPane.showInputDialog(null, "Entrez votre pseudo :", "HoneyRun", JOptionPane.PLAIN_MESSAGE);
        if (pseudo == null || pseudo.trim().isEmpty()) return;
        pseudo = pseudo.trim();

        GestionnaireJoueurs gestionnaire = new GestionnaireJoueurs();
        DonneesJoueur moi;
        try {
            moi = gestionnaire.connecter(pseudo);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        if (moi == null) return;

        final DonneesJoueur joueur = moi;
        final GestionnaireJoueurs g = gestionnaire;
        SwingUtilities.invokeLater(() -> {
            FenetreDeJeu fenetre = new FenetreDeJeu(joueur, g);
            fenetre.setVisible(true);
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



  