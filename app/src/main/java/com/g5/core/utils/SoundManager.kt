package com.g5.core.utils

import android.content.Context
import android.media.MediaPlayer
import com.g5.R

class SoundManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    fun playSound(resId: Int) {
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

    fun stopSound() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playBeginAuction() = playSound(R.raw.begin_auction)
    fun playWinAuction() = playSound(R.raw.win_auction)
    fun playResultScreen(isWinner: Boolean = true) {
        if (isWinner) playSound(R.raw.result_screen)
        else playSound(R.raw.result_screen_lose)
    }
    fun playAlarmAuction() = playSound(R.raw.alarm_auction)
    fun playActionBuzzer() = playSound(R.raw.action_buzzer)

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
