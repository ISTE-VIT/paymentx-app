package com.iste.paymentX.ui.main

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
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
    private lateinit var attachFileButton: CardView
    private lateinit var attachmentTextView: TextView

    // Firebase
    private lateinit var auth: FirebaseAuth

    // File attachment
    private var selectedFileUri: Uri? = null

    // Activity result launcher for file picking
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            // Display the file name
            val fileName = getFileNameFromUri(uri)
            attachmentTextView.text = "File: $fileName"
            attachmentTextView.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_contact_us)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize UI components
        initViews()

        // Set up user's name from Firebase Auth
        setUserName()

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

        // Find the attach file button by its parent CardView
        val attachCardView = findViewById<View>(R.id.card_attach_file)
        attachFileButton = attachCardView as CardView

        // Create TextView for showing attachment name (to be added to layout)
        attachmentTextView = TextView(this)
        attachmentTextView.id = View.generateViewId()
        attachmentTextView.setTextColor(getColor(R.color.dark_green))
        attachmentTextView.textSize = 16f
        attachmentTextView.visibility = View.GONE
    }

    private fun setUserName() {
        // Get current user from Firebase
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Get the user's display name, or their email prefix if display name is null
            val displayName = currentUser.displayName?.split(" ")?.first() ?: currentUser.email?.substringBefore('@') ?: "User"
            userNameDisplay.text = "Hello, $displayName!"
        } else {
            // Fallback if somehow user isn't authenticated
            userNameDisplay.text = "Hello, User!"
        }
    }

    private fun setupListeners() {
        // Back button click listener
        backButton.setOnClickListener {
            onBackPressed()
        }

        // Attach file click listener
        attachFileButton.setOnClickListener {
            openFilePicker()
        }

        // Submit button click listener
        submitButton.setOnClickListener {
            if (validateInputs()) {
                submitContactForm()
            }
        }
    }

    private fun openFilePicker() {
        // Launch file picker intent
        filePickerLauncher.launch("*/*") // Accept all file types
    }

    private fun getFileNameFromUri(uri: Uri): String {
        val contentResolver = applicationContext.contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)

        return cursor?.use {
            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            if (nameIndex >= 0) it.getString(nameIndex) else "Unknown file"
        } ?: "Unknown file"
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

        // File attachment info (if any)
        val fileInfo = selectedFileUri?.let {
            getFileNameFromUri(it)
        } ?: "No file attached"

        // Here you would typically send this data to your server or API
        // For now, we'll just show a success message and clear the form

        // Show success message with file info
        Toast.makeText(
            this,
            "Thank you for your message. We'll get back to you soon! ($fileInfo)",
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
        selectedFileUri = null
        attachmentTextView.visibility = View.GONE
    }
}