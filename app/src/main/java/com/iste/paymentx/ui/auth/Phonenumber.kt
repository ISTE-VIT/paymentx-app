package com.iste.paymentx.ui.auth

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.iste.paymentx.R

class Phonenumber : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_phonenumber)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Find the Confirm button by its ID
        val confirmButton = findViewById<Button>(R.id.confirmButton)
        val textField = findViewById<EditText>(R.id.phoneNumberEditText)
        val number = textField.text.toString()
        // Set an OnClickListener on the Confirm button
        confirmButton.setOnClickListener {
            // Start the OtpVerification activity
            val number = textField.text.toString()
            val intent = Intent(this, OtpVerification::class.java)
            intent.putExtra("phoneNumber",number)
            startActivity(intent)
        }
    }
}
