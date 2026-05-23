package Avatar;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Exemple de fenetre de jeu en utilisant uniquement des commandes
 *
 * @author guillaume.laurent
 */
public class FenetreDeJeu extends JFrame implements ActionListener, KeyListener {

    private BufferedImage framebuffer;
    private Graphics2D contexte;
    private JLabel jLabel1;
    private Jeu jeu;
    private Timer timer;

    public FenetreDeJeu() {
        this.setSize(1920, 1088);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.jLabel1 = new JLabel();
        this.jLabel1.setPreferredSize(new java.awt.Dimension(1920, 1088));
        this.setContentPane(this.jLabel1);
        this.pack();
        // Le focus clavier doit rester sur la fenêtre pour que les touches soient lues de façon fiable.
        this.setFocusable(true);
        this.setFocusTraversalKeysEnabled(false);

        // Le jeu dessine dans un framebuffer hors ecran, puis on affiche cette image dans le JLabel.
        this.framebuffer = new BufferedImage(this.jLabel1.getWidth(), this.jLabel1.getHeight(), BufferedImage.TYPE_INT_ARGB);
        this.jLabel1.setIcon(new ImageIcon(framebuffer));
        this.contexte = this.framebuffer.createGraphics();

        this.jeu = new Jeu();

        this.timer = new Timer(40, this);
        this.timer.start();

        this.addKeyListener(this);
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Nettoyage explicite pour eviter de conserver des pixels d'une frame precedente.
        this.contexte.setBackground(new java.awt.Color(0, 0, 0, 0));
        this.contexte.clearRect(0, 0, this.framebuffer.getWidth(), this.framebuffer.getHeight());
        this.jeu.miseAJour();
        this.jeu.rendu(this.contexte, this.framebuffer.getWidth(), this.framebuffer.getHeight());
        this.jLabel1.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FenetreDeJeu fenetre = new FenetreDeJeu();
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
