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
    fun playResultScreen() = playSound(R.raw.result_screen)
    fun playAlarmAuction() = playSound(R.raw.alarm_auction)

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
