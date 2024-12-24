package com.iste.paymentx.ui.main

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.iste.paymentx.R
import com.iste.paymentx.data.model.RetrofitInstance
import com.iste.paymentx.data.model.User
import com.iste.paymentx.ui.auth.Display
import com.iste.paymentx.ui.auth.GoogleAuthActivity
import com.iste.paymentx.ui.auth.ScanId
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import java.io.IOException

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val api = RetrofitInstance.api
        setContentView(R.layout.activity_home)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        auth = FirebaseAuth.getInstance()

        // Initialize GoogleSignInClient for revoking access on logout
        googleSignInClient = GoogleSignIn.getClient(this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )

        // Retrieve the user information from the intent
        val userName = intent.getStringExtra("USER_NAME")
        val userEmail = intent.getStringExtra("USER_EMAIL")
        val userId = intent.getStringExtra("USER_ID")

        // Display user information (for example purposes)
        findViewById<TextView>(R.id.user_name_text).text = "$userName"
        findViewById<TextView>(R.id.user_email_text).text = "$userEmail"
        findViewById<TextView>(R.id.user_id_text).text = "User ID: $userId"

        findViewById<Button>(R.id.logout_button).setOnClickListener {
            try {
                // Sign out from Firebase
                auth.signOut()
                // Revoke Google account access to prompt account chooser on next sign-in
                googleSignInClient.revokeAccess().addOnCompleteListener {
                    val intent = Intent(this, GoogleAuthActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                Log.e("HomeActivity", "Exception during logout: ", e)
            }
        }
        findViewById<Button>(R.id.checkBut).setOnClickListener() {
            if (userId != null) {
                login(email = userEmail, displayName = userName, uid = userId, isMerchant = false)
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

    private suspend fun loginHelper(authToken: String,user:User) {

        try {
            val response = RetrofitInstance.api.login(authToken,user)
            if (response.isSuccessful && response.body() != null) {
                val intent = Intent(this, ScanId::class.java)
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

    private fun login(email:String?,displayName: String?,uid:String,isMerchant:Boolean) {
        lifecycleScope.launch {
            val user : User   = User(email = email,displayName = displayName,uid =uid,isMerchant = false)
            val token = getFirebaseIdToken()
            if (token != null) {
                loginHelper("Bearer $token",user)
            } else {
                Log.e("HomeActivity", "Failed to get Firebase ID token")
            }
        }
    }
}