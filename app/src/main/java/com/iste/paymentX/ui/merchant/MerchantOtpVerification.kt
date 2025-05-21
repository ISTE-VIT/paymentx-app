package com.iste.paymentX.ui.merchant

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.iste.paymentX.R
import com.iste.paymentX.data.model.AttachPhoneRequest
import com.iste.paymentX.data.model.RetrofitInstance
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import java.io.IOException

class MerchantOtpVerification : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var backarrow: ImageView
    private lateinit var btnVerify: Button
    private var isProcessing = false

    // store credientials
    private var userName: String? = null
    private var userEmail: String? = null
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_merchant_otp_verification)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Get the user information from intent
        userName = intent.getStringExtra("USER_NAME")
        userEmail = intent.getStringExtra("USER_EMAIL")
        userId = intent.getStringExtra("USER_ID")

        auth = FirebaseAuth.getInstance()
        val phoneNumber = intent.getStringExtra("phoneNumber")
        val backarrow = findViewById<ImageView>(R.id.merchback)

        // Add this line to update the TextView
        val otpText = findViewById<TextView>(R.id.merchenter_otp_text)
        otpText.text = getString(R.string.otp_message, phoneNumber)

        val inputs = listOf(
            findViewById<EditText>(R.id.merchuid_input1),
            findViewById<EditText>(R.id.merchuid_input2),
            findViewById<EditText>(R.id.merchuid_input3),
            findViewById<EditText>(R.id.merchuid_input4),
            findViewById<EditText>(R.id.merchuid_input5)
        )
        setUpOtpInputs(inputs)

        backarrow.setOnClickListener {
            val intent = Intent(this,MerchantPhoneNumberVerification::class.java)
            startActivity(intent)
        }

        btnVerify = findViewById<Button>(R.id.merchbtnVerify)
        btnVerify.setOnClickListener {
            // Prevent multiple clicks
            if (isProcessing) {
                return@setOnClickListener
            }

            // Set processing flag and update button text
            setProcessing(true)

            if (phoneNumber != null) {
                attachPhone(phoneNumber)
            } else {
                setProcessing(false)
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

    private fun openCreateTransPINPage() {
        val intent = Intent(this, MerchantCreateTransPIN::class.java).apply {
            putExtra("USER_NAME", userName)
            putExtra("USER_EMAIL", userEmail)
            putExtra("USER_ID", userId)
        }
        startActivity(intent)
    }

    private suspend fun getFirebaseIdToken(): String? {
        return try {
            val user = auth.currentUser
            user?.getIdToken(false)?.await()?.token
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error getting Firebase ID token", e)
            setProcessing(false)
            null
        }
    }

    private suspend fun attachPhoneHelper(authToken: String, phoneNumber: String) {
        try {
            val request = AttachPhoneRequest(phoneNumber)
            val response = RetrofitInstance.api.attachPhone(authToken, request)
            if (response.isSuccessful && response.body() != null) {
                openCreateTransPINPage()
            } else {
                Log.e("HomeActivity", "Response not successful: ${response.code()} - ${response.message()}")
                runOnUiThread {
                    setProcessing(false)
                }
            }
        } catch (e: IOException) {
            Log.e("HomeActivity", "IOException, you might not have internet connection", e)
            runOnUiThread {
                setProcessing(false)
            }
        } catch (e: HttpException) {
            Log.e("HomeActivity", "HttpException, unexpected response", e)
            runOnUiThread {
                setProcessing(false)
            }
        }
    }

    private fun attachPhone(phoneNumber: String) {
        lifecycleScope.launch {
            val token = getFirebaseIdToken()
            if (token != null) {
                attachPhoneHelper("Bearer $token", phoneNumber)
            } else {
                Log.e("HomeActivity", "Failed to get Firebase ID token")
                runOnUiThread {
                    setProcessing(false)
                }
            }
        }
    }

    private fun setProcessing(processing: Boolean) {
        isProcessing = processing
        runOnUiThread {
            btnVerify.isEnabled = !processing
            btnVerify.text = if (processing) "Verifying..." else "Verify"
        }
    }
}