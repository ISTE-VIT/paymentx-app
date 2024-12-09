package com.iste.paymentx.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.iste.paymentx.R
import com.iste.paymentx.ui.auth.PinVerifyPage

class MainScreen : AppCompatActivity() {
    private lateinit var balanceTextView: TextView
    private lateinit var btnViewBalance: Button
    private lateinit var btnTopUp: ImageView
    private lateinit var btnWithdraw: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_screen)

        // Initialize views
        balanceTextView = findViewById(R.id.textView2)
        btnViewBalance = findViewById(R.id.btnViewBalance)
        btnTopUp = findViewById(R.id.btnTopUp)
        btnWithdraw = findViewById(R.id.btnWithdraw)

        // Set click listener for balance visibility
        btnViewBalance.setOnClickListener {
            val intent = Intent(this, PinVerifyPage::class.java)
            intent.putExtra("CALLING_ACTIVITY", "ViewBalance")
            startActivityForResult(intent, 100) // Start for result to check PIN verification
        }

        // Navigate to TopUp screen when TopUp button is clicked
        btnTopUp.setOnClickListener {
            val intent = Intent(this, TopUp::class.java)
            startActivity(intent)
        }

        // Navigate to Withdraw screen when Withdraw button is clicked
        btnWithdraw.setOnClickListener {
            val intent = Intent(this, Withdraw::class.java)
            startActivity(intent)
        }
    }

    // Handle the result from PinVerifyPage
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            showBalance() // Show the balance if PIN was verified
        }
    }

    private fun showBalance() {
        // Hide the button
        btnViewBalance.visibility = View.GONE

        // Show the balance TextView
        balanceTextView.visibility = View.VISIBLE
    }
}
