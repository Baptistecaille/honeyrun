package reseau;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import Interface.Jeu;

public class ThreadClient extends Thread {

    private final Socket socket;
    private final Jeu jeu;
    private final ServeurTCP serveur;
    private final int joueurId;
    private ObjectOutputStream out;
    private volatile boolean deconnecte;

    public ThreadClient(Socket socket, Jeu jeu, int joueurId) throws IOException {
        this(socket, jeu, null, joueurId);
    }

    public ThreadClient(Socket socket, Jeu jeu, ServeurTCP serveur, int joueurId) throws IOException {
        this.socket = socket;
        this.jeu = jeu;
        this.serveur = serveur;
        this.joueurId = joueurId;
        this.out = new ObjectOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            while (true) {
                MessageEntree msg = (MessageEntree) in.readObject();
                System.out.println("Joueur " + joueurId + " a presse: " + decrireTouche(msg));
                jeu.appliquerEntree(msg);
            }
        } catch (IOException | ClassNotFoundException e) {
            if (!deconnecte) {
                System.out.println("Joueur " + joueurId + " déconnecté.");
                deconnecter();
            }
        }
    }

    public void envoyerEtat(EtatJeu etat) {
        try {
            out.writeObject(etat);
            out.reset();
            out.flush();
        } catch (IOException e) {
            if (!deconnecte) {
                System.out.println("Erreur envoi état au joueur " + joueurId);
                deconnecter();
            }
        }
    }

    private void deconnecter() {
        deconnecte = true;

        if (serveur != null) {
            serveur.retirerClient(this);
        }

        jeu.retirerJoueur(joueurId);

        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    private String decrireTouche(MessageEntree msg) {
        if (msg.toucheHaut) {
            return "z";
        }
        if (msg.toucheBas) {
            return "s";
        }
        if (msg.toucheGauche) {
            return "q";
        }
        if (msg.toucheDroite) {
            return "d";
        }
        return "inconnue";
    }
}
