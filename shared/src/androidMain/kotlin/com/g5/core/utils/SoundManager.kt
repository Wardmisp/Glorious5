package com.g5.core.utils

import android.content.Context
import android.media.MediaPlayer
import com.g5.shared.R

class SoundManager(private val context: Context) : SoundPlayer {
    private var mediaPlayer: MediaPlayer? = null
    override var isEnabled: Boolean = true

    fun playSound(resId: Int) {
        if (!isEnabled) return
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, resId)
            mediaPlayer?.setOnCompletionListener { 
                it.release()
                if (mediaPlayer == it) mediaPlayer = null
            }
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun stopSound() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun playBeginAuction() = playSound(R.raw.begin_auction)
    override fun playWinAuction() = playSound(R.raw.win_auction)
    override fun playResultScreen(isWinner: Boolean) {
        if (isWinner) playSound(R.raw.result_screen)
        else playSound(R.raw.result_screen_lose)
    }
    override fun playAlarmAuction() = playSound(R.raw.alarm_auction)
    override fun playActionBuzzer() = playSound(R.raw.action_buzzer)
    override fun playActionBegin() = playSound(R.raw.action_begin)

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
