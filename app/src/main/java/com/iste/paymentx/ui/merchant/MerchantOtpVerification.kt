package com.iste.paymentx.ui.merchant

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.iste.paymentx.R
import com.iste.paymentx.ui.auth.CreateTransPIN
import com.iste.paymentx.ui.auth.Phonenumber

class MerchantOtpVerification : AppCompatActivity() {
    private lateinit var backarrow: ImageView

    // store credientials
    private var userName: String? = null
    private var userEmail: String? = null
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_merchant_otp_verification)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Get the user information from intent
        userName = intent.getStringExtra("USER_NAME")
        userEmail = intent.getStringExtra("USER_EMAIL")
        userId = intent.getStringExtra("USER_ID")


        val phoneNumber = intent.getStringExtra("phoneNumber")
        val backarrow = findViewById<ImageView>(R.id.merchback)

        // Add this line to update the TextView
        val otpText = findViewById<TextView>(R.id.merchenter_otp_text)
        otpText.text = getString(R.string.otp_message, phoneNumber)

        val inputs = listOf(
            findViewById<EditText>(R.id.merchuid_input1),
            findViewById<EditText>(R.id.merchuid_input2),
            findViewById<EditText>(R.id.merchuid_input3),
            findViewById<EditText>(R.id.merchuid_input4),
            findViewById<EditText>(R.id.merchuid_input5)
        )
        setUpOtpInputs(inputs)

        backarrow.setOnClickListener {
            val intent = Intent(this, MerchantPhoneNumberVerification::class.java)
            startActivity(intent)
        }

        val btnVerify = findViewById<Button>(R.id.merchbtnVerify)
        btnVerify.setOnClickListener{
            val intent = Intent(this, MerchantCreateTransPIN::class.java).apply {
                putExtra("USER_NAME", userName)
                putExtra("USER_EMAIL", userEmail)
                putExtra("USER_ID", userId)
            }
            startActivity(intent)
        }
    }

    private fun setUpOtpInputs(inputs: List<EditText>) {
        for (i in inputs.indices) {
            val current = inputs[i]
            val next = inputs.getOrNull(i + 1) // Get next input or null if last
            val previous = inputs.getOrNull(i - 1) // Get previous input or null if first

            current.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (!s.isNullOrEmpty()) {
                        next?.requestFocus() // Move to the next box
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            current.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (current.text.isEmpty()) {
                        previous?.apply {
                            requestFocus() // Move to the previous box
                            text.clear() // Clear the previous box
                        }
                    }
                }
                false
            }
        }
    }
}