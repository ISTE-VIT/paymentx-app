package com.iste.paymentX.ui.main

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.iste.paymentX.R

class AccountCreatedSuccessfully : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_account_created_successfully)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val continueButton = findViewById<Button>(R.id.Continue_button)

        continueButton.setOnClickListener {
            val intent = Intent(this, MainScreen::class.java)
            // Pass any necessary data to MainScreen if required
            startActivity(intent)
            finish() // Close the current activity to avoid returning to it
        }
    }
}