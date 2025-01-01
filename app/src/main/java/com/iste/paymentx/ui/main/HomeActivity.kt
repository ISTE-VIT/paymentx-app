package com.iste.paymentx.ui.main

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
import com.iste.paymentx.ui.auth.GoogleAuthActivity
import com.iste.paymentx.ui.auth.ScanId
import com.iste.paymentx.ui.merchant.MerchantPhoneNumberVerification
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import java.io.IOException

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private var userRecieved: Boolean = false
    private lateinit var vibrator: Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Initialize vibrator
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        hideContent()
        setupAuth()
        setupUI()
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

    private fun hideContent() {
        findViewById<TextView>(R.id.user_name_text).visibility = View.GONE
        findViewById<TextView>(R.id.user_email_text).visibility = View.GONE
        findViewById<TextView>(R.id.user_id_text).visibility = View.GONE
        findViewById<ImageView>(R.id.logout_button).visibility = View.GONE
        findViewById<Button>(R.id.checkBut).visibility = View.GONE
        findViewById<Button>(R.id.checkMerchant).visibility = View.GONE
        findViewById<TextView>(R.id.or).visibility = View.GONE
    }

    private fun setupAuth() {
        auth = FirebaseAuth.getInstance()
        googleSignInClient = GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )
    }

    private fun setupUI() {
        checkUser()
        val userName = intent.getStringExtra("USER_NAME")
        val userEmail = intent.getStringExtra("USER_EMAIL")
        val userId = intent.getStringExtra("USER_ID")

        findViewById<TextView>(R.id.user_name_text).text = userName
        findViewById<TextView>(R.id.user_email_text).text = userEmail
        findViewById<TextView>(R.id.user_id_text).text = "User ID: $userId"

        setupButtons(userId, userName, userEmail)
    }

    private fun setupButtons(userId: String?, userName: String?, userEmail: String?) {
        findViewById<ImageView>(R.id.logout_button).setOnClickListener {
            handleLogout()
        }

        findViewById<Button>(R.id.checkBut).setOnClickListener {
            if (userId != null) {
                login(email = userEmail, displayName = userName, uid = userId, isMerchant = false)
            }
        }

        findViewById<Button>(R.id.checkMerchant).setOnClickListener {
            if (userId != null) {
                val intent = Intent(this, MerchantPhoneNumberVerification::class.java).apply {
                    putExtra("USER_EMAIL", userEmail)
                    putExtra("USER_NAME", userName)
                    putExtra("USER_ID", userId)
                    putExtra("IS_MERCHANT", true)
                }
                startActivity(intent)
            }
        }
    }

    private fun handleLogout() {
        try {
            auth.signOut()
            googleSignInClient.revokeAccess().addOnCompleteListener {
                startActivity(Intent(this, GoogleAuthActivity::class.java))
                finish()
            }
        } catch (e: Exception) {
            Log.e("HomeActivity", "Exception during logout: ", e)
        }
    }

    private fun showContent() {
        if (userRecieved){
            val intent = Intent(this, MainScreen::class.java)
            startActivity(intent)
            finish()
        }
        else {
            findViewById<TextView>(R.id.user_name_text).visibility = View.VISIBLE
            findViewById<TextView>(R.id.user_email_text).visibility = View.VISIBLE
            findViewById<TextView>(R.id.user_id_text).visibility = View.VISIBLE
            findViewById<ImageView>(R.id.logout_button).visibility = View.VISIBLE
            findViewById<Button>(R.id.checkBut).visibility = View.VISIBLE
            findViewById<Button>(R.id.checkMerchant).visibility = View.VISIBLE
            findViewById<TextView>(R.id.or).visibility = View.VISIBLE
        }
    }

    private suspend fun getFirebaseIdToken(): String? {
        return try {
            auth.currentUser?.getIdToken(false)?.await()?.token
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error getting Firebase ID token", e)
            null
        }
    }

    private suspend fun loginHelper(authToken: String, user: User) {
        try {
            val response = RetrofitInstance.api.login(authToken, user)
            if (response.isSuccessful && response.body() != null) {
                val intent = Intent(this, ScanId::class.java).apply {
                    putExtra("USER_NAME", user.displayName)
                    putExtra("USER_EMAIL", user.email)
                    putExtra("USER_ID", user.uid)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this,"hello",Toast.LENGTH_SHORT).show()
                vibratePhone()
                Log.e("HomeActivity", "Response not successful: ${response.code()} - ${response.message()}")
            }
        } catch (e: IOException) {
            Log.e("HomeActivity", "IOException, you might not have internet connection", e)
        } catch (e: HttpException) {
            Log.e("HomeActivity", "HttpException, unexpected response", e)
        }
    }

    private fun login(email: String?, displayName: String?, uid: String, isMerchant: Boolean) {
        lifecycleScope.launch {
            val user = User(email = email, displayName = displayName, uid = uid, isMerchant = isMerchant)
            getFirebaseIdToken()?.let { token ->
                loginHelper("Bearer $token", user)
            } ?: Log.e("HomeActivity", "Failed to get Firebase ID token")
        }
    }

    private suspend fun checkUserHelper(authToken: String) {
        try {
            val response = RetrofitInstance.api.checkUser(authToken)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()
                val user = body?.user
                if(user?.pin != null){
                    userRecieved = true
                    showContent()
                } else {
                    showContent()
                }
            } else {
                Log.e("HomeActivity", "Response not successful: ${response.code()} - ${response.message()}")
                showContent()
            }
        } catch (e: IOException) {
            Log.e("HomeActivity", "IOException, you might not have internet connection", e)
            showContent()
        } catch (e: HttpException) {
            Log.e("HomeActivity", "HttpException, unexpected response", e)
            showContent()
        }
    }

    private fun checkUser() {
        lifecycleScope.launch {
            getFirebaseIdToken()?.let { token ->
                checkUserHelper("Bearer $token")
            } ?: Log.e("HomeActivity", "Failed to get Firebase ID token")
        }
    }
}