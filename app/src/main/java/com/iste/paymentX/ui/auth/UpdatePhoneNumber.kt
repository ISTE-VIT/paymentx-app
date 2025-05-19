package com.iste.paymentX.ui.auth

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.iste.paymentX.R

class UpdatePhoneNumber : AppCompatActivity() {

    private lateinit var phoneNumberEditText: EditText
    private lateinit var confirmButton: Button
    private lateinit var backButton: ImageView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_update_phone_number)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize UI elements
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText)
        confirmButton = findViewById(R.id.confirmButton)
        backButton = findViewById(R.id.back)

        // Set back button click listener
        backButton.setOnClickListener {
            finish() // Go back to the previous screen
        }

        // Set up phone number input validation
        setupPhoneValidation()

        // Set up confirm button click listener
        confirmButton.setOnClickListener {
            validateAndProceed()
        }
    }

    private fun setupPhoneValidation() {
        // Validate phone number as user types
        phoneNumberEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                // Enable button only if phone number is 10 digits
                confirmButton.isEnabled = s?.length == 10
            }
        })
    }

    private fun validateAndProceed() {
        val phoneNumber = phoneNumberEditText.text.toString().trim()

        if (phoneNumber.length != 10) {
            Toast.makeText(this, "Please enter a 10-digit phone number", Toast.LENGTH_SHORT).show()
            return
        }

        // All validation passed, proceed to OTP verification
        navigateToOtpVerification(phoneNumber)
    }

    private fun navigateToOtpVerification(phoneNumber: String) {
        // Get current user details to pass along
        val currentUser = auth.currentUser
        val userName = currentUser?.displayName ?: ""
        val userEmail = currentUser?.email ?: ""
        val userId = currentUser?.uid ?: ""

        // Create intent for OTP verification
        val intent = Intent(this, UpdatePhoneOtpVerification::class.java).apply {
            putExtra("phoneNumber", phoneNumber)
            putExtra("USER_NAME", userName)
            putExtra("USER_EMAIL", userEmail)
            putExtra("USER_ID", userId)
        }
        startActivity(intent)
    }
}