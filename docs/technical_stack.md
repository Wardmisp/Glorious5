# Stack Technique - NBA Simulation

Ce document présente les technologies et bibliothèques utilisées dans le développement de l'application, ainsi que les justifications de ces choix techniques.

## 1. Langage & Fondations
*   **Kotlin (2.4.10)** : Le langage de référence pour le développement Android moderne. Il a été choisi pour sa concision, sa sécurité (gestion des nulls) et son support natif des coroutines.
*   **Coroutines Kotlin (1.8.0)** : Utilisées pour la gestion de l'asynchronisme (ex: accès à la base de données, délais de simulation) sans bloquer le thread principal (UI).

## 2. Interface Utilisateur (UI)
*   **Jetpack Compose (BOM 2024.09.00)** : Framework UI déclaratif moderne.
    *   *Justification* : Permet une création d'interfaces dynamiques (comme l'horloge LED ou les cartes de joueurs) beaucoup plus rapidement qu'avec les fichiers XML classiques.
*   **Material Design 3** : Utilisation de la bibliothèque `androidx.compose.material3`.
    *   *Justification* : Offre des composants d'interface cohérents avec les derniers standards visuels d'Android.
*   **Canvas API (Compose)** : Utilisé pour le dessin personnalisé des afficheurs 7 segments.

## 3. Architecture & Gestion d'État
*   **Modèle MVVM (Model-View-ViewModel)** :
    *   *View* : Screens Compose.
    *   *ViewModel* : `GameViewModel` gère la logique de l'état de jeu.
    *   *Model* : Data classes et Repositories.
*   **Use Cases (Clean Architecture)** : Séparation de la logique métier complexe (ex: `CalculateWinProbabilityUseCase`) dans des classes dédiées.
    *   *Justification* : Facilite les tests unitaires et la réutilisation de la logique sans polluer les ViewModels.

## 4. Persistance des Données
*   **Room Database (2.8.4)** : Couche d'abstraction sur SQLite.
    *   *Justification* : Utilisé pour stocker les statistiques des joueurs et les historiques de saisons. Room offre une vérification des requêtes SQL à la compilation et une intégration native avec les Coroutines.
*   **Kotlin Symbol Processing (KSP)** : Utilisé à la place de KAPT pour la génération de code Room.
    *   *Justification* : KSP est plus performant et plus rapide pour compiler les annotations Kotlin.

## 5. Services & Monitoring
*   **Firebase Crashlytics** : Monitoring des crashs en temps réel.
    *   *Justification* : Indispensable pour identifier les bugs sur les différents modèles d'appareils Android en production.
*   **Firebase Analytics** : Suivi du comportement des utilisateurs.
*   **Google Services** : Plugin pour l'intégration de l'écosystème Firebase.

## 6. Multimédia
*   **Android MediaPlayer** : Utilisé dans `SoundManager` pour la gestion des effets sonores (sifflet, buzzer, ambiance).
    *   *Justification* : Simple et efficace pour la lecture de fichiers audio courts dans les ressources `raw`.

## 7. Build & Outils
*   **Gradle Kotlin DSL (.kts)** : Fichiers de configuration de build écrits en Kotlin.
*   **Version Catalogs (libs.versions.toml)** : Gestion centralisée des versions de dépendances.
    *   *Justification* : Évite les duplications de versions entre les modules et facilite les mises à jour globales.
