package reseau;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import Interface.Jeu;

public class ServeurTCP {

    public static final int PORT = 1476;
    private static final int MAX_CLIENTS = 3;

    private final Jeu jeu;
    private final List<ThreadClient> clients = new ArrayList<>();
    private ServerSocket serverSocket;

    public ServeurTCP(Jeu jeu) {
        this.jeu = jeu;
    }

    public void demarrer() throws IOException {
        serverSocket = new ServerSocket(PORT);
        new Thread(this::accepterClients).start();
    }

    private void accepterClients() {
        int prochainId = 1;
        while (clients.size() < MAX_CLIENTS) {
            try {
                Socket socket = serverSocket.accept();
                ThreadClient thread = new ThreadClient(socket, jeu, this, prochainId);
                ajouterClient(thread);
                thread.start();
                System.out.println("Joueur " + prochainId + " connecté.");
                prochainId++;
            } catch (IOException e) {
                System.out.println("Serveur: arrêt de l'écoute.");
                break;
            }
        }
    }

    public void broadcast(EtatJeu etat) {
        List<ThreadClient> copieClients;
        synchronized (this) {
            copieClients = new ArrayList<>(clients);
        }

        for (ThreadClient client : copieClients) {
            client.envoyerEtat(etat);
        }
    }

    public synchronized int getNombreClients() {
        return clients.size();
    }

    public synchronized void ajouterClient(ThreadClient client) {
        clients.add(client);
    }

    public synchronized void retirerClient(ThreadClient client) {
        clients.remove(client);
    }

    public void fermer() {
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
