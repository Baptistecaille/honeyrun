# MONSTERS — Feuille de route

## Contexte du jeu

Jeu multijoueur à 4 joueurs. Les joueurs collectent du miel depuis un pot au centre de la carte.
Les monstres sont des obstacles contrôlés par le jeu (pas par les joueurs).

- Nombre fixe de monstres : **4 ou 5**
- Pas d'apparition de monstres au fil du temps
- Comportement aux bords de la carte : **rebond** (inversion de la direction)

---

## Conséquences d'une collision

| Version | Ce qui se passe lors d'une collision |
| --- | --- |
| V1 | Le joueur est renvoyé à son point de départ. S'il portait du miel, il le perd. |
| V2 | Identique à V1, plus : après **3 touches** par des monstres, le joueur meurt et disparaît définitivement. |

---

## Feuille de route (Approche B — incréments jouables)

Chaque version est un état de jeu complet et testable.

### V1 — Déplacement aléatoire + collision

**Objectif :** Les monstres sont présents sur la carte, se déplacent aléatoirement et interagissent avec les joueurs.

| Tâche | Description |
| --- | --- |
| `initRandomDirection()` | Définir une direction initiale aléatoire (vecteur normalisé) et appliquer `maxSpeed` |
| `update(dt)` | Déplacer le monstre à chaque frame : `x += vx * dt`, `y += vy * dt`. Mettre à jour la hitbox. |
| Rebond sur les bords | Si le monstre atteint un bord de la carte, inverser la composante de vitesse correspondante (`vx` ou `vy`) |
| `rendu(Graphics2D)` | Dessiner le monstre à l'écran (cercle ou forme colorée) |
| Détection de collision | Vérifier à chaque frame si la hitbox du monstre chevauche celle d'un joueur |
| Conséquence de collision | En cas de collision : téléporter le joueur à son spawn, lui retirer le miel qu'il porte |

---

### V2 — Comportement IA + mort du joueur

**Objectif :** Les monstres sont plus intelligents et les enjeux sont plus élevés.

| Tâche | Description |
| --- | --- |
| `state` (enum interne) | Ajouter les états `RANDOM`, `GUARD`, `CHASE` à la classe `Monsters` |
| Comportement de garde | Le monstre reste près du pot de miel (se dirige vers lui s'il s'en éloigne trop) |
| Comportement de chasse | Si un joueur porte du miel, le monstre le plus proche passe en `CHASE` et se dirige vers ce joueur |
| Transitions d'état | `GUARD` → `CHASE` quand un joueur prend le miel. `CHASE` → `GUARD` quand le miel est lâché ou que le joueur disparaît. |
| Compteur de touches | Compter les touches reçues par joueur. Après 3 touches, le joueur disparaît définitivement. |

---

### V3 — Différenciation des statistiques *(optionnel, si le temps le permet)*

**Objectif :** Les monstres se distinguent sans modifier la logique IA.

| Statistique | Exemple de variation |
| --- | --- |
| `maxSpeed` | Un monstre rapide, les autres plus lents |
| Taille de la hitbox | Un grand monstre = une hitbox plus grande |
| Couleur | Distinction visuelle entre les types de monstres |

---

## Architecture des classes

### `Monsters.java`

| Attribut / Méthode | Statut | Notes |
| --- | --- | --- |
| `x, y` | existant | Position en pixels |
| `vx, vy` | existant | Vitesse en pixels/seconde |
| `maxSpeed` | existant | Vitesse maximale |
| `hx, hy` | existant | Vecteur de direction normalisé |
| `color` | existant | Pour le rendu |
| `hitbox` | existant | Hitbox rectangulaire |
| `state` (enum) | à ajouter | `RANDOM`, `GUARD`, `CHASE` — V2 |
| `initRandomDirection()` | à ajouter | Direction et vitesse aléatoires — V1 |
| `update(dt)` | à ajouter | Déplacement + rebond + IA — V1/V2 |
| `rendu(Graphics2D)` | à ajouter | Dessin à l'écran — V1 |

### `Hitbox.java`

| Attribut / Méthode | Statut | Notes |
| --- | --- | --- |
| `x, y, width, height` | existant | Dimensions du rectangle |
| `update(newX, newY)` | existant | Synchronise la hitbox avec la position du monstre |
| `intersects(Hitbox other)` | à ajouter | Vérification de collision entre deux hitboxes — V1 |
