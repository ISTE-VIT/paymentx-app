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

class PinVerifyPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pin_verify_page)

        val sharedPref = getSharedPreferences("PaymentX", MODE_PRIVATE)
        val storedPin = sharedPref.getString("transactionPin", null)

        val pinInputs = listOf(
            findViewById<EditText>(R.id.input1),
            findViewById<EditText>(R.id.input2),
            findViewById<EditText>(R.id.input3),
            findViewById<EditText>(R.id.input4),
            findViewById<EditText>(R.id.input5),
            findViewById<EditText>(R.id.input6)
        )

        val btnVerify = findViewById<Button>(R.id.btnVerify)

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

        btnVerify.setOnClickListener {
            val enteredPin = pinInputs.joinToString("") { it.text.toString() }

            if (enteredPin.length == 6) {
                if (enteredPin == storedPin) {
                    val amount = intent.getIntExtra("EXTRA_AMOUNT", 0)

                    if (amount > 0) {
                        // Check if it's from Withdraw or TopUp
                        val callingActivity = intent.getStringExtra("CALLING_ACTIVITY")
                        val intent = if (callingActivity == "Withdraw") {
                            Intent(this, WithdrawCompleted::class.java)
                        } else {
                            Intent(this, TopUpCompleted::class.java)
                        }

                        intent.putExtra("EXTRA_AMOUNT", amount)
                        startActivity(intent)
                        finish()
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
