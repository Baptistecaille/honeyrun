# HoneyRun

Jeu multijoueur 2D en Java où des joueurs s'affrontent pour ramener du miel à leur base tout en évitant des monstres.

---

## Concept

Jusqu'à 4 joueurs se déplacent sur une carte. Au centre se trouve une ruche contenant du miel. Chaque joueur doit :

1. Se rendre à la ruche et **récolter du miel** (3 secondes sur place)
2. **Ramener le miel** à son point de départ pour marquer la victoire
3. **Éviter les monstres** qui patrouillent la carte — une collision coûte une vie et fait perdre le miel porté

---

## Règles

| Règle | Détail |
| ----- | ------ |
| Vies | Chaque joueur commence avec **3 vies** |
| Collision | Le joueur est renvoyé à son spawn et perd le miel qu'il portait |
| Mort | À 0 vie, le joueur est éliminé définitivement |
| Victoire | Ramener du miel jusqu'à la zone de spawn |
| Monstres | 4 à 5 monstres contrôlés par le jeu, rebond sur les bords de la carte |
| Invincibilité | 1 seconde d'invincibilité après avoir été touché |

---

## Architecture

```text
src/
├── Tools/
│   ├── Coordinates.java   — position (x, y) partagée entre threads
│   ├── Hitbox.java        — rectangle de collision
│   └── Avatar.java        — utilitaire visuel
├── monsters/
│   ├── Monsters.java      — logique de mouvement et d'IA
│   └── tests/
│       ├── Interface.java — interface de test visuelle
│       └── Player.java    — stub joueur pour les tests monstres
└── players/
    ├── Player.java        — logique du joueur (mouvement, récolte, collisions)
    └── tests/
        └── Interface.java — interface de test visuelle
```

### Classes principales

**`Monsters`** — Se déplace aléatoirement parmi 8 directions cardinales/diagonales à ~60 fps. Change de direction toutes les 3 secondes. Rebondit sur les bords. Deux threads dédiés : un pour le mouvement, un pour la synchronisation de la hitbox.

**`Player`** — Se déplace avec les touches fléchées. Gère la récolte de miel (présence 3 s dans la `hiveZone`), les collisions avec les monstres, le compteur de vies et la condition de victoire.

**`Hitbox`** — Rectangle AABB utilisé pour la détection de collisions.

**`Coordinates`** — Conteneur de position `(x, y)` utilisé dans des blocs `synchronized`.

---

## Prérequis

- Java 21+
- Maven 3.6+

---

## Lancer le projet

```bash
# Compiler
mvn compile

# Lancer l'interface de test des monstres
mvn exec:java -Dexec.mainClass="monsters.tests.Interface"

# Lancer l'interface de test du joueur
mvn exec:java -Dexec.mainClass="players.tests.Interface"
```

---

## Feuille de route

### V1 — Déplacement aléatoire + collision *(en cours)*

- [ ] `Hitbox.intersects()` — détection de collision entre deux hitboxes
- [ ] `Monsters.update(dt)` — déplacement + rebond sur les bords
- [ ] `Monsters.rendu(Graphics2D)` — rendu visuel du monstre
- [ ] Conséquence de collision — téléporter le joueur à son spawn, lui retirer le miel

### V2 — IA + mort du joueur

- [ ] États `RANDOM` / `GUARD` / `CHASE` dans `Monsters`
- [ ] Comportement de garde : le monstre reste près de la ruche
- [ ] Comportement de chasse : si un joueur porte du miel, le monstre le plus proche le poursuit
- [ ] Compteur de touches : 3 touches → élimination définitive

### V3 — Différenciation *(optionnel)*

- [ ] `maxSpeed` variable selon le monstre
- [ ] Taille de hitbox variable
- [ ] Couleurs distinctes par type de monstre
