package com.iste.paymentX.ui.merchant

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.iste.paymentX.R
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.iste.paymentX.data.model.AttachPhoneRequest
import com.iste.paymentX.data.model.RetrofitInstance
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import java.io.IOException

class MerchantUpdatePhoneOtpVerification : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var backarrow: ImageView
    private lateinit var btnVerify: Button
    private lateinit var otpText: TextView

    // Store credentials
    private var userName: String? = null
    private var userEmail: String? = null
    private var phoneNumber: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_merchant_update_phone_otp_verification)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Get the user information from intent
        userName = intent.getStringExtra("MERCHANT_NAME")
        userEmail = intent.getStringExtra("MERCHANT_EMAIL")
        phoneNumber = intent.getStringExtra("phoneNumber")

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize UI elements
        backarrow = findViewById(R.id.back)
        btnVerify = findViewById(R.id.btnVerify)
        otpText = findViewById(R.id.enter_otp_text)

        // Update the OTP text to include the phone number
        otpText.text = getString(R.string.otp_message, phoneNumber)

        // Set up OTP input fields
        val inputs = listOf(
            findViewById<EditText>(R.id.uid_input1),
            findViewById<EditText>(R.id.uid_input2),
            findViewById<EditText>(R.id.uid_input3),
            findViewById<EditText>(R.id.uid_input4),
            findViewById<EditText>(R.id.uid_input5)
        )
        setUpOtpInputs(inputs)

        // Set click listener for back arrow
        backarrow.setOnClickListener {
            finish() // Go back to update phone number screen
        }

        // Set click listener for verify button
        btnVerify.setOnClickListener {
            // In a real app, we would verify OTP here
            // For this implementation, we'll simulate success
            if (validateOtpInputs(inputs)) {
                if (phoneNumber != null) {
                    attachPhone(phoneNumber!!)
                }
            } else {
                Toast.makeText(this, "Please enter a valid OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setUpOtpInputs(inputs: List<EditText>) {
        for (i in inputs.indices) {
            val current = inputs[i]
            val next = inputs.getOrNull(i + 1) // Get next input or null if last
            val previous = inputs.getOrNull(i - 1) // Get previous input or null if first

            current.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (!s.isNullOrEmpty()) {
                        next?.requestFocus() // Move to the next box
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            current.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (current.text.isEmpty()) {
                        previous?.apply {
                            requestFocus() // Move to the previous box
                            text.clear() // Clear the previous box
                        }
                    }
                }
                false
            }
        }
    }

    private fun validateOtpInputs(inputs: List<EditText>): Boolean {
        // Check if all OTP fields are filled
        for (input in inputs) {
            if (input.text.isEmpty()) {
                return false
            }
        }
        return true
    }

    private suspend fun getFirebaseIdToken(): String? {
        return try {
            val user = auth.currentUser
            user?.getIdToken(false)?.await()?.token
        } catch (e: Exception) {
            Log.e("PhoneOtpVerification", "Error getting Firebase ID token", e)
            null
        }
    }

    private suspend fun attachPhoneHelper(authToken: String, phoneNumber: String) {
        try {
            val request = AttachPhoneRequest(phoneNumber)
            val response = RetrofitInstance.api.attachPhone(authToken, request)
            if (response.isSuccessful && response.body() != null) {
                // Phone number updated successfully
                Toast.makeText(this, "Phone number updated successfully", Toast.LENGTH_SHORT).show()

                // Navigate back to Edit Profile screen
                navigateToEditProfile()
            } else {
                // Handle error responses based on status code
                when (response.code()) {
                    409 -> {
                        // 409 Conflict - Phone number is already linked to another profile
                        Toast.makeText(
                            this,
                            "This phone number is already linked to another profile",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    401 -> {
                        Toast.makeText(
                            this,
                            "Authentication failed. Please login again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        Toast.makeText(
                            this,
                            "Failed to update phone number: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                Log.e("PhoneOtpVerification", "Response not successful: ${response.code()} - ${response.message()}")
            }
        } catch (e: IOException) {
            Toast.makeText(this, "Network error, check your connection", Toast.LENGTH_SHORT).show()
            Log.e("PhoneOtpVerification", "IOException, you might not have internet connection", e)
        } catch (e: HttpException) {
            // Handle HTTP exceptions, including specific status codes
            if (e.code() == 409) {
                Toast.makeText(
                    this,
                    "This phone number is already linked to another profile",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(this, "HTTP error: ${e.code()}", Toast.LENGTH_SHORT).show()
            }
            Log.e("PhoneOtpVerification", "HttpException, unexpected response", e)
        } catch (e: Exception) {
            Toast.makeText(this, "Unexpected error", Toast.LENGTH_SHORT).show()
            Log.e("PhoneOtpVerification", "Unexpected error", e)
        }
    }

    private fun attachPhone(phoneNumber: String) {
        lifecycleScope.launch {
            val token = getFirebaseIdToken()
            if (token != null) {
                attachPhoneHelper("Bearer $token", phoneNumber)
            } else {
                Toast.makeText(
                    this@MerchantUpdatePhoneOtpVerification,
                    "Authentication error, please sign in again",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("PhoneOtpVerification", "Failed to get Firebase ID token")
            }
        }
    }

    private fun navigateToEditProfile() {
        // Clear the activity stack up to EditProfile
        val intent = Intent(this, MerchantEditProfile::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
}