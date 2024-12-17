package com.iste.paymentx.ui.auth

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.iste.paymentx.R
import com.iste.paymentx.ui.main.TickMarkAnimation

class Display : AppCompatActivity() {

    private lateinit var uidTextView: TextView
    private lateinit var confirmButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_display)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        try {
            // Initialize TextView and Button
            uidTextView = findViewById(R.id.display_UID)
            confirmButton = findViewById(R.id.Confirm_button)

            // Get the UID from the intent
            val uid = intent.getStringExtra("CARD_UID")

            if (uid != null) {
                uidTextView.text = "Card UID: $uid"
                Log.d("DisplayUID", "Successfully displayed UID: $uid")
            } else {
                uidTextView.text = "No UID received"
                Log.e("DisplayUID", "No UID received in intent")
                Toast.makeText(this, "Error: No UID received", Toast.LENGTH_SHORT).show()
            }

            // Set OnClickListener for the Confirm button
            confirmButton.setOnClickListener {
                val phoneIntent = Intent(this, Phonenumber::class.java)
                startActivity(phoneIntent)
            }

        } catch (e: Exception) {
            Log.e("DisplayUID", "Error in onCreate: ${e.message}")
            Toast.makeText(this, "Error displaying UID", Toast.LENGTH_SHORT).show()
        }
    }
}