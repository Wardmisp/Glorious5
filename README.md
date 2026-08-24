# 🏀 Glorious 5

**Glorious 5** est un jeu mobile Android de stratégie et d'enchères de basketball NBA, développé avec **Kotlin** et **Jetpack Compose**.

Affrontez l'intelligence artificielle ou d'autres joueurs pour constituer le meilleur cinq majeur (*Glorious 5*) grâce à un système d'enchères en temps réel et de révélation progressive de statistiques NBA historiques.

---

## 🚀 Statut du projet & Déploiement

- **Distribution** : La livraison et le déploiement de l'application s'effectuent via le **Google Play Store**.
- **Phase actuelle** : Le projet est actuellement en **phase de test interne** (*Internal Testing* via la Google Play Console) afin de valider l'expérience utilisateur, les performances et l'équilibrage du jeu avant son ouverture au grand public.
- **Mode 2 Joueurs en ligne (En cours de développement)** : Une fonctionnalité multijoueur en ligne à 2 joueurs est activement développée avec un backend dédié (voir le dépôt [Nbackend](https://github.com/Wardmisp/Nbackend)).
- **Mode Pass & Play (En réflexion)** : L'ajout d'un mode *Pass & Play* (tour par tour sur le même écran/appareil) est actuellement à l'étude.

---

## 🎮 Concept & Règles du jeu

Le jeu simule une séance de draft et d'enchères haletante inspirée des meilleures saisons de l'histoire de la NBA :

1. **Budget initial** : Chaque participant commence la partie avec un budget de **50 crédits**.
2. **Pool de joueurs & Rounds** : Une sélection de joueurs issus d'une base de données de saisons NBA est proposée tour après tour.
3. **Révélation progressive (Blind Auction)** :
   - Les informations du joueur (statistiques : Points, Rebonds, Passes, Interceptions, Contres ; Saison ; Poste ; Équipe ; Identité) se dévoilent progressivement au fil des secondes.
   - Les joueurs doivent évaluer le potentiel du profil avant que son identité ne soit entièrement révélée pour optimiser leurs enchères.
4. **Timer & Enchères** : Un compte à rebours de 15 secondes régit chaque tour. Les participants peuvent surenchérir ou passer leur tour.
5. **Fin de partie & Scoring** :
   - À l'issue des enchères, les équipes constituées sont évaluées selon une formule de performance composite basée sur les statistiques réelles des joueurs :
     $$\text{Score} = \text{PTS} + 0.7 \times \text{REB} + 0.8 \times \text{AST} + 1.5 \times \text{STL} + 1.5 \times \text{BLK}$$
   - L'équipe avec le meilleur score cumulé remporte la draft.

---

## ✨ Fonctionnalités principales

- 🤖 **Mode Solo (Vs IA)** : Mesurez votre sens tactique face à une intelligence artificielle capable d'évaluer la valeur des joueurs et d'adapter ses enchères en temps réel.
- 🌐 **Mode 2 Joueurs en ligne (En développement)** : Affrontez d'autres joueurs en temps réel grâce au backend dédié [Nbackend](https://github.com/Wardmisp/Nbackend).
- 🔄 **Mode Pass & Play (En réflexion)** : Possibilité de jouer à 2 sur le même appareil au tour par tour, actuellement à l'étude.
- 📊 **Base de données NBA historique** : Intégration d'une base SQLite de plus de 300 saisons de légende avec statistiques détaillées (points, rebonds, passes, interceptions, contres, pourcentages de tir, PER, Win Shares).
- 🎨 **Interface moderne & immersive** : Design épuré aux couleurs et lignes d'un terrain de basketball, conçu intégralement avec Jetpack Compose et Material 3.
- ⚡ **Animations & Transitions fluides** : Feedback visuel dynamique pour le compte à rebours, les enchères et les révélations de cartes de joueurs.

---

## 🛠️ Stack Technique & Architecture

- **Langage** : [Kotlin](https://kotlinlang.org/) (JVM 11 Target)
- **UI Framework** : [Jetpack Compose](https://developer.android.com/jetpack/compose) avec **Material 3**
- **Architecture** : MVVM (Model-View-ViewModel) avec gestion d'état unidirectionnelle (`UiState`, `StateFlow`, `Compose State`)
- **Persistance des données** : [Room Database](https://developer.android.com/training/data-storage/room) pré-peuplée avec base SQLite locale (`nba_top300.db`)
- **Asynchronisme** : Kotlin Coroutines & Flow
- **Système de build** : Gradle (Kotlin DSL `.kts`)
- **Compatibilité Android** :
  - `minSdk` : 28 (Android 9.0 Pie)
  - `targetSdk` / `compileSdk` : 36 / 37

---

## 📁 Structure du Projet

```text
Glorious5/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── assets/
│   │   │   │   ├── audio/                # Effets sonores (buzzer, enchères)
│   │   │   │   └── databases/
│   │   │   │       └── nba_top300.db     # Base de données SQLite pré-remplie
│   │   │   ├── java/com/g5/
│   │   │   │   ├── core/                 # Utilitaires & Managers (SoundManager, etc.)
│   │   │   │   ├── data/                 # Couche Données (Room DB, DAO, Entités, Repositories)
│   │   │   │   │   ├── local/
│   │   │   │   │   └── repository/
│   │   │   │   ├── domain/               # Couche Métier (Modèles & UseCases de scoring)
│   │   │   │   │   ├── model/
│   │   │   │   │   └── usecase/
│   │   │   │   ├── ui/                   # Interface utilisateur Jetpack Compose
│   │   │   │   │   ├── components/       # Composants réutilisables
│   │   │   │   │   ├── screens/          # Écrans (Home, Game, GameOver, Options)
│   │   │   │   │   ├── theme/            # Thèmes (Mode Clair / Sombre), Typographie, Couleurs
│   │   │   │   │   └── viewmodel/        # GameViewModel & UiState
│   │   │   │   └── MainActivity.kt       # Point d'entrée de l'application
│   │   │   └── AndroidManifest.xml
│   │   └── test/                         # Tests unitaires
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
│   └── libs.versions.toml                # Gestion centralisée des versions (Version Catalog)
├── CHANGELOG.md
├── VERSION.txt
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🔧 Installation & Exécution en local

### Prérequis
- **Android Studio** (version Ladybug / Meerkat ou supérieure recommandée)
- **JDK 11** ou supérieur configuré
- Un appareil Android physique ou un émulateur (API 28 minimum)

### Étapes
1. Clonez le dépôt Git :
   ```bash
   git clone <url-du-depot>
   cd Glorious5
   ```
2. Ouvrez le projet dans Android Studio.
3. Laissez Gradle synchroniser les dépendances (via le Version Catalog `libs.versions.toml`).
4. Lancez l'application sur votre émulateur ou appareil cible :
   ```bash
   ./gradlew assembleDebug
   ```
   ou via le bouton **Run 'app'** dans Android Studio.

---

## 📄 Licence

Projet développé dans le cadre de Glorious 5. Tous droits réservés.
