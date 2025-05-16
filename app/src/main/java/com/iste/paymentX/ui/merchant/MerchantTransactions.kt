package com.iste.paymentX.ui.merchant

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.iste.paymentX.R
import com.iste.paymentX.data.model.RetrofitInstance
import com.iste.paymentX.data.model.Transaction
import com.iste.paymentX.ui.auth.GoogleAuthActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class MerchantTransactions : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionAdapter: MerchTransactionAdapter
    private lateinit var btnHome: ImageView
    private lateinit var btnProfile: ImageView
    private lateinit var btnTransact: ImageView
    private lateinit var searchEditText: EditText
    private lateinit var btnStatusFilter: MaterialButton
    private lateinit var btnDateFilter: MaterialButton
    private lateinit var btnAmountFilter: MaterialButton
    private lateinit var auth: FirebaseAuth

    private val transactionList: MutableList<Transaction> = mutableListOf()
    private val originalTransactionList: MutableList<Transaction> = mutableListOf()

    private var isStatusAscending = true
    private var isDateAscending = true
    private var isAmountAscending = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_merchant_transactions)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Set system UI colors
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.navigationBarColor = ContextCompat.getColor(this, R.color.navigation_bar_color)
            window.statusBarColor = ContextCompat.getColor(this, R.color.header)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }

        // Initialize views
        initializeViews()

        // Set up listeners
        setupListeners()

        // Set up navigation
        setupNavigation()

        // Fetch transactions
        fetchTransactions()
    }

    private fun initializeViews() {
        // Initialize RecyclerView
        recyclerView = findViewById(R.id.merchrecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        transactionAdapter = MerchTransactionAdapter(transactionList)
        recyclerView.adapter = transactionAdapter

        // Initialize navigation buttons
        btnHome = findViewById(R.id.merchbtnHome)
        btnProfile = findViewById(R.id.merchbtnProfile)
        btnTransact = findViewById(R.id.merchbtnTransact)

        // Initialize search and filter
        searchEditText = findViewById(R.id.searchEditText)
        btnStatusFilter = findViewById(R.id.btnStatusFilter)
        btnDateFilter = findViewById(R.id.btnDateFilter)
        btnAmountFilter = findViewById(R.id.btnAmountFilter)
    }

    private fun setupListeners() {
        // Search functionality
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterTransactions(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Filter buttons
        btnStatusFilter.setOnClickListener {
            isStatusAscending = !isStatusAscending
            sortByStatus(isStatusAscending)
        }

        btnDateFilter.setOnClickListener {
            isDateAscending = !isDateAscending
            sortByDate(isDateAscending)
        }

        btnAmountFilter.setOnClickListener {
            isAmountAscending = !isAmountAscending
            sortByAmount(isAmountAscending)
        }
    }

    private fun setupNavigation() {
        btnHome.setOnClickListener {
            val intent = Intent(this, MerchantMainScreen::class.java)
            startActivity(intent)
            finish()
        }

        btnProfile.setOnClickListener {
            handleLogout()
        }

        // Transaction button is already selected on this screen
        btnTransact.isSelected = true
    }

    private fun fetchTransactions() {
        lifecycleScope.launch {
            getFirebaseIdToken()?.let { token ->
                fetchTransactionsHelper("Bearer $token")
            } ?: Log.e("MerchantTransactions", "Failed to get Firebase ID token")
        }
    }

    private suspend fun fetchTransactionsHelper(authToken: String) {
        try {
            val response = RetrofitInstance.api.getMerchTrans(authToken)
            if (response.isSuccessful && response.body() != null) {
                response.body()?.let { transactions ->
                    transactionList.clear()
                    originalTransactionList.clear()

                    transactionList.addAll(transactions)
                    originalTransactionList.addAll(transactions)

                    transactionAdapter.notifyDataSetChanged()
                }
            } else {
                Log.e("MerchantTransactions", "Response not successful: ${response.code()} - ${response.message()}")
            }
        } catch (e: IOException) {
            Log.e("MerchantTransactions", "IOException, you might not have internet connection", e)
        } catch (e: HttpException) {
            Log.e("MerchantTransactions", "HttpException, unexpected response", e)
        }
    }

    private suspend fun getFirebaseIdToken(): String? {
        return try {
            auth.currentUser?.getIdToken(false)?.await()?.token
        } catch (e: Exception) {
            Log.e("MerchantTransactions", "Error getting Firebase ID token", e)
            null
        }
    }

    private fun handleLogout() {
        try {
            auth.signOut()
            val intent = Intent(this, GoogleAuthActivity::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e("MerchantTransactions", "Exception during logout: ", e)
        }
    }

    private fun filterTransactions(query: String) {
        val filteredList = if (query.isEmpty()) {
            // If search is empty, show all transactions
            originalTransactionList
        } else {
            // Filter by username, amount, status, etc.
            originalTransactionList.filter { transaction ->
                transaction.userName.contains(query, ignoreCase = true) ||
                        transaction.amount.toString().contains(query) ||
                        transaction.status.contains(query, ignoreCase = true)
            }
        }

        transactionList.clear()
        transactionList.addAll(filteredList)
        transactionAdapter.notifyDataSetChanged()
    }

    private fun sortByStatus(ascending: Boolean) {
        val sortedList = if (ascending) {
            transactionList.sortedBy { it.status }
        } else {
            transactionList.sortedByDescending { it.status }
        }

        transactionList.clear()
        transactionList.addAll(sortedList)
        transactionAdapter.notifyDataSetChanged()
    }

    private fun sortByDate(ascending: Boolean) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())

        val sortedList = if (ascending) {
            transactionList.sortedBy {
                try {
                    dateFormat.parse(it.timestamp)?.time ?: 0
                } catch (e: Exception) {
                    0
                }
            }
        } else {
            transactionList.sortedByDescending {
                try {
                    dateFormat.parse(it.timestamp)?.time ?: 0
                } catch (e: Exception) {
                    0
                }
            }
        }

        transactionList.clear()
        transactionList.addAll(sortedList)
        transactionAdapter.notifyDataSetChanged()
    }

    private fun sortByAmount(ascending: Boolean) {
        val sortedList = if (ascending) {
            transactionList.sortedBy { it.amount }
        } else {
            transactionList.sortedByDescending { it.amount }
        }

        transactionList.clear()
        transactionList.addAll(sortedList)
        transactionAdapter.notifyDataSetChanged()
    }
}