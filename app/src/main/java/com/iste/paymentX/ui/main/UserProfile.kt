package com.iste.paymentX.ui.main

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import java.io.IOException

class UserProfile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    // UI elements
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var uidTextView: TextView
    private lateinit var phoneTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_profile)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Initialize UI elements
        nameTextView = findViewById(R.id.user_name)
        emailTextView = findViewById(R.id.user_email)
        uidTextView = findViewById(R.id.uid)
        phoneTextView = findViewById(R.id.phno)

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

        // Get user data from Firebase Auth and populate initial data
        val currentUser = auth.currentUser
        if (currentUser != null) {
            nameTextView.text = currentUser.displayName ?: "User"
            emailTextView.text = currentUser.email ?: "No email available"
            uidTextView.text = currentUser.uid.take(10)  // Show first 10 characters of UID
            phoneTextView.text = currentUser.phoneNumber ?: "Not set"

            // Fetch more details from backend API
            fetchUserDetails()
        } else {
            // Use data from intent as fallback if Firebase user is null
            val userName = intent.getStringExtra("USER_NAME") ?: "User"
            val userEmail = intent.getStringExtra("USER_EMAIL") ?: "user@example.com"
            val userId = intent.getStringExtra("USER_ID") ?: ""

            nameTextView.text = userName
            emailTextView.text = userEmail
            uidTextView.text = userId.take(10)
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
            val intent = Intent(this, MainScreen::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            animateButtonClick(it)
            startActivity(intent)
            finish()
        }

        findViewById<ImageView>(R.id.btnTransact).setOnClickListener {
            // Navigate to transactions and finish this activity
            val intent = Intent(this, Transactions::class.java)
            animateButtonClick(it)
            startActivity(intent)
            finish()
        }

        findViewById<CardView>(R.id.terms_cond).setOnClickListener {
            val intent = Intent(this, TermsAndConditions::class.java)
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
            Log.e("UserProfile", "Error getting Firebase ID token", e)
            null
        }
    }

    private fun fetchUserDetails() {
        lifecycleScope.launch {
            try {
                val token = getFirebaseIdToken()
                if (token != null) {
                    // Fetch user details from the backend API
                    val response = RetrofitInstance.api.checkUser("Bearer $token")

                    if (response.isSuccessful && response.body() != null) {
                        val userData = response.body()?.user

                        // Update UI with user data from backend
                        userData?.let { user ->
                            // Only update if data is not null and not empty
                            user.displayName?.let { if (it.isNotEmpty()) nameTextView.text = it }
                            user.email?.let { if (it.isNotEmpty()) emailTextView.text = it }

                            // Update ID Card UID if available (formatted for display)
                            user.idCardUID?.let {
                                if (it.isNotEmpty()) {
                                    // Use the UID directly if it's already formatted
                                    if (it.contains(":")) {
                                        uidTextView.text = it.uppercase()
                                    } else {
                                        // If the backend sends "9A8EB9DE", format it as "9A:8E:B9:DE"
                                        // Only add colons if there aren't any
                                        var formattedUID = ""
                                        for (i in it.indices) {
                                            formattedUID += it[i]
                                            if ((i + 1) % 2 == 0 && i < it.length - 1) {
                                                formattedUID += ":"
                                            }
                                        }
                                        uidTextView.text = formattedUID.uppercase()
                                    }
                                }
                            }

                            // Update phone number if available
                            user.phoneNumber?.let {
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
                        Log.e("UserProfile", "Failed to fetch user details: ${response.code()}")
                    }
                }
            } catch (e: IOException) {
                Log.e("UserProfile", "Network error while fetching user details", e)
            } catch (e: HttpException) {
                Log.e("UserProfile", "HTTP error while fetching user details", e)
            } catch (e: Exception) {
                Log.e("UserProfile", "Unexpected error while fetching user details", e)
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
            Log.e("UserProfile", "Exception during logout: ", e)
        }
    }
}