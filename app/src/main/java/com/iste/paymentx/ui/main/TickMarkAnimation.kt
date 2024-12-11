package com.iste.paymentx.ui.main

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.iste.paymentx.R

class TickMarkAnimation : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tick_mark_animation)

        // Initialize MediaPlayer with a success sound
        // Note: Replace R.raw.success_sound with an actual sound file you have in your raw folder
        mediaPlayer = MediaPlayer.create(this, R.raw.success_sound)

        // Optional: Set volume (0.0 to 1.0)
        mediaPlayer?.setVolume(0.5f, 0.5f)

        // Optional: Programmatically control the animation
        val tickAnimationView: LottieAnimationView = findViewById(R.id.tick)

        // Play sound when activity starts
        try {
            mediaPlayer?.start()
        } catch (e: Exception) {
            // Log or handle any sound playing errors
            e.printStackTrace()
        }

        // Automatically finish the activity after animation
        Handler(Looper.getMainLooper()).postDelayed({
            // Stop and release the media player
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null

            finish()
        }, 2000) // 2 seconds
    }

    override fun onDestroy() {
        super.onDestroy()
        // Ensure media player is released when activity is destroyed
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
    }
}