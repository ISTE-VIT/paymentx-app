package com.iste.paymentx.ui.main

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.iste.paymentx.R

class TopUpCompleted : AppCompatActivity() {

    private lateinit var topUpAmountTextView: TextView
    private lateinit var backArrowImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_top_up_completed)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        topUpAmountTextView = findViewById(R.id.textView2)
        backArrowImageView = findViewById(R.id.imageView3)

        val topUpAmount = intent.getIntExtra("EXTRA_AMOUNT", 0)
        topUpAmountTextView.text = "₹$topUpAmount"

        backArrowImageView.setOnClickListener {
            navigateToMainScreen()
        }

        // Handle system back button
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToMainScreen()
            }
        })
    }

    private fun navigateToMainScreen() {
        val intent = Intent(this, MainScreen::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}