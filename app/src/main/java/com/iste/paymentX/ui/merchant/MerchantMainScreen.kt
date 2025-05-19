package com.iste.paymentX.ui.merchant

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
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.iste.paymentX.R
import com.iste.paymentX.data.model.RetrofitInstance
import com.iste.paymentX.data.model.Transaction
import com.iste.paymentX.ui.auth.GoogleAuthActivity
import com.iste.paymentX.ui.main.UserProfile
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import java.io.IOException

class MerchantMainScreen : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var balanceTextView: TextView
    private lateinit var btnViewBalance: Button
    private lateinit var btnReceive: ImageView
    private lateinit var btnWithdraw: ImageView
    private lateinit var btnTransact: ImageView  // Added declaration for transaction button
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionAdapter: MerchTransactionAdapter
    private val transactionList: MutableList<Transaction> = mutableListOf()
    private lateinit var greetingTextView: TextView

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
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        auth = FirebaseAuth.getInstance()
        googleSignInClient = GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )

        // Add click listener for transaction button
        btnTransact = findViewById(R.id.merchbtnTransact)
        btnTransact.setOnClickListener {
            val intent = Intent(this, MerchantTransactions::class.java)
            startActivity(intent)
        }

        // Set navigation bar color programmatically for additional compatibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.navigationBarColor = ContextCompat.getColor(this, R.color.navigation_bar_color)
            window.statusBarColor = ContextCompat.getColor(this, R.color.header)
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

        recyclerView = findViewById(R.id.merchrecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        transactionAdapter = MerchTransactionAdapter(transactionList)
        recyclerView.adapter = transactionAdapter

        findViewById<ImageView>(R.id.merchbtnProfile).setOnClickListener {
            val intent = Intent(this, MerchantProfile::class.java)
            // Pass user data to UserProfile activity
            intent.putExtra("USER_NAME", userName)
            intent.putExtra("USER_EMAIL", userEmail)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
            // Don't call finish() here to keep MainScreen in the back stack
        }

        // Save shopName in SharedPreferences if retrieved from intent
        if (shopName != null) {
            sharedPreferences.edit().putString("SHOP_NAME", shopName).apply()
        }

        // Initialize views
        balanceTextView = findViewById(R.id.merchtextView2)
        btnViewBalance = findViewById(R.id.merchbtnViewBalance)
        btnReceive = findViewById(R.id.merchbtnRecieve)
        btnWithdraw = findViewById(R.id.merchbtnWithdraw)
        greetingTextView = findViewById(R.id.merchtextView)
        balanceTextView.visibility = View.GONE
        btnViewBalance.visibility = View.VISIBLE

        // Set default greeting while fetching from backend
        greetingTextView.text = "Hi, Shop"

        // Set click listener for balance visibility
        btnViewBalance.setOnClickListener {
            btnViewBalance.visibility = View.GONE
            val intent = Intent(this, MerchantPINVerifyPage::class.java)
            intent.putExtra("CALLING_ACTIVITY", "MerchantViewBalance")
            startActivityForResult(intent, 100) // Start for result to check PIN verification
        }

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

        // Add click listener for "See All" text to navigate to transactions screen
        findViewById<TextView>(R.id.merchtextView4).setOnClickListener {
            val intent = Intent(this, MerchantTransactions::class.java)
            startActivity(intent)
        }

        // Fetch merchant details from backend (including shop name)
        fetchMerchantDetails()

        fetchTrans()
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
                Log.e("MerchantMainScreen", "Response not successful: ${response.code()} - ${response.message()}")
            }
        } catch (e: IOException) {
            btnViewBalance.visibility = View.VISIBLE
            balanceTextView.visibility = View.GONE
            Log.e("MerchantMainScreen", "IOException, you might not have internet connection", e)
        } catch (e: HttpException) {
            btnViewBalance.visibility = View.VISIBLE
            balanceTextView.visibility = View.GONE
            Log.e("MerchantMainScreen", "HttpException, unexpected response", e)
        }
    }

    private fun getBalance() {
        lifecycleScope.launch {
            val token = getFirebaseIdToken()
            if (token != null) {
                getBalanceHelper("Bearer $token")
            } else {
                Log.e("MerchantMainScreen", "Failed to get Firebase ID token")
            }
        }
    }

    private suspend fun getFirebaseIdToken(): String? {
        return try {
            auth.currentUser?.getIdToken(false)?.await()?.token
        } catch (e: Exception) {
            Log.e("MerchantMainScreen", "Error getting Firebase ID token", e)
            null
        }
    }

    private fun handleLogout() {
        try {
            auth.signOut()
            googleSignInClient.revokeAccess().addOnCompleteListener {
                startActivity(Intent(this, GoogleAuthActivity::class.java))
                finish()
            }
        } catch (e: Exception) {
            Log.e("MerchMainScreen", "Exception during logout: ", e)
        }
    }

    private suspend fun fetchTransHelper(authToken: String) {
        try {
            val response = RetrofitInstance.api.getMerchTrans(authToken)
            if (response.isSuccessful && response.body() != null) {
                response.body()?.let { transactions ->
                    transactionList.clear()
                    transactionList.addAll(transactions)
                    transactionAdapter.notifyDataSetChanged()
                }
            } else {
                Log.e("MerchantMainScreen", "Response not successful: ${response.code()} - ${response.message()}")
            }
        } catch (e: IOException) {
            Log.e("MerchantMainScreen", "IOException, you might not have internet connection", e)

        } catch (e: HttpException) {
            Log.e("MerchantMainScreen", "HttpException, unexpected response", e)

        }
    }

    private fun fetchTrans() {
        lifecycleScope.launch {
            getFirebaseIdToken()?.let { token ->
                fetchTransHelper("Bearer $token")
            } ?: Log.e("MerchantMainScreen", "Failed to get Firebase ID token")
        }
    }

    // New method to fetch merchant details from backend
    private fun fetchMerchantDetails() {
        lifecycleScope.launch {
            try {
                val token = getFirebaseIdToken()
                if (token != null) {
                    // You might need to replace this with the appropriate API call for merchant details
                    // For now, I'm using checkUser API - you might have a different endpoint for merchants
                    val response = RetrofitInstance.api.checkUser("Bearer $token")

                    if (response.isSuccessful && response.body() != null) {
                        val userData = response.body()?.user

                        // Update UI with merchant data from backend
                        userData?.let { user ->
                            // For merchants, you might have a shopName field in your API response
                            // If you have a separate merchant API endpoint, use that instead

                            // Assuming the shop name is stored in displayName or a similar field
                            // You might need to adjust this based on your actual API response structure
                            user.displayName?.let { displayName ->
                                if (displayName.isNotEmpty()) {
                                    // For shops, you might want to use the full shop name
                                    // or just the first word (like "Fresh Mart" -> "Fresh")
                                    val shopNameToDisplay = displayName.split(" ").first()
                                    greetingTextView.text = "Hi, $shopNameToDisplay"

                                    // Save the full shop name
                                    shopName = displayName
                                    sharedPreferences.edit().putString("SHOP_NAME", displayName).apply()
                                }
                            }

                            // Alternative: If you have a separate field for shop name in your API
                            // Replace this with the actual field name from your API response
                            // user.shopName?.let { backendShopName ->
                            //     if (backendShopName.isNotEmpty()) {
                            //         val shopNameToDisplay = backendShopName.split(" ").first()
                            //         greetingTextView.text = "Hi, $shopNameToDisplay"
                            //         shopName = backendShopName
                            //         sharedPreferences.edit().putString("SHOP_NAME", backendShopName).apply()
                            //     }
                            // }
                        }
                    } else {
                        Log.e("MerchantMainScreen", "Failed to fetch merchant details: ${response.code()}")
                        // Fallback to using existing shopName
                        setFallbackGreeting()
                    }
                } else {
                    Log.e("MerchantMainScreen", "Failed to get Firebase ID token")
                    // Fallback to using existing shopName
                    setFallbackGreeting()
                }
            } catch (e: IOException) {
                Log.e("MerchantMainScreen", "Network error while fetching merchant details", e)
                setFallbackGreeting()
            } catch (e: HttpException) {
                Log.e("MerchantMainScreen", "HTTP error while fetching merchant details", e)
                setFallbackGreeting()
            } catch (e: Exception) {
                Log.e("MerchantMainScreen", "Unexpected error while fetching merchant details", e)
                setFallbackGreeting()
            }
        }
    }

    // Fallback method to set greeting from existing sources
    private fun setFallbackGreeting() {
        // Try to use the shop name from intent/SharedPreferences
        val fallbackShopName = shopName

        if (!fallbackShopName.isNullOrEmpty()) {
            val shopNameToDisplay = fallbackShopName.split(" ").first()
            greetingTextView.text = "Hi, $shopNameToDisplay"
        } else {
            // Last resort - use default
            greetingTextView.text = "Hi, Shop"
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh merchant data when returning to main screen
        fetchMerchantDetails()
    }
}