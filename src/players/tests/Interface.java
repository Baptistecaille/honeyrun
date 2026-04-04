package players.tests;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.concurrent.locks.LockSupport;

import javax.swing.JFrame;
import javax.swing.JPanel;

import monsters.Coordinates;
import monsters.Hitbox;
import monsters.Monsters;
import players.Player;

public class Interface {

	public static void main(String[] args) {
		final int windowWidth = 800;
		final int windowHeight = 600;

		Hitbox monsterHitbox = new Hitbox(new Coordinates(0, 0), 40.0, 40.0);
		Monsters monster = new Monsters(380, 280, 90, monsterHitbox);
		monster.setMovementBounds(0, 0, windowWidth - monsterHitbox.getWidth(), windowHeight - monsterHitbox.getHeight());
		monster.setColor(Color.RED);

		ArrayList<Monsters> monsters = new ArrayList<>();
		monsters.add(monster);

		Hitbox playerHitbox = new Hitbox(new Coordinates(0, 0), 36.0, 36.0);
		Hitbox hiveZone = new Hitbox(new Coordinates(360, 260), 80.0, 80.0);
		Hitbox spawnZone = new Hitbox(new Coordinates(20, 20), 100.0, 100.0);

		Player player = new Player(40, 40, 180, playerHitbox, hiveZone, spawnZone, monsters);
		player.setMovementBounds(0, 0, windowWidth - playerHitbox.getWidth(), windowHeight - playerHitbox.getHeight());

		monster.startMovement();
		player.startMovement();

		JPanel panel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);

				Graphics2D g2 = (Graphics2D) g;

				g2.setColor(new Color(255, 214, 102));
				g2.fillRect((int) hiveZone.getX(), (int) hiveZone.getY(), (int) hiveZone.getWidth(), (int) hiveZone.getHeight());

				g2.setColor(new Color(177, 241, 198));
				g2.fillRect((int) spawnZone.getX(), (int) spawnZone.getY(), (int) spawnZone.getWidth(), (int) spawnZone.getHeight());

				g2.setColor(monster.getColor() != null ? monster.getColor() : Color.RED);
				g2.fillRect((int) monster.getX(), (int) monster.getY(), (int) monsterHitbox.getWidth(), (int) monsterHitbox.getHeight());

				g2.setColor(new Color(47, 98, 255));
				g2.fillRect((int) player.getX(), (int) player.getY(), (int) playerHitbox.getWidth(), (int) playerHitbox.getHeight());

				g2.setColor(Color.BLACK);
				g2.setFont(new Font("SansSerif", Font.BOLD, 18));
				g2.drawString("Lives: " + player.getLives(), 16, 24);
				g2.drawString("Honey: " + (player.hasHoney() ? "YES" : "NO"), 16, 48);

				if (player.isHarvesting()) {
					g2.drawString("Harvesting...", 16, 72);
				}

				if (player.isWon() || player.isGameOver()) {
					g2.setColor(new Color(0, 0, 0, 140));
					g2.fillRect(0, 0, getWidth(), getHeight());

					g2.setColor(Color.WHITE);
					g2.setFont(new Font("SansSerif", Font.BOLD, 64));

					String message = player.isWon() ? "YOU WIN" : "GAME OVER";
					int textWidth = g2.getFontMetrics().stringWidth(message);
					int x = (getWidth() - textWidth) / 2;
					int y = getHeight() / 2;
					g2.drawString(message, x, y);
				}
			}
		};

		panel.setBackground(Color.WHITE);
		panel.setPreferredSize(new Dimension(windowWidth, windowHeight));

		JFrame frame = new JFrame("Honeyrun Player + Monster Test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.add(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);

		frame.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				player.onKeyPressed(e.getKeyCode());
			}

			@Override
			public void keyReleased(KeyEvent e) {
				player.onKeyReleased(e.getKeyCode());
			}
		});

		frame.setVisible(true);

		new Thread(() -> {
			while (true) {
				panel.repaint();
				LockSupport.parkNanos(16_000_000L);
				if (Thread.currentThread().isInterrupted()) {
					break;
				}
			}
		}, "render-loop").start();
	}
}