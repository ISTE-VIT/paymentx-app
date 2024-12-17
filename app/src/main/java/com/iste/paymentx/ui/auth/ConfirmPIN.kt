package com.iste.paymentx.ui.auth

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.iste.paymentx.R
import com.iste.paymentx.ui.main.MainScreen

class ConfirmPIN : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_confirm_pin)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Retrieve the PIN passed from CreateTransPIN
        val transactionPin = intent.getStringExtra("transactionPin")
        if (transactionPin == null) {
            Toast.makeText(this, "Transaction PIN not received!", Toast.LENGTH_SHORT).show()
            finish() // Exit if no PIN is passed
            return
        }

        val pinInputs = listOf(
            findViewById<EditText>(R.id.input1),
            findViewById<EditText>(R.id.input2),
            findViewById<EditText>(R.id.input3),
            findViewById<EditText>(R.id.input4),
            findViewById<EditText>(R.id.input5),
            findViewById<EditText>(R.id.input6)
        )

        pinInputs.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1) {
                        // Move to the next EditText
                        if (index < pinInputs.size - 1) {
                            pinInputs[index + 1].requestFocus()
                        }
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            editText.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    // Clear current EditText and move to the previous one
                    if (editText.text.isEmpty() && index > 0) {
                        pinInputs[index - 1].requestFocus()
                        pinInputs[index - 1].text.clear()
                    }
                }
                false
            }
        }

        val btnVerify = findViewById<Button>(R.id.btnVerify)
        btnVerify.setOnClickListener {
            // Gather input PIN
            val enteredPin = pinInputs.joinToString("") { it.text.toString() }

            if (enteredPin.length == 6) {
                if (enteredPin == transactionPin) {
                    // Redirect to MainScreen
                    val intent = Intent(this, MainScreen::class.java)
                    startActivity(intent)
                    finish() // Optional: Close this activity
                } else {
                    // Show error if PINs do not match
                    Toast.makeText(this, "PINs do not match. Try again.", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Show error if PIN is incomplete
                Toast.makeText(this, "Please enter all 6 digits.", Toast.LENGTH_SHORT).show()
            }
        }
        val sharedPref = getSharedPreferences("PaymentX", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("transactionPin", transactionPin)  // Save PIN
        editor.apply()
    }
}
