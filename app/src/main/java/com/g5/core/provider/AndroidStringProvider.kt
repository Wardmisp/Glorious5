package com.g5.core.provider

import android.content.Context
import com.g5.R
import com.g5.domain.provider.CommentaryKey
import com.g5.domain.provider.StringProvider

/** Seule implémentation de [StringProvider] — fait le pont entre le domaine (qui ignore Android)
 * et les ressources `R.string`. */
class AndroidStringProvider(private val context: Context) : StringProvider {

    override fun commentary(key: CommentaryKey, actorName: String, opponentName: String): String {
        val resId = when (key) {
            CommentaryKey.COMMON_BLOCK -> R.string.commentary_common_block
            CommentaryKey.COMMON_STEAL -> R.string.commentary_common_steal
            CommentaryKey.COMMON_REBOUND_DUEL -> R.string.commentary_common_rebound_duel
            CommentaryKey.COMMON_FOUL_DRAWN -> R.string.commentary_common_foul_drawn
            CommentaryKey.COMMON_STEEL_DEFENSE -> R.string.commentary_common_steel_defense
            CommentaryKey.COMMON_READ -> R.string.commentary_common_read
            CommentaryKey.COMMON_COLD_BLOODED -> R.string.commentary_common_cold_blooded
            CommentaryKey.BACKCOURT_THREE_POINTER -> R.string.commentary_backcourt_three_pointer
            CommentaryKey.BACKCOURT_BLIND_PASS -> R.string.commentary_backcourt_blind_pass
            CommentaryKey.BACKCOURT_STEPBACK -> R.string.commentary_backcourt_stepback
            CommentaryKey.BACKCOURT_FLOATER -> R.string.commentary_backcourt_floater
            CommentaryKey.BACKCOURT_ANKLE_BREAKER -> R.string.commentary_backcourt_ankle_breaker
            CommentaryKey.BACKCOURT_CROSSOVER -> R.string.commentary_backcourt_crossover
            CommentaryKey.FRONTCOURT_POSTERIZE -> R.string.commentary_frontcourt_posterize
            CommentaryKey.FRONTCOURT_ALLEY_OOP -> R.string.commentary_frontcourt_alley_oop
            CommentaryKey.FRONTCOURT_ILLEGAL_BLOCK -> R.string.commentary_frontcourt_illegal_block
            CommentaryKey.FRONTCOURT_AND_ONE -> R.string.commentary_frontcourt_and_one
            CommentaryKey.FRONTCOURT_OFFENSIVE_REBOUND -> R.string.commentary_frontcourt_offensive_rebound
            CommentaryKey.FRONTCOURT_POST_MOVE -> R.string.commentary_frontcourt_post_move
            CommentaryKey.WING_FULL_COURT -> R.string.commentary_wing_full_court
            CommentaryKey.WING_ASSIST -> R.string.commentary_wing_assist
            CommentaryKey.WING_MIDRANGE -> R.string.commentary_wing_midrange
        }
        return context.getString(resId, actorName, opponentName)
    }
}
