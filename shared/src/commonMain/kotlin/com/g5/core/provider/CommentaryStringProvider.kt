package com.g5.core.provider

import com.g5.domain.provider.CommentaryKey
import com.g5.domain.provider.StringProvider

/** Vrai si la langue système est l'anglais — seule bascule de langue nécessaire ici, entre le
 * français (par défaut) et l'anglais. Implémenté par plateforme (`Locale` sur Android,
 * `NSLocale` sur iOS) dans un fichier séparé pour rester synchrone : [StringProvider.commentary]
 * est appelé depuis du code non-suspend ([com.g5.domain.usecase.GenerateMatchSimulationUseCase]),
 * donc pas question de passer par les ressources Compose (dont l'accès est `suspend`). */
expect fun isEnglishLocale(): Boolean

/** Seule implémentation de [StringProvider] — remplace l'ancien `AndroidStringProvider` qui
 * lisait `R.string.commentary_*` (Android uniquement) par des gabarits en dur, identiques sur les
 * deux plateformes. */
class CommentaryStringProvider : StringProvider {
    override fun commentary(key: CommentaryKey, actorName: String, opponentName: String): String {
        val a = actorName
        val o = opponentName
        return if (isEnglishLocale()) {
            when (key) {
                CommentaryKey.COMMON_BLOCK -> "Incredible block by $a, denying $o's dunk attempt!"
                CommentaryKey.COMMON_STEAL -> "$a picks $o's pocket and takes off alone on the break!"
                CommentaryKey.COMMON_REBOUND_DUEL -> "Intense physical battle: $a wins the rebounding duel against $o."
                CommentaryKey.COMMON_FOUL_DRAWN -> "$a draws the foul as $o bulldozes through — what a commitment!"
                CommentaryKey.COMMON_STEEL_DEFENSE -> "Lockdown defense: $a gives $o no room at all and forces the turnover."
                CommentaryKey.COMMON_READ -> "Perfect read: $a anticipates $o's pass and pushes the break."
                CommentaryKey.COMMON_COLD_BLOODED -> "Ice in his veins! $a pump-fakes and gets $o to fly by before scoring."
                CommentaryKey.BACKCOURT_THREE_POINTER -> "$a drills a spectacular three right over $o's head!"
                CommentaryKey.BACKCOURT_BLIND_PASS -> "A surgical no-look pass from $a leaves $o frozen in place."
                CommentaryKey.BACKCOURT_STEPBACK -> "$a buries a deep step-back despite $o's defense!"
                CommentaryKey.BACKCOURT_FLOATER -> "Silky touch: $a finishes with an elegant floater over $o."
                CommentaryKey.BACKCOURT_ANKLE_BREAKER -> "$a strings together the dribbles and leaves $o on the floor with a nasty ankle-breaker!"
                CommentaryKey.BACKCOURT_CROSSOVER -> "High-flying sequence: $a erases $o with a devastating crossover."
                CommentaryKey.FRONTCOURT_POSTERIZE -> "$a posterizes $o with a violent dunk!"
                CommentaryKey.FRONTCOURT_ALLEY_OOP -> "$a soars for a monster alley-oop, all $o can do is watch."
                CommentaryKey.FRONTCOURT_ILLEGAL_BLOCK -> "Goaltending? No! $a cleanly pins the ball against the backboard right in front of $o."
                CommentaryKey.FRONTCOURT_AND_ONE -> "$a powers through the rim for a spectacular and-one over $o."
                CommentaryKey.FRONTCOURT_OFFENSIVE_REBOUND -> "$a dominates the paint and rips down a crucial offensive rebound over $o."
                CommentaryKey.FRONTCOURT_POST_MOVE -> "Pure power: $a backs $o down in the post and finishes with a textbook move."
                CommentaryKey.WING_FULL_COURT -> "$a goes coast to coast and finishes a layup despite $o's foul."
                CommentaryKey.WING_ASSIST -> "A gorgeous assist from $a as $o was late on the rotation."
                CommentaryKey.WING_MIDRANGE -> "$a pulls up for a silky mid-range jumper over $o's outstretched arms."
            }
        } else {
            when (key) {
                CommentaryKey.COMMON_BLOCK -> "Incroyable contre de $a qui repousse la tentative de dunk de $o."
                CommentaryKey.COMMON_STEAL -> "$a intercepte le ballon dans les mains de $o et s'en va finir seul en contre-attaque !"
                CommentaryKey.COMMON_REBOUND_DUEL -> "Duel physique intense : $a gagne son duel au rebond face à $o."
                CommentaryKey.COMMON_FOUL_DRAWN -> "$a provoque le passage en force de $o, quel engagement !"
                CommentaryKey.COMMON_STEEL_DEFENSE -> "Défense d'acier : $a ne laisse aucun espace à $o et force la perte de balle."
                CommentaryKey.COMMON_READ -> "Lecture de jeu parfaite : $a anticipe la passe de $o et lance la transition."
                CommentaryKey.COMMON_COLD_BLOODED -> "Quel sang-froid ! $a feinte le tir et oblige $o à sauter dans le vide avant de marquer."
                CommentaryKey.BACKCOURT_THREE_POINTER -> "$a marque un 3 points spectaculaire sur la tête de $o !"
                CommentaryKey.BACKCOURT_BLIND_PASS -> "Passe aveugle chirurgicale de $a qui laisse $o totalement immobile."
                CommentaryKey.BACKCOURT_STEPBACK -> "$a plante un step-back longue distance malgré la défense de $o !"
                CommentaryKey.BACKCOURT_FLOATER -> "Touché de velours : $a termine avec un floater élégant au-dessus de $o."
                CommentaryKey.BACKCOURT_ANKLE_BREAKER -> "$a enchaîne les dribbles et fait mordre la poussière à $o sur un cassage de chevilles !"
                CommentaryKey.BACKCOURT_CROSSOVER -> "Séquence de haute volée : $a efface $o d'un crossover dévastateur."
                CommentaryKey.FRONTCOURT_POSTERIZE -> "$a postérise violemment $o avec un dunk dévastateur !"
                CommentaryKey.FRONTCOURT_ALLEY_OOP -> "$a s'envole pour un alley-oop monumental, $o ne peut que regarder."
                CommentaryKey.FRONTCOURT_ILLEGAL_BLOCK -> "Contre illégal ? Non ! $a scotche proprement le ballon contre la planche devant $o."
                CommentaryKey.FRONTCOURT_AND_ONE -> "$a finit en force au cercle avec un 'and-one' spectaculaire face à $o."
                CommentaryKey.FRONTCOURT_OFFENSIVE_REBOUND -> "$a domine la raquette et arrache un rebond offensif crucial devant $o."
                CommentaryKey.FRONTCOURT_POST_MOVE -> "Puissance pure : $a enfonce $o au poste bas et finit avec un move d'école."
                CommentaryKey.WING_FULL_COURT -> "$a traverse tout le terrain et finit par un lay-up malgré la faute de $o."
                CommentaryKey.WING_ASSIST -> "Magnifique passe décisive de $a alors que $o était en retard sur la rotation."
                CommentaryKey.WING_MIDRANGE -> "$a déclenche un tir à mi-distance soyeux au-dessus des bras de $o."
            }
        }
    }
}
