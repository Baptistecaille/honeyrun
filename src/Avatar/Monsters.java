package Avatar;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

import TileMapping.CollisionMap;
import Tools.Coordinates;
import Tools.Hitbox;
import multiplayer.DonneesJoueur;

public class Monsters extends Avatar {

    private static final double TILE_SIZE = GameConstants.TILE_SIZE;
    public static final int AGGRO_RANGE_BLOCKS = GameConstants.AGGRO_RANGE_BLOCKS;

    private Coordinates velocity; // vitesse vu comme un vecteur de déplacement d'où l'utilisation de Coordinates 
    private Coordinates heading;
    private Color color;
    private double stepAccumulator = 0.0;
    private double chaseSpeed = GameConstants.MONSTER_CHASE_SPEED;
    private volatile ArrayList<DonneesJoueur> joueurs = new ArrayList<>();

    // Les limites de déplacement du monstre.
    private Coordinates minBounds = new Coordinates(0.0, 0.0); 
    private Coordinates maxBounds = new Coordinates(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    // initialisation du thread.
    private volatile boolean running = false;
    private Thread movementThread;

    // init de random pour les délpacements aléatoires.
    private final Random random = new Random();
    private CollisionMap collisionMap;

    public Monsters(double x, double y, double speed, Hitbox hitbox) {
        super(new Coordinates(snapToTileValue(x), snapToTileValue(y)), null, speed, hitbox);
        this.velocity = new Coordinates(0, 0);
        this.heading = new Coordinates(0, 0);
        if (this.hitbox != null) {
            this.hitbox.update(this.position);
        }
    }

    @Override
    public void startMovement() {
        if (running) {
            return;
        }
        running = true;

        // Démarrage du tread de mouvement
        movementThread = new Thread(() -> {
            long lastTime = System.currentTimeMillis();

            while (running) {
                long now = System.currentTimeMillis();
                double dt = (now - lastTime) / 1000.0;
                lastTime = now;

                miseAJour(dt);
                syncHitbox();

                try { Thread.sleep(16); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; } // ~60 FPS TO MODIFY TO MATCH THE SPEED OF THE MONSTER
            }
        }, "monster-movement");

        movementThread.start();
    }

    @Override
    public void stopMovement() {
        // Les deux threads du monstre s'arretent ensemble pour eviter qu'ils continuent a tourner apres la partie.
        running = false;
        if (movementThread != null) {
            movementThread.interrupt();
        }
    }

    public void miseAJour(double dt) {
        if (speed <= 0) {
            return;
        }

        DonneesJoueur cible = trouverCibleAvecMiel();


        // Si une cible avec du miel est trouvée, on la poursuit
        if (cible != null) {
            poursuivre(cible, dt, chaseSpeed);
            return;
        }

        stepAccumulator += dt;
        int cmCol = (int)(position.getX() / TILE_SIZE);
        int cmRow = (int)(position.getY() / TILE_SIZE);
        double factor = (collisionMap != null) ? collisionMap.getSpeedFactor(cmCol, cmRow) : 1.0;
        double secondsPerTile = 1.0 / (speed * factor);

        // Si le monstre n'a pas encore parcouru une tuile complète, on ne change pas de direction.
        if (stepAccumulator < secondsPerTile) {
            velocity.setX(0);
            velocity.setY(0);
            return;
        }

        // Sinon, on choisit une nouvelle direction aléatoire et on réinitialise l'accumulateur de temps.
        stepAccumulator -= secondsPerTile;
        synchronized (position) {
            chooseRandomDirection();
        }
    }


    private DonneesJoueur trouverCibleAvecMiel() {
        DonneesJoueur meilleureCible = null;
        double meilleureDistanceCarree = AGGRO_RANGE_BLOCKS * AGGRO_RANGE_BLOCKS; // on compare la distance au carré 

        double monsterX;
        double monsterY;
        synchronized (position) {
            monsterX = position.getX();
            monsterY = position.getY();
        }

        for (DonneesJoueur joueur : joueurs) {
            if (joueur == null || !joueur.hasHoney) {
                continue;
            }

            double dx = (joueur.x - monsterX) / TILE_SIZE; // dx exprimé en nombre de tuiles
            double dy = (joueur.y - monsterY) / TILE_SIZE; // dy exprimé en nombre de tuiles
            double distanceCarree = dx * dx + dy * dy; 
            if (distanceCarree <= meilleureDistanceCarree) {
                meilleureDistanceCarree = distanceCarree;
                meilleureCible = joueur;
            }
        }

        return meilleureCible;
    }

    private void poursuivre(DonneesJoueur cible, double dt, double speedTiles) {

        // stepAccumulator accumule le temps écoulé depuis le dernier changement de direction.
        stepAccumulator += dt;

        // Si le monstre n'a pas encore parcouru une tuile complète à la vitesse de poursuite, on continue dans la même direction.
        int cmCol = (int)(position.getX() / TILE_SIZE);
        int cmRow = (int)(position.getY() / TILE_SIZE);
        double factor = (collisionMap != null) ? collisionMap.getSpeedFactor(cmCol, cmRow) : 1.0;
        double secondsPerTile = 1.0 / (speedTiles * factor);
        if (stepAccumulator < secondsPerTile) {
            velocity.setX(0);
            velocity.setY(0);
            return;
        }
        // Sinon, on réinitialise l'accumulateur de temps et on met à jour la position du monstre en direction de la cible.
        stepAccumulator -= secondsPerTile;

        synchronized (position) {
            double dx = cible.x - position.getX(); // différence de position en x entre la cible et le monstre
            double dy = cible.y - position.getY(); // différence de position en y entre la cible et le monstre
            double distanceX = Math.abs(dx); // distance absolue en x entre la cible et le monstre
            double distanceY = Math.abs(dy); // distance absolue en y entre la cible et le monstre

            // Si le monstre est déjà sur la cible, on ne bouge pas.
            if (distanceX == 0 && distanceY == 0) {
                velocity.setX(0);
                velocity.setY(0);
                return;
            }
            
            // On choisit de se déplacer en priorité sur l'axe où la cible est la plus éloignée, pour éviter les mouvements en zigzag.
            double dirX = 0;
            double dirY = 0;
            if (distanceX >= distanceY) {
                dirX = dx > 0 ? 1 : -1; // si le joueur est a droite du monstre, dirX = 1 sinon -1
            } else {
                dirY = dy > 0 ? 1 : -1; // si le joueur est en dessous du monstre, dirY = 1 sinon -1
            }

            // Calcul de la nouvelle position du monstre en fonction de la direction choisie et de la vitesse de poursuite.
            double nextX = position.getX() + dirX * TILE_SIZE;
            double nextY = position.getY() + dirY * TILE_SIZE;

            // On s'assure que la nouvelle position ne dépasse pas les limites de déplacement du monstre.
            nextX = Math.max(minBounds.getX(), Math.min(maxBounds.getX(), nextX));
            nextY = Math.max(minBounds.getY(), Math.min(maxBounds.getY(), nextY));

            if (isMurAt(nextX, nextY)) {
                // La direction principale est bloquée par un mur.
                // On calcule les deux directions perpendiculaires pour contourner l'obstacle.
                // Si le monstre allait horizontalement, on essaie vertical (et inversement).
                // La première alternative (p1) est celle qui rapproche le plus de la cible sur l'axe opposé.
                double p1x, p1y;
                if (dirX != 0) {
                    // Mouvement horizontal bloqué → essayer vertical
                    // p1 va vers la cible en y, p2 dans le sens opposé
                    p1x = 0; p1y = dy >= 0 ? 1 : -1;
                } else {
                    // Mouvement vertical bloqué → essayer horizontal
                    // p1 va vers la cible en x, p2 dans le sens opposé
                    p1x = dx >= 0 ? 1 : -1; p1y = 0;
                }
                // p2 est l'opposé de p1 (l'autre côté)
                double p2x = -p1x; double p2y = -p1y;

                // On calcule les positions candidates en respectant les limites de déplacement
                double c1x = Math.max(minBounds.getX(), Math.min(maxBounds.getX(), position.getX() + p1x * TILE_SIZE));
                double c1y = Math.max(minBounds.getY(), Math.min(maxBounds.getY(), position.getY() + p1y * TILE_SIZE));
                double c2x = Math.max(minBounds.getX(), Math.min(maxBounds.getX(), position.getX() + p2x * TILE_SIZE));
                double c2y = Math.max(minBounds.getY(), Math.min(maxBounds.getY(), position.getY() + p2y * TILE_SIZE));

                // On tente la première alternative (la plus proche de la cible sur l'axe perpendiculaire)
                if (!isMurAt(c1x, c1y)) {
                    position.setX(c1x); position.setY(c1y);
                    heading.setX(p1x); heading.setY(p1y);
                    velocity.setX(p1x * TILE_SIZE); velocity.setY(p1y * TILE_SIZE);
                    return;
                }
                // Si elle est aussi bloquée, on tente l'autre côté
                if (!isMurAt(c2x, c2y)) {
                    position.setX(c2x); position.setY(c2y);
                    heading.setX(p2x); heading.setY(p2y);
                    velocity.setX(p2x * TILE_SIZE); velocity.setY(p2y * TILE_SIZE);
                    return;
                }
                // Toutes les alternatives sont bloquées : le monstre reste sur place ce tick
                velocity.setX(0);
                velocity.setY(0);
                return;
            }

            // Mise à jour de la position, de la direction et de la vitesse du monstre.
            position.setX(nextX);
            position.setY(nextY);
            heading.setX(dirX);
            heading.setY(dirY);
            velocity.setX(dirX * TILE_SIZE);
            velocity.setY(dirY * TILE_SIZE);
        }
    }

    // Synchronisation de la hitbox avec la position du monstre pour assurer que les collisions soient détectées correctement.
    private void syncHitbox() {
        if (hitbox != null) {
            synchronized (position) {
                hitbox.update(position);
            }
        }
    }


    private boolean isMurAt(double worldX, double worldY) {
        if (collisionMap == null) return false;
        return collisionMap.isMur((int)(worldX / TILE_SIZE), (int)(worldY / TILE_SIZE));
    }

    private void chooseRandomDirection() {

        // initialisation des directions possibles (droite, gauche, bas, haut)
        int[][] dirs = {
            { 1, 0}, {-1, 0}, {0, 1}, {0,-1}, { 1, 1}, { 1,-1}, {-1, 1}, {-1,-1} // on ajoute les diagonales pour plus de fluidité dans les déplacements
        };

        // On choisit une direction de départ aléatoire pour éviter que les monstres aient tous le même comportement au démarrage.
        int startIndex = random.nextInt(dirs.length);


        for (int i = 0; i < dirs.length; i++) {
            int[] d = dirs[(startIndex + i) % dirs.length];
            // On calcule la position suivante du monstre en fonction de la direction choisie et de la taille d'une tuile.
            double nextX = position.getX() + d[0] * TILE_SIZE;
            double nextY = position.getY() + d[1] * TILE_SIZE;

            // ON verifie que la nouvelle position ne dépasse pas les limites de déplacement du monstre.
            if (nextX >= minBounds.getX()
                    && nextX <= maxBounds.getX()
                    && nextY >= minBounds.getY()
                    && nextY <= maxBounds.getY()
                    && !isMurAt(nextX, nextY)) {
                //Si elle est valide, on met à jour la position, la direction et la vitesse du monstre, puis on sort de la boucle.
                position.setX(nextX);
                position.setY(nextY);
                heading.setX(d[0]);
                heading.setY(d[1]);
                velocity.setX(d[0] * TILE_SIZE);
                velocity.setY(d[1] * TILE_SIZE);
                return;
            }
        }
        // Sinon on ne change pas de direction et on attend le prochain appel de miseAJour pour essayer une nouvelle direction.
        velocity.setX(0);
        velocity.setY(0);
    }

    public double getX() { synchronized (position) { return position.getX(); } }
    public double getY() { synchronized (position) { return position.getY(); } }
    public double getVx() { return velocity.getX(); }
    public double getVy() { return velocity.getY(); }
    public double getHx() { return heading.getX(); }
    public double getHy() { return heading.getY(); }
    public double getMaxSpeed() { return speed; }
    public Color getColor() { return color; }

    public void setX(double x) { synchronized (position) { position.setX(x); } }
    public void setY(double y) { synchronized (position) { position.setY(y); } }
    public void setPosition(double x, double y) {
        synchronized (position) {
            position.setX(snapToTile(x));
            position.setY(snapToTile(y));
            stepAccumulator = 0.0;
            if (hitbox != null) {
                hitbox.update(position);
            }
        }
    }
    public void setVx(double vx) { velocity.setX(vx); }
    public void setVy(double vy) { velocity.setY(vy); }
    public void setHx(double hx) { heading.setX(hx); }
    public void setHy(double hy) { heading.setY(hy); }
    public void setMaxSpeed(double maxSpeed) { this.speed = maxSpeed; }
    public void setChaseSpeed(double chaseSpeed) { this.chaseSpeed = chaseSpeed; }
    public void setColor(Color color) { this.color = color; }
    public void setJoueurs(ArrayList<DonneesJoueur> joueurs) {
        this.joueurs = joueurs != null ? new ArrayList<>(joueurs) : new ArrayList<>(); // si la liste de joueurs est null, on en crée une vide pour éviter les NullPointerException
    }

    public void setMovementBounds(double minX, double minY, double maxX, double maxY) {
        this.minBounds = new Coordinates(minX, minY);
        this.maxBounds = new Coordinates(Math.max(minX, maxX), Math.max(minY, maxY));
    }

    public void setCollisionMap(CollisionMap collisionMap) {
        this.collisionMap = collisionMap;
    }

    private double snapToTile(double value) {
        return snapToTileValue(value);
    }

    private static double snapToTileValue(double value) {
        return Math.round(value / TILE_SIZE) * TILE_SIZE;
    }
}
