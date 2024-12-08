package com.iste.paymentx.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.iste.paymentx.R

class Phonenumber : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_phonenumber)

        // Find the Confirm button by its ID
        val confirmButton = findViewById<Button>(R.id.confirmButton)

        // Set an OnClickListener on the Confirm button
        confirmButton.setOnClickListener {
            // Start the OtpVerification activity
            val intent = Intent(this, OtpVerification::class.java)
            startActivity(intent)
        }
    }
}
