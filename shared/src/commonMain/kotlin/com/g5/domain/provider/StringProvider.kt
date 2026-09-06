package com.g5.domain.provider

/**
 * Résout un texte localisé pour le domaine, qui ne doit pas dépendre d'Android (donc pas de
 * `Context`/`R.string` directement) — l'implémentation réelle (core/provider) fait le lien avec
 * les ressources Android. Actuellement uniquement les commentaires de simulation de match, seul
 * texte utilisateur généré dans la couche domaine.
 */
interface StringProvider {
    fun commentary(key: CommentaryKey, actorName: String, opponentName: String): String
}

/** Une réplique commentée possible pour une action de [com.g5.domain.usecase.GenerateMatchSimulationUseCase].
 * Chacune prend deux arguments de formatage : l'auteur de l'action puis l'adversaire concerné. */
enum class CommentaryKey {
    COMMON_BLOCK, COMMON_STEAL, COMMON_REBOUND_DUEL, COMMON_FOUL_DRAWN, COMMON_STEEL_DEFENSE, COMMON_READ, COMMON_COLD_BLOODED,
    BACKCOURT_THREE_POINTER, BACKCOURT_BLIND_PASS, BACKCOURT_STEPBACK, BACKCOURT_FLOATER, BACKCOURT_ANKLE_BREAKER, BACKCOURT_CROSSOVER,
    FRONTCOURT_POSTERIZE, FRONTCOURT_ALLEY_OOP, FRONTCOURT_ILLEGAL_BLOCK, FRONTCOURT_AND_ONE, FRONTCOURT_OFFENSIVE_REBOUND, FRONTCOURT_POST_MOVE,
    WING_FULL_COURT, WING_ASSIST, WING_MIDRANGE;

    companion object {
        val COMMON = listOf(COMMON_BLOCK, COMMON_STEAL, COMMON_REBOUND_DUEL, COMMON_FOUL_DRAWN, COMMON_STEEL_DEFENSE, COMMON_READ, COMMON_COLD_BLOODED)
        val BACKCOURT = listOf(BACKCOURT_THREE_POINTER, BACKCOURT_BLIND_PASS, BACKCOURT_STEPBACK, BACKCOURT_FLOATER, BACKCOURT_ANKLE_BREAKER, BACKCOURT_CROSSOVER)
        val FRONTCOURT = listOf(FRONTCOURT_POSTERIZE, FRONTCOURT_ALLEY_OOP, FRONTCOURT_ILLEGAL_BLOCK, FRONTCOURT_AND_ONE, FRONTCOURT_OFFENSIVE_REBOUND, FRONTCOURT_POST_MOVE)
        val WING = listOf(WING_FULL_COURT, WING_ASSIST, WING_MIDRANGE)
    }
}
