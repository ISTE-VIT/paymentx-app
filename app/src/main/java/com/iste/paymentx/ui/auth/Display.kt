package com.iste.paymentx.ui.auth

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.iste.paymentx.data.model.User
import com.iste.paymentx.ui.main.TickMarkAnimation
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import java.io.IOException

class Display : AppCompatActivity() {

    private lateinit var uidTextView: TextView
    private lateinit var confirmButton: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_display)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        auth = FirebaseAuth.getInstance()

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
                val intent = Intent(this, Phonenumber::class.java)
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
}