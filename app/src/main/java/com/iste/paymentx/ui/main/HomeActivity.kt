package com.iste.paymentx.ui.main

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
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
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private var userRecieved: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        hideContent()
        setupBiometricAuthentication()
        startBiometricAuthentication()
        setupAuth()
        setupUI()
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

    private fun setupBiometricAuthentication() {
        val executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val cryptoObject = result.cryptoObject
                    showContent()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    handleAuthenticationError(errorCode, errString)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        this@HomeActivity,
                        "Authentication failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verify Identity")
            .setSubtitle("Use your biometric credential or face to access the app")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.BIOMETRIC_WEAK or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
    }

    private fun handleAuthenticationError(errorCode: Int, errString: CharSequence) {
        when (errorCode) {
            BiometricPrompt.ERROR_NEGATIVE_BUTTON,
            BiometricPrompt.ERROR_USER_CANCELED -> {
                finish()
            }
            BiometricPrompt.ERROR_HW_NOT_PRESENT -> {
                Toast.makeText(
                    this,
                    "This device doesn't support biometric authentication",
                    Toast.LENGTH_LONG
                ).show()
                showContent()
            }
            else -> {
                Toast.makeText(
                    this,
                    "Authentication error: $errString",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun startBiometricAuthentication() {
        if (checkBiometricAvailability()) {
            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun checkBiometricAvailability(): Boolean {
        val biometricManager = BiometricManager.from(this)

        return when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                handleBiometricUnavailable("No biometric hardware")
                false
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                handleBiometricUnavailable("Biometric hardware unavailable")
                false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                promptBiometricEnrollment()
                false
            }
            else -> false
        }
    }

    private fun handleBiometricUnavailable(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        showContent()
    }

    private fun promptBiometricEnrollment() {
        val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
            putExtra(
                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.BIOMETRIC_WEAK or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
        }
        startActivityForResult(enrollIntent, BIOMETRIC_ENROLLMENT_REQUEST_CODE)
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
                startActivity(Intent(this, ScanId::class.java))
            } else {
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
                }
            } else {
                Log.e("HomeActivity", "Response not successful: ${response.code()} - ${response.message()}")
            }
        } catch (e: IOException) {
            Log.e("HomeActivity", "IOException, you might not have internet connection", e)
        } catch (e: HttpException) {
            Log.e("HomeActivity", "HttpException, unexpected response", e)
        }
    }

    private fun checkUser() {
        lifecycleScope.launch {
            getFirebaseIdToken()?.let { token ->
                checkUserHelper("Bearer $token")
            } ?: Log.e("HomeActivity", "Failed to get Firebase ID token")
        }
    }

    override fun onResume() {
        super.onResume()
        if (findViewById<TextView>(R.id.user_name_text).visibility != View.VISIBLE) {
            startBiometricAuthentication()
        }
    }

    companion object {
        private const val BIOMETRIC_ENROLLMENT_REQUEST_CODE = 100
    }
}