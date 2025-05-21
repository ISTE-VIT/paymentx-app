package com.iste.paymentX.ui.merchant

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.iste.paymentX.R
import com.iste.paymentX.data.model.RetrofitInstance
import com.iste.paymentX.ui.auth.UpdatePhoneNumber
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import java.io.IOException

class MerchantEditProfile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    // UI elements
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var updatePhoneButton: Button
    private lateinit var backButton: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_merchant_edit_profile)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Initialize UI elements
        nameTextView = findViewById(R.id.name)
        emailTextView = findViewById(R.id.email)
        phoneTextView = findViewById(R.id.phno)
        updatePhoneButton = findViewById(R.id.btn_update_phno)
        backButton = findViewById(R.id.back)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Set back button click listener
        backButton.setOnClickListener {
            finish() // Go back to the previous screen
        }

        // Fetch and display user data
        fetchMerchantData()

        // Set click listener for Update Phone Number button
        updatePhoneButton.setOnClickListener {
            navigateToMerchantUpdatePhoneNumber()
        }
    }

    private fun fetchMerchantData() {
        // Get user data from Firebase Auth
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Populate with basic Firebase data first
            nameTextView.text = currentUser.displayName ?: "User"
            emailTextView.text = currentUser.email ?: "No email available"
            phoneTextView.text = currentUser.phoneNumber ?: "Not set"

            // Fetch more details from backend API
            fetchMerchantDetailsFromBackend()
        } else {
            // Fallback if no user is signed in
            Toast.makeText(this, "No user signed in", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private suspend fun getFirebaseIdToken(): String? {
        return try {
            val user = auth.currentUser
            user?.getIdToken(false)?.await()?.token
        } catch (e: Exception) {
            Log.e("EditProfile", "Error getting Firebase ID token", e)
            null
        }
    }

    private fun fetchMerchantDetailsFromBackend() {
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
                        Log.e("EditProfile", "Failed to fetch user details: ${response.code()}")
                    }
                }
            } catch (e: IOException) {
                Log.e("EditProfile", "Network error while fetching user details", e)
            } catch (e: HttpException) {
                Log.e("EditProfile", "HTTP error while fetching user details", e)
            } catch (e: Exception) {
                Log.e("EditProfile", "Unexpected error while fetching user details", e)
            }
        }
    }

    private fun navigateToMerchantUpdatePhoneNumber() {
        val intent = Intent(this, MerchantUpdatePhoneNumber::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        // Refresh user data when returning to this activity
        fetchMerchantData()
    }

}