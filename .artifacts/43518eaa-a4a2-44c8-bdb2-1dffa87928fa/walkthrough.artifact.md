# Migration du Package vers `com.g5` terminée

J'ai réussi à renommer le package de l'application de `com.example.androididea` vers `com.g5`.

## Modifications effectuées

1.  **Configuration Gradle** :
    - Mise à jour du `namespace` vers `com.g5` dans `app/build.gradle.kts`.
    - Mise à jour de l' `applicationId` vers `com.g5` dans `app/build.gradle.kts`.
2.  **Structure des dossiers** :
    - Déplacement de tous les fichiers sources vers la nouvelle structure `app/src/main/java/com/g5/`, `app/src/test/java/com/g5/` et `app/src/androidTest/java/com/g5/`.
    - Suppression de l'ancienne arborescence `com/example/androididea`.
3.  **Code Source Kotlin** :
    - Mise à jour de toutes les déclarations `package` vers `com.g5`.
    - Mise à jour de tous les `import` internes (incluant `com.g5.R`).
    - Mise à jour du test d'instrumentation pour vérifier le nouveau nom de package.

## Vérification

- **Compilation** : `./gradlew :app:assembleDebug` a réussi.
- **Tests Unitaires** : `./gradlew :app:testDebugUnitTest` a réussi (2 tests passés).
- **Synchronisation IDE** : Le projet a été synchronisé avec succès.

> [!NOTE]
> Comme l' `applicationId` a changé, l'application sera considérée comme nouvelle sur votre appareil de test.
