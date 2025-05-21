package com.iste.paymentX.ui.merchant

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.iste.paymentX.R
import com.iste.paymentX.data.model.CreatePinRequest
import com.iste.paymentX.data.model.RetrofitInstance
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import java.io.IOException

class MerchantConfirmPIN : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var backarrow: ImageView
    private lateinit var vibrator: Vibrator
    private lateinit var btnVerify: Button
    private var isProcessing = false

    // store credientials
    private var userName: String? = null
    private var userEmail: String? = null
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_merchant_confirm_pin)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Get the user information from intent
        userName = intent.getStringExtra("USER_NAME")
        userEmail = intent.getStringExtra("USER_EMAIL")
        userId = intent.getStringExtra("USER_ID")

        auth = FirebaseAuth.getInstance()

        // Initialize vibrator
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Retrieve the PIN passed from CreateTransPIN
        val transactionPin = intent.getStringExtra("transactionPin")
        backarrow = findViewById(R.id.merchback)

        backarrow.setOnClickListener {
            val intent = Intent(this,MerchantCreateTransPIN::class.java)
            startActivity(intent)
        }

        if (transactionPin == null) {
            Toast.makeText(this, "Transaction PIN not received!", Toast.LENGTH_SHORT).show()
            vibratePhone()
            finish() // Exit if no PIN is passed
            return
        }

        val pinInputs = listOf(
            findViewById<EditText>(R.id.merchinput1),
            findViewById<EditText>(R.id.merchinput2),
            findViewById<EditText>(R.id.merchinput3),
            findViewById<EditText>(R.id.merchinput4),
            findViewById<EditText>(R.id.merchinput5),
            findViewById<EditText>(R.id.merchinput6)
        )

        pinInputs.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1) {
                        // Move to the next EditText
                        if (index < pinInputs.size - 1) {
                            pinInputs[index + 1].requestFocus()
                        }
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            editText.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    // Clear current EditText and move to the previous one
                    if (editText.text.isEmpty() && index > 0) {
                        pinInputs[index - 1].requestFocus()
                        pinInputs[index - 1].text.clear()
                    }
                }
                false
            }
        }

        btnVerify = findViewById<Button>(R.id.merchbtnConfirm)
        btnVerify.setOnClickListener {
            // Prevent multiple clicks
            if (isProcessing) {
                return@setOnClickListener
            }

            // Gather input PIN
            val enteredPin = pinInputs.joinToString("") { it.text.toString() }

            if (enteredPin.length == 6) {
                if (enteredPin == transactionPin) {
                    // Set processing flag and update button text
                    setProcessing(true)
                    // Redirect to MainScreen
                    createPin(enteredPin)
                } else {
                    // Show error if PINs do not match
                    Toast.makeText(this, "PINs do not match. Try again.", Toast.LENGTH_SHORT).show()
                    vibratePhone()
                }
            } else {
                // Show error if PIN is incomplete
                Toast.makeText(this, "Please enter all 6 digits.", Toast.LENGTH_SHORT).show()
                vibratePhone()
            }
        }
        val sharedPref = getSharedPreferences("PaymentX", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("transactionPin", transactionPin)  // Save PIN
        editor.apply()
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

    private suspend fun createPinHelper(authToken: String,pin: String) {
        try {
            val request = CreatePinRequest(pin)
            val response = RetrofitInstance.api.createPin(authToken,request)
            if (response.isSuccessful && response.body() != null) {
                val intent = Intent(this, MerchantAccountInfo::class.java).apply {
                    putExtra("USER_NAME", userName)
                    putExtra("USER_EMAIL", userEmail)
                    putExtra("USER_ID", userId)
                }
                startActivity(intent)
                finish()
            } else {
                Log.e("HomeActivity", "Response not successful: ${response.code()} - ${response.message()}")
                runOnUiThread {
                    Toast.makeText(this, "Failed to create PIN. Please try again.", Toast.LENGTH_SHORT).show()
                    setProcessing(false)
                }
            }
        } catch (e: IOException) {
            Log.e("HomeActivity", "IOException, you might not have internet connection", e)
            runOnUiThread {
                Toast.makeText(this, "Network error. Check your connection.", Toast.LENGTH_SHORT).show()
                setProcessing(false)
            }
        } catch (e: HttpException) {
            Log.e("HomeActivity", "HttpException, unexpected response", e)
            runOnUiThread {
                Toast.makeText(this, "Server error. Please try again later.", Toast.LENGTH_SHORT).show()
                setProcessing(false)
            }
        }
    }

    private fun createPin(pin: String) {
        lifecycleScope.launch {
            val token = getFirebaseIdToken()
            if (token != null) {
                createPinHelper("Bearer $token",pin)
            } else {
                Log.e("HomeActivity", "Failed to get Firebase ID token")
                runOnUiThread {
                    Toast.makeText(this@MerchantConfirmPIN, "Authentication error. Please try again.", Toast.LENGTH_SHORT).show()
                    setProcessing(false)
                }
            }
        }
    }

    private fun setProcessing(processing: Boolean) {
        isProcessing = processing
        runOnUiThread {
            btnVerify.isEnabled = !processing
            btnVerify.text = if (processing) "Confirming..." else "Confirm"
        }
    }

    private fun vibratePhone() {
        if (vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(200)
            }
        }
    }
}