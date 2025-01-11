package com.iste.paymentX.ui.merchant

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.iste.paymentX.R
import com.iste.paymentX.data.model.RetrofitInstance
import com.iste.paymentX.data.model.TransactionRequest
import com.iste.paymentX.ui.main.TickMarkAnimation
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import java.io.IOException

class FetchCustomerPIN : AppCompatActivity() {
    private var amount: Int = 0
    private lateinit var btnVerify: Button
    private lateinit var vibrator: Vibrator
    private lateinit var auth : FirebaseAuth
    private var idCardUID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_fetch_customer_pin)
        auth = FirebaseAuth.getInstance()
        /// Get and verify amount
        amount = intent.getIntExtra("EXTRA_AMOUNT", 0)
        idCardUID = intent.getStringExtra("CARD_UID")
        val amountText = findViewById<TextView>(R.id.textView11)
        amountText.text = "to Pay ₹$amount"

        // Initialize vibrator
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // PIN input fields
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

        btnVerify = findViewById(R.id.btnVerify)
        btnVerify.setOnClickListener {
            val enteredPin = pinInputs.joinToString("") { it.text.toString() }
            if (enteredPin.length == 6) {
                if(idCardUID!=null && amount>=0){
                    initTrans(idCardUID!!,amount,enteredPin)
                }
            } else {
                Toast.makeText(this, "Please enter all 6 digits.", Toast.LENGTH_SHORT).show()
                vibratePhone()
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

    private suspend fun getFirebaseIdToken(): String? {
        return try {
            val user = auth.currentUser
            user?.getIdToken(false)?.await()?.token
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error getting Firebase ID token", e)
            null
        }
    }

    private suspend fun initTransHelper(authToken: String,details: TransactionRequest) {
        try {
            //Toast.makeText(this,details.amount.toString()+details.pin+details.idCardUID,Toast.LENGTH_SHORT).show()
            val response = RetrofitInstance.api.doTrans(authToken,details)
            if (response.isSuccessful && response.body() != null) {
                //Toast.makeText(this,"hello",Toast.LENGTH_SHORT).show()
                val tickIntent = Intent(this, TickMarkAnimation::class.java)
                startActivity(tickIntent)
                Handler(Looper.getMainLooper()).postDelayed({
                    val destinationIntent = Intent(this, ReceiveCompleted::class.java).apply {
                        putExtra("EXTRA_AMOUNT", details.amount)
                    }
                    startActivity(destinationIntent)
                    finish()
                },2000)
            } else {
                Log.e("HomeActivity", "Response not successful: ${response.code()} - ${response.message()}")
            }
        } catch (e: IOException) {
            Log.e("HomeActivity", "IOException, you might not have internet connection", e)
        } catch (e: HttpException) {
            Log.e("HomeActivity", "HttpException, unexpected response", e)
        }
    }

    private fun initTrans(cardUID: String,amount: Int,pin:String) {
        lifecycleScope.launch {
            val token = getFirebaseIdToken()
            if (token != null) {
                val details = TransactionRequest(cardUID,amount,pin)
                initTransHelper("Bearer $token",details)
            } else {
                Log.e("HomeActivity", "Failed to get Firebase ID token")
            }
        }
    }
}