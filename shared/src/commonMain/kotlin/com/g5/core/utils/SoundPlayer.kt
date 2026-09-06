package com.g5.core.utils

/** Sons d'ambiance de la partie (début/fin d'enchère, chrono, etc.) — implémenté avec
 * `MediaPlayer` sur Android ; l'implémentation iOS est un stub silencieux pour l'instant (voir
 * SoundPlayer.ios.kt), en attendant une vraie lecture audio (AVFoundation). */
interface SoundPlayer {
    var isEnabled: Boolean

    fun stopSound()
    fun playBeginAuction()
    fun playWinAuction()
    fun playResultScreen(isWinner: Boolean = true)
    fun playAlarmAuction()
    fun playActionBuzzer()
    fun playActionBegin()
}
