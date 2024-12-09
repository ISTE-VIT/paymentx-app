package com.iste.paymentx.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.iste.paymentx.R
import com.iste.paymentx.ui.auth.PinVerifyPage

class Withdraw : AppCompatActivity() {
    private lateinit var amountEditText: EditText
    private lateinit var button1: Button
    private lateinit var button2: Button
    private lateinit var button3: Button
    private lateinit var continueButton: Button
    private lateinit var backArrow: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_withdraw)

        amountEditText = findViewById(R.id.amountEditText)
        button1 = findViewById(R.id.button1)
        button2 = findViewById(R.id.button2)
        button3 = findViewById(R.id.button3)
        backArrow = findViewById(R.id.previouspage)
        continueButton = findViewById(R.id.continueButton)

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
        continueButton.setOnClickListener {
            val amount = amountEditText.text.toString().toDoubleOrNull()
            if (amount != null && amount > 0) {
                val intent = Intent(this, PinVerifyPage::class.java)
                intent.putExtra("EXTRA_AMOUNT", amount) // Optional: Pass the amount if needed in PinVerifyPage
                intent.putExtra("CALLING_ACTIVITY", "Withdraw") // 👈 Pass the calling activity info
                startActivity(intent)
            } else {
                Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateAmount(amountToAdd: Int) {
        val currentAmount = amountEditText.text.toString().toIntOrNull() ?: 0
        val newAmount = currentAmount + amountToAdd
        amountEditText.setText(newAmount.toString())
    }

    private fun navigateToMainScreen() {
        val intent = Intent(this, MainScreen::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
}
