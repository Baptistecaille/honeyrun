package reseau.tests;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class ServeurMain {

    private static final int JOUEUR_ID = 1;

    private ServeurMain() {
    }

    public static void main(String[] args) throws Exception {
        Class<?> jeuClass = Class.forName("Interface.Jeu");
        Class<?> serveurClass = Class.forName("reseau.ServeurTCP");
        Class<?> etatJeuClass = Class.forName("reseau.EtatJeu");

        Object jeu = creerJeuServeur(jeuClass);
        Object serveur = serveurClass.getConstructor(jeuClass).newInstance(jeu);
        jeuClass.getMethod("setServeurTCP", serveurClass).invoke(jeu, serveur);

        serveurClass.getMethod("demarrer").invoke(serveur);

        int port = serveurClass.getField("PORT").getInt(null);
        System.out.println("Serveur reseau lance sur le port " + port + ".");
        System.out.println("En attente d'un client. Ctrl-C pour arreter.");

        try {
            while (true) {
                nettoyerClientsFermees(serveur, serveurClass);
                if (!aDesClientsActifs(serveur, serveurClass)) {
                    Thread.sleep(50L);
                    continue;
                }

                actualiserJoueurs(jeu, jeuClass);
                Object etat = jeuClass.getMethod("construireEtatJeu").invoke(jeu);
                serveurClass.getMethod("broadcast", etatJeuClass).invoke(serveur, etat);
                Thread.sleep(50L);
            }
        } finally {
            fermer(serveur);
        }
    }

    private static Object creerJeuServeur(Class<?> jeuClass) throws Exception {
        Object jeu = allouerInstance(jeuClass);
        Field joueursField = jeuClass.getDeclaredField("joueurs");
        joueursField.setAccessible(true);

        List<Object> joueurs = new ArrayList<>();
        joueurs.add(creerJoueur());
        joueursField.set(jeu, joueurs);
        return jeu;
    }

    private static Object creerJoueur() throws Exception {
        Class<?> playerClass = Class.forName("Interface.Player");
        Object joueur = playerClass.getDeclaredConstructor().newInstance();
        playerClass.getMethod("setId", int.class).invoke(joueur, JOUEUR_ID);

        Field nameField = playerClass.getDeclaredField("name");
        nameField.setAccessible(true);
        nameField.set(joueur, "joueur-" + JOUEUR_ID);
        return joueur;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> lireJoueurs(Object jeu, Class<?> jeuClass) throws Exception {
        Field joueursField = jeuClass.getDeclaredField("joueurs");
        joueursField.setAccessible(true);
        return (List<Object>) joueursField.get(jeu);
    }

    private static void actualiserJoueurs(Object jeu, Class<?> jeuClass) throws Exception {
        for (Object joueur : lireJoueurs(jeu, jeuClass)) {
            Method miseAJour = joueur.getClass().getMethod("miseAJour");
            miseAJour.invoke(joueur);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Object> lireClients(Object serveur, Class<?> serveurClass) throws Exception {
        Field clientsField = serveurClass.getDeclaredField("clients");
        clientsField.setAccessible(true);
        return (List<Object>) clientsField.get(serveur);
    }

    private static void nettoyerClientsFermees(Object serveur, Class<?> serveurClass) throws Exception {
        List<Object> clients = lireClients(serveur, serveurClass);
        for (Iterator<Object> it = clients.iterator(); it.hasNext();) {
            Object threadClient = it.next();
            Socket socket = lireSocket(threadClient);
            if (socket == null || socket.isClosed()) {
                it.remove();
            }
        }
    }

    private static boolean aDesClientsActifs(Object serveur, Class<?> serveurClass) throws Exception {
        for (Object threadClient : lireClients(serveur, serveurClass)) {
            Socket socket = lireSocket(threadClient);
            if (socket != null && !socket.isClosed()) {
                return true;
            }
        }
        return false;
    }

    private static Socket lireSocket(Object threadClient) throws Exception {
        Field socketField = threadClient.getClass().getDeclaredField("socket");
        socketField.setAccessible(true);
        return (Socket) socketField.get(threadClient);
    }

    private static Object allouerInstance(Class<?> type) throws Exception {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Object unsafe = unsafeField.get(null);
        Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
        return allocateInstance.invoke(unsafe, type);
    }

    private static void fermer(Object cible) {
        try {
            cible.getClass().getMethod("fermer").invoke(cible);
        } catch (Exception ignored) {
        }
    }
}