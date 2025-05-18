package com.iste.paymentX.ui.main

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
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
import com.iste.paymentX.ui.auth.PinVerifyPage
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
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var btnTransact: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionAdapter: UserTransactionAdapter
    private val transactionList: MutableList<Transaction> = mutableListOf()

    // Store credentials
    private var userName: String? = null
    private var userEmail: String? = null
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_screen)
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

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        // Get the user information from intent or SharedPreferences
        userName = intent.getStringExtra("USER_NAME") ?: sharedPreferences.getString("USER_NAME", null)
        userEmail = intent.getStringExtra("USER_EMAIL")
        userId = intent.getStringExtra("USER_ID")

        findViewById<ImageView>(R.id.btnProfile).setOnClickListener {
            val intent = Intent(this, UserProfile::class.java)
            // Pass user data to UserProfile activity
            intent.putExtra("USER_NAME", userName)
            intent.putExtra("USER_EMAIL", userEmail)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
            // Don't call finish() here to keep MainScreen in the back stack
        }

        // Save userName in SharedPreferences if retrieved from intent
        if (userName != null) {
            sharedPreferences.edit().putString("USER_NAME", userName).apply()
        }

        // Set navigation bar color programmatically for additional compatibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.navigationBarColor = ContextCompat.getColor(this, R.color.navigation_bar_color)
            window.statusBarColor = ContextCompat.getColor(this, R.color.header)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }

        // Initialize views
        balanceTextView = findViewById(R.id.textView2)
        btnViewBalance = findViewById(R.id.btnViewBalance)
        btnTopUp = findViewById(R.id.btnTopUp)
        btnWithdraw = findViewById(R.id.btnWithdraw)
        btnTransact = findViewById(R.id.btnTransact)
        balanceTextView.visibility = View.GONE
        btnViewBalance.visibility = View.VISIBLE

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        transactionAdapter = UserTransactionAdapter(transactionList)
        recyclerView.adapter = transactionAdapter

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

        // Fix for Transaction button - proper click handler
        btnTransact.setOnClickListener {
            try {
                val intent = Intent(this, Transactions::class.java)
                startActivity(intent)
                // Don't call finish() here as we want to keep MainScreen in the back stack
            } catch (e: Exception) {
                Log.e("MainScreen", "Error navigating to Transactions: ", e)
            }
        }

        // Navigate to Withdraw screen when Withdraw button is clicked
        btnWithdraw.setOnClickListener {
            val intent = Intent(this, Withdraw::class.java)
            startActivity(intent)
        }

        // Add click listener for "See All" text to navigate to transactions screen
        findViewById<TextView>(R.id.textView4).setOnClickListener {
            val intent = Intent(this, Transactions::class.java)
            startActivity(intent)
        }

        // Fetch recent transactions
        fetchTransactions()
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
            Log.e("MainScreen", "Error getting Firebase ID token", e)
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
                Log.e("MainScreen", "Response not successful: ${response.code()} - ${response.message()}")
            }
        } catch (e: IOException) {
            btnViewBalance.visibility = View.VISIBLE
            balanceTextView.visibility = View.GONE
            Log.e("MainScreen", "IOException, you might not have internet connection", e)
        } catch (e: HttpException) {
            btnViewBalance.visibility = View.VISIBLE
            balanceTextView.visibility = View.GONE
            Log.e("MainScreen", "HttpException, unexpected response", e)
        }
    }

    private fun getBalance() {
        lifecycleScope.launch {
            val token = getFirebaseIdToken()
            if (token != null) {
                getBalanceHelper("Bearer $token")
            } else {
                Log.e("MainScreen", "Failed to get Firebase ID token")
            }
        }
    }

    private fun fetchTransactions() {
        lifecycleScope.launch {
            getFirebaseIdToken()?.let { token ->
                fetchTransactionsHelper("Bearer $token")
            } ?: Log.e("MainScreen", "Failed to get Firebase ID token")
        }
    }

    private suspend fun fetchTransactionsHelper(authToken: String) {
        try {
            val response = RetrofitInstance.api.getUserTrans(authToken)
            if (response.isSuccessful && response.body() != null) {
                response.body()?.let { transactions ->
                    transactionList.clear()
                    // Add recent transactions (limit to 5 for main screen)
                    transactionList.addAll(transactions.take(5))
                    transactionAdapter.notifyDataSetChanged()
                }
            } else {
                Log.e("MainScreen", "Response not successful: ${response.code()} - ${response.message()}")
            }
        } catch (e: IOException) {
            Log.e("MainScreen", "IOException, you might not have internet connection", e)
        } catch (e: HttpException) {
            Log.e("MainScreen", "HttpException, unexpected response", e)
        }
    }
}