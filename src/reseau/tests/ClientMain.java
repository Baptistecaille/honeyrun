package reseau.tests;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

public final class ClientMain {

    private static final int JOUEUR_ID = 1;
    private static final String HOST_PAR_DEFAUT = "127.0.0.1";

    private ClientMain() {
    }

    public static void main(String[] args) throws Exception {
        Class<?> jeuClass = Class.forName("Interface.Jeu");
        Class<?> clientClass = Class.forName("reseau.ClientTCP");
        Class<?> messageEntreeClass = Class.forName("reseau.MessageEntree");

        Object jeu = creerJeuClient(jeuClass);
        Object client = clientClass.getConstructor(jeuClass).newInstance(jeu);

        String host = args.length > 0 ? args[0] : HOST_PAR_DEFAUT;
        clientClass.getMethod("connecter", String.class).invoke(client, host);

        System.out.println("Client reseau connecte a " + host + ".");
        System.out.println("Tape z/q/s/d puis Entree pour envoyer une entree.");
        System.out.println("Tape x puis Entree pour quitter.");

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String ligne;
        try {
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
                Thread.sleep(100L);
            }
        } finally {
            fermer(client);
        }
    }

    private static boolean estToucheValide(char touche) {
        return touche == 'z' || touche == 'q' || touche == 's' || touche == 'd';
    }

    private static Object creerJeuClient(Class<?> jeuClass) throws Exception {
        Object jeu = allouerInstance(jeuClass);
        Field joueursField = jeuClass.getDeclaredField("joueurs");
        joueursField.setAccessible(true);
        joueursField.set(jeu, new ArrayList<>());
        return jeu;
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