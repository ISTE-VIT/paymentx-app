package com.iste.paymentX.ui.merchant

import android.content.Intent
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
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.iste.paymentX.R
import com.iste.paymentX.data.model.RetrofitInstance
import com.iste.paymentX.ui.auth.GoogleAuthActivity
import com.iste.paymentX.ui.main.ContactUs
import com.iste.paymentX.ui.main.DeveloperTeam
import com.iste.paymentX.ui.main.FeedbackForm
import com.iste.paymentX.ui.main.HelpCentre
import com.iste.paymentX.ui.main.TermsAndConditions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import java.io.IOException

class MerchantProfile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    // UI elements
    private lateinit var storeNameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var merchantNameTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var editButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_merchant_profile)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Initialize UI elements
        storeNameTextView = findViewById(R.id.merchant_store_name)
        emailTextView = findViewById(R.id.merchant_user_email)
        merchantNameTextView = findViewById(R.id.merchant_name)
        phoneTextView = findViewById(R.id.merchant_phno)
        editButton = findViewById(R.id.edit)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        googleSignInClient = GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )

        // Set system UI colors
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.navigationBarColor = ContextCompat.getColor(this, R.color.navigation_bar_color)
            window.statusBarColor = ContextCompat.getColor(this, R.color.header)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }

        // Get merchant data from Firebase Auth and populate initial data
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Store name remains permanent as "Balaji Stores"
            storeNameTextView.text = "Balaji Stores"
            emailTextView.text = currentUser.email ?: "No email available"
            merchantNameTextView.text = currentUser.displayName ?: "Merchant"
            phoneTextView.text = currentUser.phoneNumber ?: "Not set"

            // Fetch more details from backend API
            fetchMerchantDetails()
        } else {
            // Use data from intent as fallback if Firebase user is null
            val merchantName = intent.getStringExtra("MERCHANT_NAME") ?: "Merchant"
            val merchantEmail = intent.getStringExtra("MERCHANT_EMAIL") ?: "merchant@example.com"

            storeNameTextView.text = "Balaji Stores"
            emailTextView.text = merchantEmail
            merchantNameTextView.text = merchantName
        }

        // Set up edit profile button click listener (functionality to be implemented later)
        editButton.setOnClickListener {
            animateButtonClick(it)
            // TODO: Navigate to MerchantEditProfile activity when created
            // val intent = Intent(this, MerchantEditProfile::class.java)
            // startActivity(intent)
        }

        // Set up sign out functionality
        findViewById<ConstraintLayout>(R.id.sign_out).setOnClickListener {
            animateButtonClick(it)
            handleLogout()
        }
        findViewById<CardView>(R.id.sign_out1).setOnClickListener {
            animateButtonClick(it)
            handleLogout()
        }

        // Set up navigation buttons
        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, MerchantMainScreen::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            animateButtonClick(it)
            startActivity(intent)
            finish()
        }

        findViewById<ImageView>(R.id.btnTransact).setOnClickListener {
            // Navigate to transactions and finish this activity
            val intent = Intent(this, MerchantTransactions::class.java)
            animateButtonClick(it)
            startActivity(intent)
            finish()
        }

        findViewById<CardView>(R.id.terms_cond).setOnClickListener {
            val intent = Intent(this, TermsAndConditions::class.java)
            animateButtonClick(it)
            startActivity(intent)
        }

        findViewById<CardView>(R.id.contact).setOnClickListener {
            val intent = Intent(this, ContactUs::class.java)
            animateButtonClick(it)
            startActivity(intent)
        }

        findViewById<CardView>(R.id.feedback).setOnClickListener {
            val intent = Intent(this, FeedbackForm::class.java)
            animateButtonClick(it)
            startActivity(intent)
        }

        findViewById<CardView>(R.id.help).setOnClickListener {
            val intent = Intent(this, HelpCentre::class.java)
            animateButtonClick(it)
            startActivity(intent)
        }

        findViewById<CardView>(R.id.developer_team).setOnClickListener {
            val intent = Intent(this, DeveloperTeam::class.java)
            animateButtonClick(it)
            startActivity(intent)
        }
    }

    // Helper method to add click animation
    private fun animateButtonClick(view: android.view.View) {
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }

    private suspend fun getFirebaseIdToken(): String? {
        return try {
            val user = auth.currentUser
            user?.getIdToken(false)?.await()?.token
        } catch (e: Exception) {
            Log.e("MerchantProfile", "Error getting Firebase ID token", e)
            null
        }
    }

    private fun fetchMerchantDetails() {
        lifecycleScope.launch {
            try {
                val token = getFirebaseIdToken()
                if (token != null) {
                    // Fetch merchant details from the backend API
                    // Note: You might need to create a separate API endpoint for merchants
                    // For now, using the same checkUser endpoint
                    val response = RetrofitInstance.api.checkUser("Bearer $token")

                    if (response.isSuccessful && response.body() != null) {
                        val merchantData = response.body()?.user

                        // Update UI with merchant data from backend
                        merchantData?.let { merchant ->
                            // Store name remains permanent
                            storeNameTextView.text = "Balaji Stores"

                            // Only update if data is not null and not empty
                            merchant.displayName?.let { if (it.isNotEmpty()) merchantNameTextView.text = it }
                            merchant.email?.let { if (it.isNotEmpty()) emailTextView.text = it }

                            // Update phone number if available
                            merchant.phoneNumber?.let {
                                if (it.isNotEmpty()) {
                                    // Format phone number for display if needed
                                    if (it.startsWith("+")) {
                                        phoneTextView.text = it
                                    } else {
                                        phoneTextView.text = "+91 $it"
                                    }
                                }
                            }
                        }
                    } else {
                        Log.e("MerchantProfile", "Failed to fetch merchant details: ${response.code()}")
                    }
                }
            } catch (e: IOException) {
                Log.e("MerchantProfile", "Network error while fetching merchant details", e)
            } catch (e: HttpException) {
                Log.e("MerchantProfile", "HTTP error while fetching merchant details", e)
            } catch (e: Exception) {
                Log.e("MerchantProfile", "Unexpected error while fetching merchant details", e)
            }
        }
    }

    private fun handleLogout() {
        try {
            auth.signOut()
            googleSignInClient.revokeAccess().addOnCompleteListener {
                startActivity(Intent(this, GoogleAuthActivity::class.java))
                finishAffinity() // Close all activities in the stack
            }
        } catch (e: Exception) {
            Log.e("MerchantProfile", "Exception during logout: ", e)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh merchant data when returning from other screens
        fetchMerchantDetails()
    }
}