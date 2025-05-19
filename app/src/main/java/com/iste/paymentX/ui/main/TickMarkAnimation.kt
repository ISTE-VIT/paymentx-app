package com.iste.paymentX.ui.main

import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Window
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.iste.paymentX.R

class TickMarkAnimation : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var tickAnimationView: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set fullscreen for a cleaner animation experience
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        enableEdgeToEdge()
        setContentView(R.layout.activity_tick_mark_animation)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Set status bar color to match the animation background
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        // Initialize animation view
        tickAnimationView = findViewById(R.id.tick)

        // Ensure animation plays from the beginning and only once
        tickAnimationView.repeatCount = 0
        tickAnimationView.speed = 1.0f
        tickAnimationView.playAnimation()

        // Initialize MediaPlayer with success sound
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.success_sound)

            // Set volume and start playing
            mediaPlayer?.setVolume(0.7f, 0.7f)
            mediaPlayer?.start()

            // Add completion listener to release resources
            mediaPlayer?.setOnCompletionListener { mp ->
                mp.release()
                mediaPlayer = null
            }
        } catch (e: Exception) {
            // Log or handle any sound playing errors
            e.printStackTrace()
        }

        // Add listener to detect when animation finishes
        tickAnimationView.addAnimatorUpdateListener {
            if (tickAnimationView.progress >= 0.95f) {
                tickAnimationView.removeAllAnimatorListeners()
                tickAnimationView.cancelAnimation()
                // Animation is practically complete, prepare to finish
                finishWithDelay(300) // Short delay after animation completes
            }
        }

        // Backup timer in case animation listener fails
        Handler(Looper.getMainLooper()).postDelayed({
            finishWithDelay(0)
        }, 2000) // 2 seconds total maximum duration
    }

    private fun finishWithDelay(delayMillis: Long) {
        Handler(Looper.getMainLooper()).postDelayed({
            // Release media player if still active
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
                mediaPlayer = null
            }

            // Finish the activity with a fade out transition
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, delayMillis)
    }

    override fun onPause() {
        super.onPause()
        // Stop animation when activity is paused
        if (::tickAnimationView.isInitialized) {
            tickAnimationView.pauseAnimation()
        }
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