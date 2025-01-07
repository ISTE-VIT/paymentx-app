package com.iste.paymentx.ui.merchant

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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.iste.paymentx.R
import com.iste.paymentx.data.model.RetrofitInstance
import com.iste.paymentx.data.model.Transaction
import com.iste.paymentx.ui.auth.GoogleAuthActivity
import com.iste.paymentx.ui.main.TopUp
import com.iste.paymentx.ui.main.Withdraw
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
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionAdapter: MerchTransactionAdapter
    private val transactionList: MutableList<Transaction> = mutableListOf()

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

        auth = FirebaseAuth.getInstance()
        googleSignInClient = GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )

        findViewById<ImageView>(R.id.merchbtnProfile).setOnClickListener(){
            handleLogout()
        }


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

        recyclerView = findViewById(R.id.merchrecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        transactionAdapter = MerchTransactionAdapter(transactionList)
        recyclerView.adapter = transactionAdapter

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

        fetchTrans()
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

    private suspend fun getFirebaseIdToken(): String? {
        return try {
            auth.currentUser?.getIdToken(false)?.await()?.token
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error getting Firebase ID token", e)
            null
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
                Log.e("HomeActivity", "Response not successful: ${response.code()} - ${response.message()}")
            }
        } catch (e: IOException) {
            Log.e("HomeActivity", "IOException, you might not have internet connection", e)

        } catch (e: HttpException) {
            Log.e("HomeActivity", "HttpException, unexpected response", e)

        }
    }

    private fun fetchTrans() {
        lifecycleScope.launch {
            getFirebaseIdToken()?.let { token ->
                fetchTransHelper("Bearer $token")
            } ?: Log.e("HomeActivity", "Failed to get Firebase ID token")
        }
    }
}