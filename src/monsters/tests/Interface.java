package monsters.tests;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

import monsters.Coordinates;
import monsters.Hitbox;
import monsters.Monsters;

public class Interface {

    public static void main(String[] args) {
        Hitbox hitbox = new Hitbox(new Coordinates(0, 0), 40.0, 40.0);
        Monsters monster = new Monsters(380, 280, 60, hitbox);
        monster.setColor(Color.RED);
        monster.startMovement();

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(monster.getColor() != null ? monster.getColor() : Color.RED);
                int x = (int) monster.getX();
                int y = (int) monster.getY();
                int w = (int) hitbox.getWidth();
                int h = (int) hitbox.getHeight();
                g.fillRect(x, y, w, h);
            }
        };
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(800, 600));

        JFrame frame = new JFrame("Honeyrun Monster Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Repaint loop ~60fps
        new Thread(() -> {
            while (true) {
                panel.repaint();
                try { Thread.sleep(16); } catch (InterruptedException e) { break; } // 16ms is arround 60 fps
            }
        }, "render-loop").start();
    }
}
