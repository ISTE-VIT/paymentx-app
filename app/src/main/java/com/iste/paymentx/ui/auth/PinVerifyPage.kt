package com.iste.paymentx.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.iste.paymentx.R
import com.iste.paymentx.ui.main.TopUpCompleted
import com.iste.paymentx.ui.main.WithdrawCompleted

class PinVerifyPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pin_verify_page)

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

        // Verify button
        val btnVerify = findViewById<Button>(R.id.btnVerify)

        // Auto-focus and navigation between PIN input fields
        pinInputs.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && index < pinInputs.size - 1) {
                        pinInputs[index + 1].requestFocus()
                    } else if (s.isNullOrEmpty() && index > 0) {
                        pinInputs[index - 1].requestFocus()
                    }
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
        // Verify button click listener
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
                            // Determine the destination based on the calling activity
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
                                return@setOnClickListener
                            }
                            startActivity(destinationIntent)
                            finish()
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
