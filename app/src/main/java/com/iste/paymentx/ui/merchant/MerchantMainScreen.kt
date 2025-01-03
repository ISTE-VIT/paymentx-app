package com.iste.paymentx.ui.merchant

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.iste.paymentx.R
import com.iste.paymentx.ui.main.TopUp
import com.iste.paymentx.ui.main.Withdraw

class MerchantMainScreen : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var balanceTextView: TextView
    private lateinit var btnViewBalance: Button
    private lateinit var btnReceive: ImageView
    private lateinit var btnWithdraw: ImageView

    // Variables to store the received data
    private var userName: String? = null
    private var userEmail: String? = null
    private var userId: String? = null
    private var shopName: String? = null
    private var shopkeeperName: String? = null
    private var shopLocation: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_merchant_main_screen)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Set navigation bar color programmatically for additional compatibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.navigationBarColor = ContextCompat.getColor(this, R.color.navigation_bar_color)
            window.statusBarColor = Color.parseColor("#C5D9C9")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("ShopPrefs", MODE_PRIVATE)

        // Get all information from intent
        userName = intent.getStringExtra("USER_NAME")
        userEmail = intent.getStringExtra("USER_EMAIL")
        userId = intent.getStringExtra("USER_ID")
        shopName = intent.getStringExtra("SHOP_NAME") ?: sharedPreferences.getString("SHOP_NAME", null)
        shopkeeperName = intent.getStringExtra("SHOPKEEPER_NAME")
        shopLocation = intent.getStringExtra("SHOP_LOCATION")

        // Save userName in SharedPreferences if retrieved from intent
        if (shopName != null) {
            sharedPreferences.edit().putString("SHOP_NAME", shopName).apply()
        }

        // Initialize views
        balanceTextView = findViewById(R.id.merchtextView2)
        btnViewBalance = findViewById(R.id.merchbtnViewBalance)
        btnReceive = findViewById(R.id.merchbtnTopUp)
        btnWithdraw = findViewById(R.id.merchbtnWithdraw)
        balanceTextView.visibility = View.GONE
        btnViewBalance.visibility = View.VISIBLE

        // Add this line to set only the first name
        findViewById<TextView>(R.id.merchtextView).text = "Hi, $shopName"

        // Navigate to TopUp screen when TopUp button is clicked
        btnReceive.setOnClickListener {
            val intent = Intent(this, Receive::class.java)
            startActivity(intent)
        }

        // Navigate to Withdraw screen when Withdraw button is clicked
        btnWithdraw.setOnClickListener {
            val intent = Intent(this, MerchantWithdraw::class.java)
            startActivity(intent)
        }
    }
}