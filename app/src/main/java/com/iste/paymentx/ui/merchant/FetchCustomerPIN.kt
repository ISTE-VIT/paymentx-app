package com.iste.paymentx.ui.merchant

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.iste.paymentx.R
import com.iste.paymentx.ui.main.TickMarkAnimation

class FetchCustomerPIN : AppCompatActivity() {
    private var amount: Int = 0
    private lateinit var btnVerify: Button
    private lateinit var vibrator: Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_fetch_customer_pin)

        /// Get and verify amount
        amount = intent.getIntExtra("EXTRA_AMOUNT", 0)

        val amountText = findViewById<TextView>(R.id.textView11)
        amountText.text = "to Pay ₹$amount"

        // Initialize vibrator
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

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

        btnVerify = findViewById(R.id.btnVerify)
        btnVerify.setOnClickListener {
            val enteredPin = pinInputs.joinToString("") { it.text.toString() }
            if (enteredPin.length == 6) {
                val tickIntent = Intent(this, TickMarkAnimation::class.java)
                startActivity(tickIntent)
                Handler(Looper.getMainLooper()).postDelayed({
                    val destinationIntent = Intent(this, ReceiveCompleted::class.java).apply {
                        putExtra("EXTRA_AMOUNT", amount)
                    }
                    startActivity(destinationIntent)
                    finish()
                },2000)
            } else {
                Toast.makeText(this, "Please enter all 6 digits.", Toast.LENGTH_SHORT).show()
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
}