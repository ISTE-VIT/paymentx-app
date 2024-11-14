package com.iste.paymentx.ui.auth

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.iste.paymentx.R

class Display : AppCompatActivity() {

    private lateinit var uidTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_display)
        try {
            // Initialize TextView
            uidTextView = findViewById(R.id.display_UID)

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

        } catch (e: Exception) {
            Log.e("DisplayUID", "Error in onCreate: ${e.message}")
            Toast.makeText(this, "Error displaying UID", Toast.LENGTH_SHORT).show()
        }

    }
}