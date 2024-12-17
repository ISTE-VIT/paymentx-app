package com.iste.paymentx.ui.auth

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.iste.paymentx.R
import com.iste.paymentx.ui.main.TickMarkAnimation
import com.iste.paymentx.ui.main.TopUpCompleted
import com.iste.paymentx.ui.main.WithdrawCompleted

class PinVerifyPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pin_verify_page)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val sharedPref = getSharedPreferences("PaymentX", MODE_PRIVATE)
        val storedPin = sharedPref.getString("transactionPin", null)

        // PIN input fields
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
            val enteredPin = pinInputs.joinToString("") { it.text.toString() }

            if (enteredPin.length == 6) {
                if (enteredPin == storedPin) {
                    val callingActivity = intent.getStringExtra("CALLING_ACTIVITY")
                    if (callingActivity == "ViewBalance") {
                        setResult(RESULT_OK) // Send success result to MainScreen
                        finish()
                    } else {
                        val amount = intent.getDoubleExtra("EXTRA_AMOUNT", 0.0).toInt()

                        if (amount > 0) {
                            // Start tick mark animation
                            val tickIntent = Intent(this, TickMarkAnimation::class.java)
                            startActivity(tickIntent)

                            // Determine the destination based on the calling activity
                            Handler(Looper.getMainLooper()).postDelayed({
                                val destinationIntent = if (callingActivity == "Withdraw") {
                                    Intent(this, WithdrawCompleted::class.java).apply {
                                        putExtra("EXTRA_AMOUNT", amount)
                                    }
                                } else if (callingActivity == "TopUp") {
                                    Intent(this, TopUpCompleted::class.java).apply {
                                        putExtra("EXTRA_AMOUNT", amount)
                                    }
                                } else {
                                    Toast.makeText(this, "Invalid operation", Toast.LENGTH_SHORT).show()
                                    return@postDelayed
                                }
                                startActivity(destinationIntent)
                                finish()
                            }, 2000) // 2 seconds delay for tick animation
                        } else {
                            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "PINs do not match. Try again.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter all 6 digits.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}