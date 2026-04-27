package reseau;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import Interface.Jeu;

public class ClientTCP {

    public static final int PORT = 1476;

    private final Jeu jeu;
    private Socket socket;
    private ObjectOutputStream out;

    public ClientTCP(Jeu jeu) {
        this.jeu = jeu;
    }

    public void connecter(String ip) throws IOException {
        socket = new Socket(ip, PORT);
        out = new ObjectOutputStream(socket.getOutputStream());
        new Thread(this::lireEtats).start();
    }

    public void envoyerEntree(MessageEntree msg) {
        try {
            out.writeObject(msg);
            out.reset();
            out.flush();
        } catch (IOException e) {
            System.out.println("Erreur envoi entrée au serveur.");
        }
    }

    private void lireEtats() {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            while (true) {
                EtatJeu etat = (EtatJeu) in.readObject();
                jeu.appliquerEtat(etat);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Serveur déconnecté.");
            fermer();
        }
    }

    public void fermer() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
