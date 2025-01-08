package com.iste.paymentx.ui.auth

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
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.iste.paymentx.R
import com.iste.paymentx.data.model.CreatePinRequest
import com.iste.paymentx.data.model.ErrorResponse
import com.iste.paymentx.data.model.RetrofitInstance
import com.iste.paymentx.data.model.WalletRequest
import com.iste.paymentx.ui.main.MainScreen
import com.iste.paymentx.ui.main.TickMarkAnimation
import com.iste.paymentx.ui.main.TopUpCompleted
import com.iste.paymentx.ui.main.WithdrawCompleted
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import java.io.IOException

class PinVerifyPage : AppCompatActivity() {
    private lateinit var auth : FirebaseAuth
    private lateinit var vibrator: Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        auth = FirebaseAuth.getInstance()
        setContentView(R.layout.activity_pin_verify_page)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

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

        val btnVerify = findViewById<Button>(R.id.btnVerify)
        btnVerify.setOnClickListener {
            val enteredPin = pinInputs.joinToString("") { it.text.toString() }
            if (enteredPin.length == 6) {
                    val callingActivity = intent.getStringExtra("CALLING_ACTIVITY")
                    if (callingActivity == "ViewBalance") {
                        verifyPinBalance(enteredPin)
                    } else {
                            val amount = intent.getDoubleExtra("EXTRA_AMOUNT", 0.0).toInt()
                            if (amount > 0) {
                                // Start tick mark animation
                                if (callingActivity != null) {
                                    verifyPinWallet(enteredPin,amount,callingActivity)
                                }
                            } else {
                                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
                                vibratePhone()
                            }
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

    private suspend fun verifyPinHelperBalance(authToken: String,pin: String){
        try {
            val request = CreatePinRequest(pin)
            val response = RetrofitInstance.api.verifyPin(authToken,request)
            if (response.isSuccessful && response.body() != null) {
                setResult(RESULT_OK) // Send success result to MainScreen
                finish()
            } else {
                Toast.makeText(this,"Incorrect Pin",Toast.LENGTH_SHORT).show()
                vibratePhone()
                finish()
                Log.e("HomeActivity", "Response not successful: ${response.code()} - ${response.message()}")
            }
        } catch (e: IOException) {
            Toast.makeText(this,"Error",Toast.LENGTH_SHORT).show()
            vibratePhone()
            finish()
            Log.e("HomeActivity", "IOException, you might not have internet connection", e)
        } catch (e: HttpException) {
            Toast.makeText(this,"Error",Toast.LENGTH_SHORT).show()
            vibratePhone()
            finish()
            Log.e("HomeActivity", "HttpException, unexpected response", e)
        }
    }

    private fun verifyPinBalance(pin: String){
        lifecycleScope.launch {
            val token = getFirebaseIdToken()
            if (token != null) {
                verifyPinHelperBalance("Bearer $token",pin)
            } else {
                Log.e("HomeActivity", "Failed to get Firebase ID token")
            }
        }
    }

    private suspend fun verifyPinHelperWallet(authToken: String,pin: String,amount: Int,callingActivity: String){
        try {
            val request = WalletRequest(pin, amount)
            if(callingActivity=="Withdraw"){
                val response = RetrofitInstance.api.withdraw(authToken, request)
                if(response.isSuccessful && response.body()!=null){
                    val tickIntent = Intent(this, TickMarkAnimation::class.java)
                    startActivity(tickIntent)
                    Handler(Looper.getMainLooper()).postDelayed({
                        val destinationIntent = Intent(this, WithdrawCompleted::class.java).apply {
                            putExtra("EXTRA_AMOUNT", amount)
                        }
                        startActivity(destinationIntent)
                        finish()
                    },2000)
                }
                else {
                    val errorBody = response.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                    Toast.makeText(this,errorResponse.message,Toast.LENGTH_SHORT).show()
                    vibratePhone()
                    val intent = Intent(this, MainScreen::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                    Log.e("HomeActivity", "Response not successful: ${response.code()} - ${response.message()}")
                }
            }
            else if(callingActivity=="TopUp"){
                val response = RetrofitInstance.api.topup(authToken, request)
                if(response.isSuccessful && response.body()!=null){
                    val tickIntent = Intent(this, TickMarkAnimation::class.java)
                    startActivity(tickIntent)
                    Handler(Looper.getMainLooper()).postDelayed({
                        val destinationIntent = Intent(this, TopUpCompleted::class.java).apply {
                            putExtra("EXTRA_AMOUNT", amount)
                        }
                        startActivity(destinationIntent)
                        finish()
                    },2000)
                }
                else {
                    val errorBody = response.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                    Toast.makeText(this,errorResponse.message,Toast.LENGTH_SHORT).show()
                    vibratePhone()
                    val intent = Intent(this, MainScreen::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                    Log.e("HomeActivity", "Response not successful: ${response.code()} - ${response.message()}")
                }
            }
            else{
                val intent = Intent(this, MainScreen::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        } catch (e: IOException) {
            Toast.makeText(this,"Error",Toast.LENGTH_SHORT).show()
            vibratePhone()
            val intent = Intent(this, MainScreen::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            Log.e("HomeActivity", "IOException, you might not have internet connection", e)
        } catch (e: HttpException) {
            Toast.makeText(this,"Error",Toast.LENGTH_SHORT).show()
            vibratePhone()
            val intent = Intent(this, MainScreen::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            Log.e("HomeActivity", "HttpException, unexpected response", e)
        }
    }

    private fun verifyPinWallet(pin: String,amount: Int,callingActivity: String){
        lifecycleScope.launch {
            val token = getFirebaseIdToken()
            if (token != null) {
                verifyPinHelperWallet("Bearer $token",pin,amount,callingActivity)
            } else {
                Log.e("HomeActivity", "Failed to get Firebase ID token")
            }
        }
    }
}