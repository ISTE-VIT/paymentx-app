package com.iste.paymentX.ui.merchant

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.button.MaterialButton
import com.iste.paymentX.R
import java.util.Calendar
import java.util.Date

class ReceiveCompleted : AppCompatActivity() {
    private lateinit var backArrow: ImageView
    private lateinit var amountTextView: TextView
    private lateinit var transactionDateTextView: TextView
    private lateinit var customerNameTextView: TextView
    private lateinit var transactionIdTextView: TextView
    private lateinit var downloadButton: LinearLayout
    private lateinit var shareButton: LinearLayout
    private lateinit var homeButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_receive_completed)

        // Initialize views
        backArrow = findViewById(R.id.back)
        amountTextView = findViewById(R.id.tvAmount)
        transactionDateTextView = findViewById(R.id.tvTransactionDate)
        customerNameTextView = findViewById(R.id.tvCustomerValue)
        transactionIdTextView = findViewById(R.id.tvTransactionIdValue)
        downloadButton = findViewById(R.id.btnDownload)
        shareButton = findViewById(R.id.btnShare)
        homeButton = findViewById(R.id.btnHome)

        // Get data from intent
        val amount = intent.getIntExtra("EXTRA_AMOUNT", 0)
        val customerName = intent.getStringExtra("CUSTOMER_NAME") ?: "CUSTOMER"
        val transactionId = intent.getStringExtra("TRANSACTION_ID") ?: generateTransactionId()

        Log.d("ReceiveCompleted", "Received amount: $amount, customer: $customerName")

        // Set current date and time
        val currentDate = Date()
        val dateFormat = DateFormat.format("dd MMMM, yyyy 'at' hh:mm a", currentDate)
        transactionDateTextView.text = dateFormat.toString()

        // Format and display amount
        if (amount > 0) {
            val formattedAmount = "₹${String.format("%,.2f", amount.toFloat())}"
            amountTextView.text = formattedAmount
        } else {
            Log.e("ReceiveCompleted", "Amount is 0 or not received properly")
            amountTextView.text = "₹0.00"
        }

        // Set customer name and transaction ID
        customerNameTextView.text = customerName.uppercase()
        transactionIdTextView.text = transactionId

        // Set click listeners
        backArrow.setOnClickListener {
            navigateToMainScreen()
        }

        homeButton.setOnClickListener {
            navigateToMainScreen()
        }

        downloadButton.setOnClickListener {
            // Implementation for downloading receipt
            // This could save the receipt as PDF or image
            Log.d("ReceiveCompleted", "Download receipt clicked")
            // Show a toast or feedback to user
        }

        shareButton.setOnClickListener {
            // Implementation for sharing receipt
            // This could use Android's share intent
            val shareText = "Transaction completed! Amount: ${amountTextView.text}, " +
                    "Transaction ID: $transactionId, Date: ${transactionDateTextView.text}"

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        }

        // Handle system back button
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToMainScreen()
            }
        })
    }

    private fun navigateToMainScreen() {
        val intent = Intent(this, MerchantMainScreen::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun generateTransactionId(): String {
        // Generate a pseudo-random transaction ID
        val alphanumeric = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val calendar = Calendar.getInstance()
        val prefix = calendar.get(Calendar.YEAR).toString().substring(2) +
                String.format("%02d", calendar.get(Calendar.MONTH) + 1)

        return prefix + (1..8).map { alphanumeric.random() }.joinToString("")
    }
}