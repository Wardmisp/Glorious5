package com.g5.core.utils

/** Stub silencieux — pas encore de lecture audio réelle sur iOS (voir SoundPlayer.kt). */
class NoopSoundPlayer : SoundPlayer {
    override var isEnabled: Boolean = true

    override fun stopSound() {}
    override fun playBeginAuction() {}
    override fun playWinAuction() {}
    override fun playResultScreen(isWinner: Boolean) {}
    override fun playAlarmAuction() {}
    override fun playActionBuzzer() {}
    override fun playActionBegin() {}
}
