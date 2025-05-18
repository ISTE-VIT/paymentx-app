package com.iste.paymentX.ui.main

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.button.MaterialButton
import com.iste.paymentX.R

class ContactUs : AppCompatActivity() {

    // Declare UI components
    private lateinit var backButton: ImageView
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var subjectEditText: EditText
    private lateinit var messageEditText: EditText
    private lateinit var submitButton: MaterialButton
    private lateinit var userNameDisplay: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_contact_us)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Initialize UI components
        initViews()

        // Set up user's name if available
        setupUserName()

        // Set up click listeners
        setupListeners()
    }

    private fun initViews() {
        backButton = findViewById(R.id.back)
        nameEditText = findViewById(R.id.enter_name)
        emailEditText = findViewById(R.id.email)
        phoneEditText = findViewById(R.id.phno)
        subjectEditText = findViewById(R.id.subject)
        messageEditText = findViewById(R.id.message)
        submitButton = findViewById(R.id.submit_button)
        userNameDisplay = findViewById(R.id.name)
    }

    private fun setupUserName() {
        // You would typically get this from SharedPreferences or your user data
        val userName = getUserName() // Implement this method based on your app's user management
        if (!userName.isNullOrEmpty()) {
            userNameDisplay.text = "Hello, $userName!"
        } else {
            userNameDisplay.text = "Hello there!"
        }
    }

    private fun getUserName(): String? {
        // This is a placeholder - implement according to how your app stores user data
        // Example with SharedPreferences:
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("user_name", null)

        // For testing purposes, you can return a hardcoded value:
        // return "Rudra"
    }

    private fun setupListeners() {
        // Back button click listener
        backButton.setOnClickListener {
            onBackPressed()
        }

        // Submit button click listener
        submitButton.setOnClickListener {
            if (validateInputs()) {
                submitContactForm()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Validate name
        if (nameEditText.text.toString().trim().isEmpty()) {
            nameEditText.error = "Name is required"
            isValid = false
        }

        // Validate email
        val email = emailEditText.text.toString().trim()
        if (email.isEmpty()) {
            emailEditText.error = "Email is required"
            isValid = false
        } else if (!isValidEmail(email)) {
            emailEditText.error = "Enter a valid email address"
            isValid = false
        }

        // Validate phone number
        val phone = phoneEditText.text.toString().trim()
        if (phone.isEmpty()) {
            phoneEditText.error = "Phone number is required"
            isValid = false
        } else if (phone.length != 10 || !TextUtils.isDigitsOnly(phone)) {
            phoneEditText.error = "Enter a valid 10-digit phone number"
            isValid = false
        }

        // Validate subject
        if (subjectEditText.text.toString().trim().isEmpty()) {
            subjectEditText.error = "Subject is required"
            isValid = false
        }

        // Validate message
        if (messageEditText.text.toString().trim().isEmpty()) {
            messageEditText.error = "Message is required"
            isValid = false
        }

        return isValid
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun submitContactForm() {
        // Collect form data
        val name = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()
        val subject = subjectEditText.text.toString().trim()
        val message = messageEditText.text.toString().trim()

        // Here you would typically send this data to your server or API
        // For now, we'll just show a success message and clear the form

        // Show success message
        Toast.makeText(
            this,
            "Thank you for your message. We'll get back to you soon!",
            Toast.LENGTH_LONG
        ).show()

        // Clear the form
        clearForm()

        // Optionally navigate back or to another screen
        // finish() // Uncomment to close the activity after submission
    }

    private fun clearForm() {
        nameEditText.text.clear()
        emailEditText.text.clear()
        phoneEditText.text.clear()
        subjectEditText.text.clear()
        messageEditText.text.clear()
    }
}