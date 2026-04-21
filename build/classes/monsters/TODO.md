# MONSTERS — Suivi de progression

## V1 — Déplacement aléatoire + collision

### `Hitbox.java`
- [ ] `intersects(Hitbox other)` — Vérification de collision entre deux hitboxes

### `Monsters.java`
- [ ] `initRandomDirection()` — Direction initiale aléatoire (vecteur normalisé) + `maxSpeed`
- [ ] `update(dt)` — Déplacement (`x += vx * dt`, `y += vy * dt`) + mise à jour de la hitbox
- [ ] Rebond sur les bords — Inverser `vx` ou `vy` selon le bord atteint
- [ ] `rendu(Graphics2D)` — Dessiner le monstre (cercle ou forme colorée)
- [ ] Détection de collision — Vérifier à chaque frame si la hitbox chevauche celle d'un joueur
- [ ] Conséquence de collision — Téléporter le joueur à son spawn + retirer le miel porté

---

## V2 — Comportement IA + mort du joueur

- [ ] `state` (enum) — Ajouter les états `RANDOM`, `GUARD`, `CHASE`
- [ ] Comportement de garde — Le monstre se dirige vers le pot s'il s'en éloigne trop
- [ ] Comportement de chasse — Le monstre le plus proche passe en `CHASE` si un joueur porte du miel
- [ ] Transitions d'état — `GUARD` → `CHASE` quand le miel est pris ; `CHASE` → `GUARD` quand lâché/joueur disparu
- [ ] Compteur de touches — Après 3 touches par des monstres, le joueur disparaît définitivement

---

## V3 — Différenciation des statistiques *(optionnel)*

- [ ] `maxSpeed` variable — Un monstre rapide, les autres plus lents
- [ ] Taille de hitbox variable — Grand monstre = hitbox plus grande
- [ ] Couleurs distinctes — Distinction visuelle entre types de monstres