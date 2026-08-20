# Migration du Package vers `com.g5`

Ce plan détaille les étapes nécessaires pour renommer le package de l'application de `com.example.androididea` vers `com.g5`. Cela inclut la mise à jour des configurations Gradle, du code source et de la structure des dossiers.

## User Review Required

> [!IMPORTANT]
> Le changement de `applicationId` dans Gradle fera que le Play Store (ou votre appareil) considérera l'application comme une **nouvelle application**. Les données locales existantes sur les appareils de test pourraient être perdues lors de la réinstallation.

## Proposed Changes

### Configuration Build

#### [MODIFY] [build.gradle.kts (app)](file:///C:/Users/Rémi/AndroidStudioProjects/AndroidIdea/app/build.gradle.kts)
- Mettre à jour `namespace` vers `com.g5`.
- Mettre à jour `applicationId` vers `com.g5`.

### Manifeste

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/Rémi/AndroidStudioProjects/AndroidIdea/app/src/main/AndroidManifest.xml)
- Vérifier et mettre à jour toute référence explicite au package.

### Code Source (Kotlin)

Mise à jour de tous les fichiers `.kt` dans `src/main/java`, `src/test/java` et `src/androidTest/java` :
- Changer `package com.example.androididea...` en `package com.g5...`.
- Changer les imports `import com.example.androididea.R` en `import com.g5.R`.
- Changer les imports `import com.example.androididea.BuildConfig` en `import com.g5.BuildConfig`.

#### Déplacement des fichiers
Les fichiers seront déplacés physiquement :
- De `app/src/main/java/com/example/androididea/` vers `app/src/main/java/com/g5/`.
- De `app/src/test/java/com/example/androididea/` vers `app/src/test/java/com/g5/`.
- De `app/src/androidTest/java/com/example/androididea/` vers `app/src/androidTest/java/com/g5/`.

## Verification Plan

### Automated Tests
- Exécuter `./gradlew clean assembleDebug` pour vérifier que la compilation passe avec le nouveau package.
- Lancer les tests unitaires : `./gradlew :app:testDebugUnitTest`.

### Manual Verification
- Vérifier dans Android Studio que la structure du projet est correctement reconnue sous le nouveau package `com.g5`.
