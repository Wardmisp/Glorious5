package com.g5.di

/** Graphe Koin complet de l'app, commun aux trois plateformes — centralisé ici pour qu'Android
 * (G5Application), iOS (BasketballDraftAppViewController) et web (main.kt) démarrent exactement
 * les mêmes modules, sans risque de divergence entre points d'entrée. */
fun appKoinModules() = listOf(
    coreModule,
    soundPlayerModule(),
    databaseModule(),
    networkModule,
    repositoryModule(),
    useCaseModule,
    viewModelModule
)
