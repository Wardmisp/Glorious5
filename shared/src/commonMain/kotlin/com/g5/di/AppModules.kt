package com.g5.di

/** Graphe Koin complet de l'app, commun aux deux plateformes — centralisé ici pour qu'Android
 * (G5Application) et iOS (BasketballDraftAppViewController) démarrent exactement les mêmes
 * modules, sans risque de divergence entre les deux points d'entrée. */
fun appKoinModules() = listOf(
    coreModule,
    soundPlayerModule(),
    databaseModule(),
    networkModule,
    repositoryModule,
    useCaseModule,
    viewModelModule
)
