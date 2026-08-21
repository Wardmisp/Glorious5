# Algorithme de Calcul de Victoire - NBA Simulation

Ce document détaille la logique mathématique utilisée pour déterminer la probabilité de victoire et le gagnant final d'une rencontre entre deux équipes.

## 1. Score Individuel des Joueurs (Player Score)

Chaque joueur reçoit un score basé sur ses statistiques réelles, converties en **percentiles** par rapport à l'ensemble de la base de données (tous les joueurs de toutes les saisons). Un percentile de 90 signifie que le joueur est meilleur que 90% des autres joueurs dans cette catégorie.

### A. Calcul des Percentiles
Pour chaque statistique ($PTS, REB, AST, STL, BLK, FG\%, 3P\%, FT\%, PER, WS/Game$), on calcule :
$$P_x = \frac{\text{Nombre de joueurs } < \text{ valeur } + 0.5 \times \text{Nombre de joueurs } = \text{ valeur}}{\text{Nombre total de joueurs}} \times 100$$

### B. Scores Intermédiaires
Deux scores d'agrégation sont calculés :
*   **Efficacité (EFF)** : Mesure la précision au tir.
    $$EFF = 0.40 \times P_{FG\%} + 0.35 \times P_{3P\%} + 0.25 \times P_{FT\%}$$
*   **Impact** : Mesure la domination globale et la contribution à la victoire (Win Shares).
    $$IMPACT = 0.60 \times P_{PER} + 0.40 \times P_{WS/Game}$$

### C. Score Final du Joueur
Le score total est une moyenne pondérée des percentiles et des agrégats :
$$Score_{Total} = 0.20 \times P_{PTS} + 0.10 \times P_{REB} + 0.15 \times P_{AST} + 0.10 \times P_{STL} + 0.10 \times P_{BLK} + 0.10 \times EFF + 0.25 \times IMPACT$$

---

## 2. Score Global de l'Équipe (Team Score)

Le score de l'équipe n'est pas une simple moyenne. Il applique des **coefficients par position** pour simuler l'importance relative de chaque rôle dans une équipe équilibrée.

| Position | Coefficient |
| :--- | :--- |
| **Meneur (PG)** | 0.22 |
| **Arrière (SG)** | 0.20 |
| **Ailier (SF)** | 0.20 |
| **Ailier Fort (PF)** | 0.20 |
| **Pivot (C)** | 0.18 |

$$Score_{\text{Équipe}} = \sum (Score_{\text{Joueur}} \times Coefficient_{\text{Position}})$$

---

## 3. Probabilité de Victoire (Win Probability)

Pour convertir la différence de score entre deux équipes en une probabilité (entre 0 et 1), nous utilisons une **fonction logistique** (similaire au système ELO).

Soit $D = Score_{\text{Équipe A}} - Score_{\text{Équipe B}}$ :
$$P(\text{A gagne}) = \frac{1}{1 + e^{-D / 8}}$$

*   Si $D = 0$ (équipes égales), $P(A) = 0.5$ (50%).
*   Le diviseur $8$ ajuste la sensibilité : une différence de 10 points de score équipe donne environ 77% de chance de victoire.

---

## 4. Détermination du Gagnant

Une fois la probabilité de victoire calculée, le gagnant est déterminé par un **tirage aléatoire pondéré** :

```kotlin
val randomValue = Math.random()
val winner = if (randomValue < p1WinProb) 1 else 2
```

> [!NOTE]
> Ce système garantit que l'équipe la plus forte a statistiquement plus de chances de gagner, tout en laissant place à l'incertitude du sport (l'"upset"). Une équipe avec 80% de chances de victoire perdra tout de même 1 match sur 5 en moyenne.
