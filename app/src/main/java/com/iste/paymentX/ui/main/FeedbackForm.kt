package com.iste.paymentX.ui.main

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.iste.paymentX.R

class FeedbackForm : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var ratingBar: RatingBar
    private lateinit var commentsEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var cancelButton: Button
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_feedback_form)

        // Initialize UI components
        nameEditText = findViewById(R.id.name)
        emailEditText = findViewById(R.id.email)
        ratingBar = findViewById(R.id.rating)
        commentsEditText = findViewById(R.id.comments)
        submitButton = findViewById(R.id.submit_button)
        cancelButton = findViewById(R.id.cancel_button)
        backButton = findViewById(R.id.back)

        // Set up rating bar style
//        ratingBar.progressDrawable = getDrawable(R.drawable.custom_rating_bar)

        // Set up button click listeners
        submitButton.setOnClickListener {
            submitFeedback()
        }

        cancelButton.setOnClickListener {
            finish()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun submitFeedback() {
        // Validate input
        val name = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val rating = ratingBar.rating
        val comments = commentsEditText.text.toString().trim()

        if (name.isEmpty()) {
            nameEditText.error = "Please enter your name"
            return
        }

        if (email.isEmpty()) {
            emailEditText.error = "Please enter your email"
            return
        }

        if (!isValidEmail(email)) {
            emailEditText.error = "Please enter a valid email"
            return
        }

        if (rating == 0f) {
            Toast.makeText(this, "Please rate your experience", Toast.LENGTH_SHORT).show()
            return
        }

        // Process the feedback (you would normally send this to a server)
        // For now, just show a success message and close the activity
        Toast.makeText(this, "Feedback submitted successfully", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }
}