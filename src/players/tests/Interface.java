package players.tests;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import Tools.Coordinates;
import Tools.Hitbox;
import monsters.Monsters;
import multiplayer.DonneesJoueur;
import multiplayer.GestionnaireJoueurs;
import players.Player;

public class Interface {

	public static void main(String[] args) throws SQLException {
		final int windowWidth = 800;
		final int windowHeight = 600;

		// Demander le pseudo avant de lancer le jeu
		String pseudo = JOptionPane.showInputDialog(null, "Entrez votre pseudo :", "HoneyRun", JOptionPane.PLAIN_MESSAGE);
		if (pseudo == null || pseudo.trim().isEmpty()) {
			return;
		}
		pseudo = pseudo.trim();

		// Connexion et attribution d'un avatar/spawn via la DB
		GestionnaireJoueurs gestionnaire = new GestionnaireJoueurs();
		DonneesJoueur moi = gestionnaire.connecter(pseudo);
		if (moi == null) {
			return; // Partie pleine
		}

		final int joueurId = moi.id;
		final Color maCouleur = moi.getCouleur();

		// Setup du jeu avec le spawn attribué par la DB
		Hitbox monsterHitbox = new Hitbox(new Coordinates(0, 0), 40.0, 40.0);
		Monsters monster = new Monsters(380, 280, 90, monsterHitbox);
		monster.setMovementBounds(0, 0, windowWidth - monsterHitbox.getWidth(), windowHeight - monsterHitbox.getHeight());
		monster.setColor(Color.RED);

		ArrayList<Monsters> monsters = new ArrayList<>();
		monsters.add(monster);

		Hitbox playerHitbox = new Hitbox(new Coordinates(0, 0), 36.0, 36.0);
		Hitbox hiveZone = new Hitbox(new Coordinates(360, 260), 80.0, 80.0);
		Hitbox spawnZone = new Hitbox(new Coordinates(moi.spawnX, moi.spawnY), 100.0, 100.0);

		Player player = new Player(moi.spawnX, moi.spawnY, 180, playerHitbox, hiveZone, spawnZone, monsters);
		player.setMovementBounds(0, 0, windowWidth - playerHitbox.getWidth(), windowHeight - playerHitbox.getHeight());

		// Liste des joueurs distants, mise à jour par le thread DB
		AtomicReference<List<DonneesJoueur>> autresJoueurs = new AtomicReference<>(new ArrayList<>());
		AtomicBoolean partieFinie = new AtomicBoolean(false);

		JPanel panel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g;

				// Zones
				g2.setColor(new Color(255, 214, 102));
				g2.fillRect((int) hiveZone.getX(), (int) hiveZone.getY(), (int) hiveZone.getWidth(), (int) hiveZone.getHeight());
				g2.setColor(new Color(177, 241, 198));
				g2.fillRect((int) spawnZone.getX(), (int) spawnZone.getY(), (int) spawnZone.getWidth(), (int) spawnZone.getHeight());

				// Monstre
				g2.setColor(monster.getColor() != null ? monster.getColor() : Color.RED);
				g2.fillRect((int) monster.getX(), (int) monster.getY(), (int) monsterHitbox.getWidth(), (int) monsterHitbox.getHeight());

				// Autres joueurs lus depuis la DB
				g2.setFont(new Font("SansSerif", Font.BOLD, 12));
				for (DonneesJoueur j : autresJoueurs.get()) {
					if (j.id == joueurId) continue;
					g2.setColor(j.getCouleur());
					g2.fillRect((int) j.x, (int) j.y, 36, 36);
					g2.setColor(Color.BLACK);
					g2.drawString(j.nom, (int) j.x, (int) j.y - 4);
				}

				// Joueur local (position précise depuis l'objet Player)
				g2.setColor(maCouleur);
				g2.fillRect((int) player.getX(), (int) player.getY(), (int) playerHitbox.getWidth(), (int) playerHitbox.getHeight());

				// HUD
				g2.setColor(Color.BLACK);
				g2.setFont(new Font("SansSerif", Font.BOLD, 18));
				g2.drawString("Lives: " + player.getLives(), 16, 24);
				g2.drawString("Honey: " + (player.hasHoney() ? "YES" : "NO"), 16, 48);
				if (player.isHarvesting()) {
					g2.drawString("Harvesting...", 16, 72);
				}

				// Écran de fin
				if (player.isWon() || player.isGameOver()) {
					g2.setColor(new Color(0, 0, 0, 140));
					g2.fillRect(0, 0, getWidth(), getHeight());
					g2.setColor(Color.WHITE);
					g2.setFont(new Font("SansSerif", Font.BOLD, 64));
					String message = player.isWon() ? "YOU WIN" : "GAME OVER";
					int textWidth = g2.getFontMetrics().stringWidth(message);
					g2.drawString(message, (getWidth() - textWidth) / 2, getHeight() / 2);
				}
			}
		};

		panel.setBackground(Color.WHITE);
		panel.setPreferredSize(new Dimension(windowWidth, windowHeight));

		JFrame frame = new JFrame("HoneyRun — " + pseudo);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setResizable(false);
		frame.add(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);

		frame.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) { player.onKeyPressed(e.getKeyCode()); }
			@Override
			public void keyReleased(KeyEvent e) { player.onKeyReleased(e.getKeyCode()); }
		});

		// Déconnexion propre à la fermeture de la fenêtre
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				player.stopMovement();
				try { gestionnaire.deconnecter(joueurId); } catch (SQLException ex) { ex.printStackTrace(); }
				frame.dispose();
				System.exit(0);
			}
		});

		frame.setVisible(true);
		monster.startMovement();
		player.startMovement();

		// Thread de synchronisation DB toutes les 100ms
		new Thread(() -> {
			while (!partieFinie.get()) {
				try {
					// Écrire sa propre position en DB
					gestionnaire.mettreAJourPosition(
						joueurId,
						player.getX(),
						player.getY(),
						player.hasHoney(),
						player.getLives()
					);

					// Lire tous les joueurs pour le rendu
					autresJoueurs.set(gestionnaire.lireTousLesJoueurs());

					// Victoire locale : signaler et réinitialiser
					if (player.isWon() && partieFinie.compareAndSet(false, true)) {
						gestionnaire.signalerVictoire(joueurId);
						Thread.sleep(300); // laisser les autres clients détecter haswin=true
						gestionnaire.reinitialiser();
					}

					// Victoire d'un autre joueur
					String gagnant = gestionnaire.detecterVictoire();
					if (gagnant != null && partieFinie.compareAndSet(false, true)) {
						gestionnaire.reinitialiser();
						SwingUtilities.invokeLater(() -> {
							JOptionPane.showMessageDialog(frame, gagnant + " a gagné !");
							player.stopMovement();
							try { gestionnaire.deconnecter(joueurId); } catch (SQLException ex) { ex.printStackTrace(); }
							frame.dispose();
							System.exit(0);
						});
					}

					Thread.sleep(100);
				} catch (SQLException ex) {
					ex.printStackTrace();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		}, "db-sync").start();

		// Boucle de rendu ~60fps
		new Thread(() -> {
			while (true) {
				panel.repaint();
				LockSupport.parkNanos(16_000_000L);
				if (Thread.currentThread().isInterrupted()) break;
			}
		}, "render-loop").start();
	}
}