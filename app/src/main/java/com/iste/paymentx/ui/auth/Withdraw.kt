package com.iste.paymentx.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.iste.paymentx.R

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

        button1.setOnClickListener { updateAmount(1000) }
        button2.setOnClickListener { updateAmount(500) }
        button3.setOnClickListener { updateAmount(100) }

        backArrow.setOnClickListener { navigateToMainScreen() }

        continueButton.setOnClickListener { handleContinue() }
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

    private fun handleContinue() {
        val enteredAmount = amountEditText.text.toString().toIntOrNull()

        if (enteredAmount == null || enteredAmount <= 0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
        } else {
            // Pass the amount to PinVerifyPage
            val intent = Intent(this, PinVerifyPage::class.java)
            intent.putExtra("EXTRA_AMOUNT", enteredAmount) // Pass the amount to the PIN verify page
            startActivity(intent)
        }
    }
}
