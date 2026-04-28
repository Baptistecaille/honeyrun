package reseau.tests;

import javax.swing.*;
import java.awt.*;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClientConnexionTest extends JFrame {

    private static final int DEFAULT_PORT = 1476;
    private static final String DEFAULT_IP = "172.16.37.70";
    private static final int TIMEOUT_MS = 3000;

    private final JTextField ipField = new JTextField(DEFAULT_IP, 20);
    private final JTextField portField = new JTextField(String.valueOf(DEFAULT_PORT), 6);
    private final JButton connectBtn = new JButton("Se connecter");
    private final JLabel statusLabel = new JLabel(" ");

    public ClientConnexionTest() {
        super("Test de connexion TCP");
        buildUI();
        connectBtn.addActionListener(e -> tenterConnexion());
        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void buildUI() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("IP serveur :"), gbc);
        gbc.gridx = 1;
        form.add(ipField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Port :"), gbc);
        gbc.gridx = 1;
        form.add(portField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        form.add(connectBtn, gbc);

        gbc.gridy = 3;
        form.add(statusLabel, gbc);

        getContentPane().add(form);
    }

    private void tenterConnexion() {
        String ip = ipField.getText().trim();
        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException ex) {
            afficherErreur("Port invalide.");
            return;
        }

        connectBtn.setEnabled(false);
        statusLabel.setForeground(Color.BLACK);
        statusLabel.setText("Connexion en cours...");

        final int finalPort = port;
        new Thread(() -> {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(ip, finalPort), TIMEOUT_MS);
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setForeground(new Color(0, 150, 0));
                    statusLabel.setText("Connecté à " + ip + ":" + finalPort);
                    connectBtn.setEnabled(true);
                });
            } catch (SocketTimeoutException ex) {
                SwingUtilities.invokeLater(() ->
                    afficherErreur("Timeout — hôte non joignable sur ce réseau"));
            } catch (ConnectException ex) {
                SwingUtilities.invokeLater(() ->
                    afficherErreur("Serveur injoignable (port fermé ou serveur éteint)"));
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                    afficherErreur(ex.getMessage()));
            } finally {
                SwingUtilities.invokeLater(() -> connectBtn.setEnabled(true));
            }
        }).start();
    }

    private void afficherErreur(String message) {
        statusLabel.setForeground(Color.RED);
        statusLabel.setText(message);
        connectBtn.setEnabled(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientConnexionTest().setVisible(true));
    }
}
