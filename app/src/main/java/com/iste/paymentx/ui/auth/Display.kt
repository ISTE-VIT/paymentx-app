package com.iste.paymentx.ui.auth

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.iste.paymentx.R
import com.iste.paymentx.data.model.AttachIdRequest
import com.iste.paymentx.data.model.RetrofitInstance
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import java.io.IOException

class Display : AppCompatActivity() {

    private lateinit var uidTextView: TextView
    private lateinit var confirmButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var vibrator: Vibrator

    // store credientials
    private var userName: String? = null
    private var userEmail: String? = null
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_display)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        auth = FirebaseAuth.getInstance()

        // Get the user information from intent
        userName = intent.getStringExtra("USER_NAME")
        userEmail = intent.getStringExtra("USER_EMAIL")
        userId = intent.getStringExtra("USER_ID")

        // Initialize vibrator
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        try {
            // Initialize TextView and Button
            uidTextView = findViewById(R.id.display_UID)
            confirmButton = findViewById(R.id.Confirm_button)

            // Get the UID from the intent
            val uid = intent.getStringExtra("CARD_UID")

            if (uid != null) {
                uidTextView.text = "Card UID: $uid"
                Log.d("DisplayUID", "Successfully displayed UID: $uid")
            } else {
                uidTextView.text = "No UID received"
                Log.e("DisplayUID", "No UID received in intent")
                Toast.makeText(this, "Error: No UID received", Toast.LENGTH_SHORT).show()
                vibratePhone()
            }

            // Set OnClickListener for the Confirm button
            confirmButton.setOnClickListener {
                if(uid!=null){
                    attachId(uid)
                }

            }

        } catch (e: Exception) {
            Log.e("DisplayUID", "Error in onCreate: ${e.message}")
            Toast.makeText(this, "Error displaying UID", Toast.LENGTH_SHORT).show()
            vibratePhone()
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

    private suspend fun attachIdHelper(authToken: String,idCardUid: String) {
        try {
            val request = AttachIdRequest(idCardUid)
            val response = RetrofitInstance.api.attachId(authToken,request)
            if (response.isSuccessful && response.body() != null) {
                val intent = Intent(this, Phonenumber::class.java).apply {
                    putExtra("USER_NAME", userName)
                    putExtra("USER_EMAIL", userEmail)
                    putExtra("USER_ID", userId)
                }
                startActivity(intent)
            } else {
                Log.e("HomeActivity", "Response not successful: ${response.code()} - ${response.message()}")
            }
        } catch (e: IOException) {
            Log.e("HomeActivity", "IOException, you might not have internet connection", e)
        } catch (e: HttpException) {
            Log.e("HomeActivity", "HttpException, unexpected response", e)
        }
    }

    private fun attachId(idCardUid: String) {
        lifecycleScope.launch {
            val token = getFirebaseIdToken()
            if (token != null) {
                attachIdHelper("Bearer $token",idCardUid)
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