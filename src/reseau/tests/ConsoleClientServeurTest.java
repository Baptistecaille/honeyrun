package reseau.tests;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class ConsoleClientServeurTest {

    private static final String HOST = "127.0.0.1";
    private static final int JOUEUR_ID = 1;

    private ConsoleClientServeurTest() {
    }

    public static void main(String[] args) throws Exception {
        Class<?> jeuClass = Class.forName("Interface.Jeu");
        Class<?> serveurClass = Class.forName("reseau.ServeurTCP");
        Class<?> clientClass = Class.forName("reseau.ClientTCP");
        Class<?> etatJeuClass = Class.forName("reseau.EtatJeu");
        Class<?> messageEntreeClass = Class.forName("reseau.MessageEntree");

        Object serveurJeu = creerJeuServeur(jeuClass);
        Object clientJeu = creerJeuClient(jeuClass);

        Object serveur = serveurClass.getConstructor(jeuClass).newInstance(serveurJeu);
        Object client = clientClass.getConstructor(jeuClass).newInstance(clientJeu);

        serveurClass.getMethod("demarrer").invoke(serveur);
        attendre(250L);

        clientClass.getMethod("connecter", String.class).invoke(client, HOST);
        attendreConnexion(serveurClass, serveur, 1, 5000L);

        Object etatInitial = construireEtatJeu(serveurJeu, jeuClass);
        serveurClass.getMethod("broadcast", etatJeuClass).invoke(serveur, etatInitial);
        attendre(150L);

        System.out.println("Test reseau pret.");
        System.out.println("Tape z/q/s/d puis Entree pour envoyer une entree.");
        System.out.println("Tape x puis Entree pour quitter.");

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String ligne;
        while ((ligne = reader.readLine()) != null) {
            String saisie = ligne.trim().toLowerCase();
            if (saisie.isEmpty()) {
                continue;
            }

            char touche = saisie.charAt(0);
            if (touche == 'x') {
                break;
            }

            if (!estToucheValide(touche)) {
                System.out.println("Touche ignoree: " + touche);
                continue;
            }

            System.out.println("[client] touche lue: " + touche);

            Object message = creerMessageEntree(messageEntreeClass, touche);
            clientClass.getMethod("envoyerEntree", messageEntreeClass).invoke(client, message);

            attendreReceptionEntree(serveurJeu, jeuClass, touche, 3000L);

            Object etatServeur = construireEtatJeu(serveurJeu, jeuClass);
            serveurClass.getMethod("broadcast", etatJeuClass).invoke(serveur, etatServeur);
            attendreEtatClient(clientJeu, jeuClass, 1, 3000L);

            imprimerEtatServeur(serveurJeu, jeuClass);
            imprimerEtatClient(clientJeu, jeuClass);
        }

        fermer(client);
        fermer(serveur);
        attendre(100L);
        System.out.println("Test termine.");
    }

    private static boolean estToucheValide(char touche) {
        return touche == 'z' || touche == 'q' || touche == 's' || touche == 'd';
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

    private static Object creerJeuClient(Class<?> jeuClass) throws Exception {
        Object jeu = allouerInstance(jeuClass);
        Field joueursField = jeuClass.getDeclaredField("joueurs");
        joueursField.setAccessible(true);
        joueursField.set(jeu, new ArrayList<>());
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

    private static Object creerMessageEntree(Class<?> messageEntreeClass, char touche) throws Exception {
        Constructor<?> constructeur = messageEntreeClass.getConstructor(int.class, boolean.class, boolean.class, boolean.class, boolean.class);
        return constructeur.newInstance(
            JOUEUR_ID,
            touche == 'z',
            touche == 's',
            touche == 'q',
            touche == 'd'
        );
    }

    private static Object construireEtatJeu(Object jeu, Class<?> jeuClass) throws Exception {
        return jeuClass.getMethod("construireEtatJeu").invoke(jeu);
    }

    private static void imprimerEtatServeur(Object jeu, Class<?> jeuClass) throws Exception {
        List<Object> joueurs = lireJoueurs(jeu, jeuClass);
        if (joueurs.isEmpty()) {
            System.out.println("[serveur] aucun joueur");
            return;
        }

        Object joueur = joueurs.get(0);
        Method miseAJour = joueur.getClass().getMethod("miseAJour");
        miseAJour.invoke(joueur);
    }

    private static void imprimerEtatClient(Object jeu, Class<?> jeuClass) throws Exception {
        List<Object> joueurs = lireJoueurs(jeu, jeuClass);
        System.out.println("[client] joueurs synchronises: " + joueurs.size());
    }

    private static void attendreConnexion(Class<?> serveurClass, Object serveur, int minimum, long timeoutMs) throws Exception {
        Method getNombreClients = serveurClass.getMethod("getNombreClients");
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (((Integer) getNombreClients.invoke(serveur)) < minimum) {
            if (System.currentTimeMillis() >= deadline) {
                throw new IllegalStateException("Le client ne s'est pas connecte dans le delai imparti.");
            }
            attendre(50L);
        }
    }

    private static void attendreEtatClient(Object jeu, Class<?> jeuClass, int minimum, long timeoutMs) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (lireJoueurs(jeu, jeuClass).size() < minimum) {
            if (System.currentTimeMillis() >= deadline) {
                throw new IllegalStateException("Le client n'a pas recu l'etat serveur dans le delai imparti.");
            }
            attendre(50L);
        }
    }

    private static void attendreReceptionEntree(Object jeu, Class<?> jeuClass, char touche, long timeoutMs) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (!toucheEstVisible(jeu, jeuClass, touche)) {
            if (System.currentTimeMillis() >= deadline) {
                throw new IllegalStateException("L'entree du client n'a pas ete appliquee sur le serveur.");
            }
            attendre(25L);
        }
    }

    private static boolean toucheEstVisible(Object jeu, Class<?> jeuClass, char touche) throws Exception {
        List<Object> joueurs = lireJoueurs(jeu, jeuClass);
        if (joueurs.isEmpty()) {
            return false;
        }

        Object joueur = joueurs.get(0);
        Class<?> playerClass = joueur.getClass();

        return switch (touche) {
            case 'z' -> lireBooleen(playerClass, joueur, "toucheHaut");
            case 's' -> lireBooleen(playerClass, joueur, "toucheBas");
            case 'q' -> lireBooleen(playerClass, joueur, "toucheGauche");
            case 'd' -> lireBooleen(playerClass, joueur, "toucheDroite");
            default -> false;
        };
    }

    private static boolean lireBooleen(Class<?> type, Object cible, String nomChamp) throws Exception {
        Field field = type.getDeclaredField(nomChamp);
        field.setAccessible(true);
        return field.getBoolean(cible);
    }

    @SuppressWarnings("unchecked")
    private static List<Object> lireJoueurs(Object jeu, Class<?> jeuClass) throws Exception {
        Field joueursField = jeuClass.getDeclaredField("joueurs");
        joueursField.setAccessible(true);
        return (List<Object>) joueursField.get(jeu);
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

    private static void attendre(long delaiMs) throws InterruptedException {
        Thread.sleep(delaiMs);
    }
}