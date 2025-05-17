package com.iste.paymentX.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.iste.paymentX.R

class DeveloperTeam : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_developer_team)

        val back = findViewById<ImageView>(R.id.previouspage)
        back.setOnClickListener {
            val intent = Intent(this, UserProfile::class.java)
            startActivity(intent)
        }

        // Initialize UI elements
        setupSocialLinks()

    }

    private fun setupSocialLinks() {
        // Find all buttons
        val btnLinkedinPratham = findViewById<CardView>(R.id.btn_linkedin_pratham)
        val btnGithubPratham = findViewById<CardView>(R.id.btn_github_pratham)
        val btnLinkedinRudra = findViewById<CardView>(R.id.btn_linkedin_rudra)
        val btnGithubRudra = findViewById<CardView>(R.id.btn_github_rudra)
        val btnLinkedinSaniya = findViewById<CardView>(R.id.btn_linkedin_saniya)
        val btnGithubSaniya = findViewById<CardView>(R.id.btn_github_saniya)



        // Define the social profile URLs
        // Replace these URLs with the actual profile URLs
        val linkedinPratham = "https://www.linkedin.com/in/pratham-khanduja/"
        val githubPratham = "https://github.com/pratham-developer"
        val linkedinRudra = "https://www.linkedin.com/in/rudra-gupta-36827828b/"
        val githubRudra = "https://github.com/Rudragupta8777"
        val linkedinSaniya = "https://www.linkedin.com/in/saniya7goyal/"
        val githubSaniya = "https://github.com/san7iya"

        // Set click listeners for Pratham's social links
        btnLinkedinPratham.setOnClickListener {
            animateButtonClick(it)
            openUrl(linkedinPratham)
        }

        btnGithubPratham.setOnClickListener {
            animateButtonClick(it)
            openUrl(githubPratham)
        }

        // Set click listeners for Rudra's social links
        btnLinkedinRudra.setOnClickListener {
            animateButtonClick(it)
            openUrl(linkedinRudra)
        }

        btnGithubRudra.setOnClickListener {
            animateButtonClick(it)
            openUrl(githubRudra)
        }

        // Set click listeners for Saniya's social links
        btnLinkedinSaniya.setOnClickListener {
            animateButtonClick(it)
            openUrl(linkedinSaniya)
        }

        btnGithubSaniya.setOnClickListener {
            animateButtonClick(it)
            openUrl(githubSaniya)
        }
    }

    // Helper method to open URLs
    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    // Helper method to add click animation
    private fun animateButtonClick(view: android.view.View) {
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }
}