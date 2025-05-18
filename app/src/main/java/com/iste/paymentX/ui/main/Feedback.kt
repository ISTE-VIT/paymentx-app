package com.iste.paymentX.ui.main

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.iste.paymentX.R

class Feedback : AppCompatActivity() {

    // UI elements
    private lateinit var searchEditText: EditText
    private lateinit var nameTextView: TextView
    private lateinit var faqCardViews: List<CardView>

    // Firebase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_feedback)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize UI elements
        nameTextView = findViewById(R.id.name)
        searchEditText = findViewById(R.id.searchEditText)

        // Get all FAQ CardViews
        faqCardViews = listOf(
            findViewById(R.id.card_1),
            findViewById(R.id.card_2),
            findViewById(R.id.card_3),
            findViewById(R.id.card_4),
            findViewById(R.id.card_5),
            findViewById(R.id.card_6),
            findViewById(R.id.card_7),
            findViewById(R.id.card_8),
            findViewById(R.id.card_9),
            findViewById(R.id.card_10),
            findViewById(R.id.card_11),
            findViewById(R.id.card_12),
            findViewById(R.id.card_13),
            findViewById(R.id.card_14)
        )

        // Set user name from Firebase Auth
        setUserName()

        // Handle back button
        val backButton: ImageView = findViewById(R.id.back)
        backButton.setOnClickListener {
            onBackPressed() // Go back to the previous screen
        }

        // Handle search functionality
        setupSearch()
    }

    private fun setUserName() {
        // Get current user from Firebase
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Get the user's display name, or their email prefix if display name is null
            val displayName = currentUser.displayName ?.split(" ")?.first() ?: "User"
            nameTextView.text = "Hello, $displayName!"
        } else {
            // Fallback if somehow user isn't authenticated
            nameTextView.text = "Hello, User!"
        }
    }

    private fun setupSearch() {
        val searchButton: ImageView = findViewById(R.id.btnSearch)

        // Handle search button click
        searchButton.setOnClickListener {
            performSearch(searchEditText.text.toString().trim())
        }

        // Handle live search as user types
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                performSearch(s.toString().trim())
            }
        })
    }

    private fun performSearch(query: String) {
        // If search field is empty, show all FAQ items
        if (query.isEmpty()) {
            faqCardViews.forEach { it.visibility = View.VISIBLE }
            return
        }

        // Filter FAQs based on search query
        faqCardViews.forEach { cardView ->
            // Get the question TextView (first child) and answer TextView (second child)
            val questionTextView = (cardView.getChildAt(0) as? TextView)
            val answerTextView = (cardView.getChildAt(1) as? TextView)

            // Get text content
            val questionText = questionTextView?.text.toString().lowercase()
            val answerText = answerTextView?.text.toString().lowercase()
            val searchQuery = query.lowercase()

            // Show card if query matches either question or answer
            if (questionText.contains(searchQuery) || answerText.contains(searchQuery)) {
                cardView.visibility = View.VISIBLE
            } else {
                cardView.visibility = View.GONE
            }
        }
    }
}