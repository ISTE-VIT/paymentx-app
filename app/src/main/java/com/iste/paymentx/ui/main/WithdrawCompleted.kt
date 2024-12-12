package com.iste.paymentx.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.iste.paymentx.R

class WithdrawCompleted : AppCompatActivity() {

    private lateinit var withdrawnAmountTextView: TextView
    private lateinit var backArrowImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_withdraw_completed)

        withdrawnAmountTextView = findViewById(R.id.textView2)
        backArrowImageView = findViewById(R.id.imageView3)

        val withdrawnAmount = intent.getIntExtra("EXTRA_AMOUNT", 0)
        withdrawnAmountTextView.text = "₹$withdrawnAmount"

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
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
}