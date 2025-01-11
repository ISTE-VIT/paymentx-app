package com.iste.paymentX.ui.merchant

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
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.iste.paymentX.R

class MerchantPhoneNumberVerification : AppCompatActivity() {
    private lateinit var vibrator: Vibrator

    // store credientials
    private var userName: String? = null
    private var userEmail: String? = null
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_merchant_phone_number_verification)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Get the user information from intent
        userName = intent.getStringExtra("USER_NAME")
        userEmail = intent.getStringExtra("USER_EMAIL")
        userId = intent.getStringExtra("USER_ID")

        // Initialize vibrator
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        val confirmButton = findViewById<Button>(R.id.merchconfirmButton)
        val textField = findViewById<EditText>(R.id.merchphoneNumberEditText)

        // Always enable the confirm button
        confirmButton.isEnabled = true

        // Optional: Add a TextWatcher to provide real-time feedback (not mandatory for logic)
        textField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // You can add real-time feedback logic here if needed
            }
        })

        confirmButton.setOnClickListener {
            val number = textField.text.toString()
            if (number.length == 10) {
                // Valid phone number, proceed to the next activity
                val intent = Intent(this, MerchantOtpVerification::class.java).apply {
                    putExtra("USER_NAME", userName)
                    putExtra("USER_EMAIL", userEmail)
                    putExtra("USER_ID", userId)
                    putExtra("phoneNumber", number)
                }
                startActivity(intent)
            } else {
                // Invalid phone number, show a toast
                Toast.makeText(this, "Please enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show()
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
