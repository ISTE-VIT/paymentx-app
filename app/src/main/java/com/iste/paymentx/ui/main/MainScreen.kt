package com.iste.paymentx.ui.main

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.iste.paymentx.R
import com.iste.paymentx.data.model.RetrofitInstance
import com.iste.paymentx.ui.auth.PinVerifyPage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import java.io.IOException

class MainScreen : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var balanceTextView: TextView
    private lateinit var btnViewBalance: Button
    private lateinit var btnTopUp: ImageView
    private lateinit var btnWithdraw: ImageView
    private lateinit var auth: FirebaseAuth

    // Store credentials
    private var userName: String? = null
    private var userEmail: String? = null
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        auth = FirebaseAuth.getInstance()
        setContentView(R.layout.activity_main_screen)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        // Get the user information from intent or SharedPreferences
        userName = intent.getStringExtra("USER_NAME") ?: sharedPreferences.getString("USER_NAME", null)
        userEmail = intent.getStringExtra("USER_EMAIL")
        userId = intent.getStringExtra("USER_ID")

        // Save userName in SharedPreferences if retrieved from intent
        if (userName != null) {
            sharedPreferences.edit().putString("USER_NAME", userName).apply()
        }

        // Set navigation bar color programmatically for additional compatibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.navigationBarColor = ContextCompat.getColor(this, R.color.navigation_bar_color)
            window.statusBarColor = Color.parseColor("#C5D9C9")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }

        // Initialize views
        balanceTextView = findViewById(R.id.textView2)
        btnViewBalance = findViewById(R.id.btnViewBalance)
        btnTopUp = findViewById(R.id.btnTopUp)
        btnWithdraw = findViewById(R.id.btnWithdraw)
        balanceTextView.visibility = View.GONE
        btnViewBalance.visibility = View.VISIBLE

        // Add this line to set only the first name
        val firstName = userName?.split(" ")?.first() ?: "User"
        findViewById<TextView>(R.id.textView).text = "Hi, $firstName"

        // Set click listener for balance visibility
        btnViewBalance.setOnClickListener {
            btnViewBalance.visibility = View.GONE
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
        btnViewBalance.visibility = View.VISIBLE
        if (requestCode == 100 && resultCode == RESULT_OK) {
            showBalance() // Show the balance if PIN was verified
        }
    }

    private fun showBalance() {
        btnViewBalance.visibility = View.GONE
        getBalance()
    }

    private suspend fun getFirebaseIdToken(): String? {
        return try {
            val user = auth.currentUser
            user?.getIdToken(false)?.await()?.token
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error getting Firebase ID token", e)
            null
        }
    }

    private suspend fun getBalanceHelper(authToken: String) {
        try {
            val response = RetrofitInstance.api.getWallet(authToken)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()
                val wallet = body?.wallet
                if (wallet != null) {
                    val amount = wallet.balance
                    balanceTextView.text = "₹" + Integer.toString(amount)
                    balanceTextView.visibility = View.VISIBLE
                }
            } else {
                btnViewBalance.visibility = View.VISIBLE
                balanceTextView.visibility = View.GONE
                Log.e("HomeActivity", "Response not successful: ${response.code()} - ${response.message()}")
            }
        } catch (e: IOException) {
            btnViewBalance.visibility = View.VISIBLE
            balanceTextView.visibility = View.GONE
            Log.e("HomeActivity", "IOException, you might not have internet connection", e)
        } catch (e: HttpException) {
            btnViewBalance.visibility = View.VISIBLE
            balanceTextView.visibility = View.GONE
            Log.e("HomeActivity", "HttpException, unexpected response", e)
        }
    }

    private fun getBalance() {
        lifecycleScope.launch {
            val token = getFirebaseIdToken()
            if (token != null) {
                getBalanceHelper("Bearer $token")
            } else {
                Log.e("HomeActivity", "Failed to get Firebase ID token")
            }
        }
    }
}