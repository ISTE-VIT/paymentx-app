package com.iste.paymentx.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.iste.paymentx.R

class AccountCreatedSuccessfully : AppCompatActivity() {
    private lateinit var continueButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_account_created_successfully)

        val continueButton = findViewById<Button>(R.id.Continue_button)

        continueButton.setOnClickListener{
            val intent = Intent(this, MainScreen::class.java)
            startActivity(intent)
        }
    }
}