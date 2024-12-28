package com.iste.paymentx.ui.auth

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
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.iste.paymentx.R
import com.iste.paymentx.data.model.CreatePinRequest
import com.iste.paymentx.data.model.RetrofitInstance
import com.iste.paymentx.ui.main.MainScreen
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import java.io.IOException

class ConfirmPIN : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var backarrow: ImageView
    private lateinit var vibrator: Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_confirm_pin)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        auth = FirebaseAuth.getInstance()

        // Initialize vibrator
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Retrieve the PIN passed from CreateTransPIN
        val transactionPin = intent.getStringExtra("transactionPin")
        val backarrow = findViewById<ImageView>(R.id.back)

        backarrow.setOnClickListener {
            val intent = Intent(this,CreateTransPIN::class.java)
            startActivity(intent)
        }

        if (transactionPin == null) {
            Toast.makeText(this, "Transaction PIN not received!", Toast.LENGTH_SHORT).show()
            vibratePhone()
            finish() // Exit if no PIN is passed
            return
        }

        val pinInputs = listOf(
            findViewById<EditText>(R.id.input1),
            findViewById<EditText>(R.id.input2),
            findViewById<EditText>(R.id.input3),
            findViewById<EditText>(R.id.input4),
            findViewById<EditText>(R.id.input5),
            findViewById<EditText>(R.id.input6)
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

        val btnVerify = findViewById<Button>(R.id.btnConfirm)
        btnVerify.setOnClickListener {
            // Gather input PIN
            val enteredPin = pinInputs.joinToString("") { it.text.toString() }

            if (enteredPin.length == 6) {
                if (enteredPin == transactionPin) {
                    // Redirect to MainScreen
                    createPin(enteredPin)
                     // Optional: Close this activity
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
            null
        }
    }

    private suspend fun createPinHelper(authToken: String,pin: String) {
        try {
            val request = CreatePinRequest(pin)
            val response = RetrofitInstance.api.createPin(authToken,request)
            if (response.isSuccessful && response.body() != null) {
                val intent = Intent(this, MainScreen::class.java)
                startActivity(intent)
                finish()
            } else {
                Log.e("HomeActivity", "Response not successful: ${response.code()} - ${response.message()}")
            }
        } catch (e: IOException) {
            Log.e("HomeActivity", "IOException, you might not have internet connection", e)
        } catch (e: HttpException) {
            Log.e("HomeActivity", "HttpException, unexpected response", e)
        }
    }

    private fun createPin(pin: String) {
        lifecycleScope.launch {
            val token = getFirebaseIdToken()
            if (token != null) {
                createPinHelper("Bearer $token",pin)
            } else {
                Log.e("HomeActivity", "Failed to get Firebase ID token")
            }
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
