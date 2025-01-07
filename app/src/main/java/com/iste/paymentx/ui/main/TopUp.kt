package com.iste.paymentx.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.iste.paymentx.R
import com.iste.paymentx.ui.auth.PinVerifyPage
import android.view.animation.AnimationUtils

class TopUp : AppCompatActivity() {
    private lateinit var amountEditText: EditText
    private lateinit var button1: Button
    private lateinit var button2: Button
    private lateinit var button3: Button
    private lateinit var backArrow: ImageView
    private lateinit var continueButton: Button
    private lateinit var vibrator: Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_top_up)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Initialize vibrator
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Initialize views
        amountEditText = findViewById(R.id.amountEditText)
        button1 = findViewById(R.id.button1)
        button2 = findViewById(R.id.button2)
        button3 = findViewById(R.id.button3)
        backArrow = findViewById(R.id.previouspage)
        continueButton = findViewById(R.id.continueButton)

        // Add TextWatcher for auto-resizing
        setupAutoResizingEditText()

        // Set click listeners for quick amount buttons
        button1.setOnClickListener {
            updateAmount(1000)
        }
        button2.setOnClickListener {
            updateAmount(500)
        }
        button3.setOnClickListener {
            updateAmount(100)
        }
        // Set click listener for back arrow
        backArrow.setOnClickListener {
            navigateToMainScreen()
        }
        // Set click listener for continue button
        continueButton.setOnClickListener {
            val amount = amountEditText.text.toString().toDoubleOrNull()
            if (amount != null && amount > 0) {
                val intent = Intent(this, PinVerifyPage::class.java)
                intent.putExtra("EXTRA_AMOUNT", amount) // Optional: Pass the amount if needed in PinVerifyPage
                intent.putExtra("CALLING_ACTIVITY", "TopUp") // 👈 Add this line to specify the action
                startActivity(intent)
            } else {
                Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                vibratePhone()
            }
        }
    }

    private fun vibratePhone() {
        if (vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(200)
            }
        }
    }


    private fun setupAutoResizingEditText() {
        // Set initial width to wrap_content
        amountEditText.layoutParams.width = resources.getDimensionPixelSize(R.dimen.min_edit_text_width)

        amountEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            @SuppressLint("ResourceType")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Calculate new width based on text length
                val textLength = s?.length ?: 0
                val minWidth = resources.getDimensionPixelSize(R.dimen.min_edit_text_width)
                val digitWidth = resources.getDimensionPixelSize(R.dimen.digit_width)

                // Calculate new width (minimum width + extra space for each digit)
                val newWidth = minWidth + (textLength * digitWidth)

                // Update EditText width
                val layoutParams = amountEditText.layoutParams
                layoutParams.width = newWidth
                amountEditText.layoutParams = layoutParams

                // Check if amount exceeds 5 lakhs
                val amount = s.toString().toIntOrNull() ?: 0
                if (amount > 500000) {
                    // Show error message
                    amountEditText.error = "Amount cannot be more than ₹5,00,000"
                    // Apply shake animation
                    val shakeAnimation = AnimationUtils.loadAnimation(this@TopUp, R.drawable.shake_animation)
                    amountEditText.startAnimation(shakeAnimation)
                    // Vibrate phone
                    vibratePhone()
                    // Clear the excess amount
                    amountEditText.setText("500000")
                    amountEditText.setSelection(amountEditText.text.length)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    @SuppressLint("ResourceType")
    private fun updateAmount(amountToAdd: Int) {
        val currentAmount = amountEditText.text.toString().toIntOrNull() ?: 0
        val newAmount = currentAmount + amountToAdd
        if (newAmount <= 500000) {
            amountEditText.setText(newAmount.toString())
        } else {
            // Show error message
            amountEditText.error = "Amount cannot be more than ₹5,00,000"
            // Apply shake animation
            val shakeAnimation = AnimationUtils.loadAnimation(this, R.drawable.shake_animation)
            amountEditText.startAnimation(shakeAnimation)
            // Vibrate phone
            vibratePhone()
        }
    }
    private fun navigateToMainScreen() {
        val intent = Intent(this, MainScreen::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish() // Finish the current activity so it is removed from the back stack
    }
}
