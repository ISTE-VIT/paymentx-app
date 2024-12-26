package com.iste.paymentx.ui.main

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import java.io.IOException

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val api = RetrofitInstance.api
        setContentView(R.layout.activity_home)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Hide content initially
        hideContent()
        setupBiometricAuthentication()
        startBiometricAuthentication()
        setupAuth()
        setupUI()
    }

    private fun hideContent() {
        findViewById<View>(R.id.imageView).visibility = View.GONE
        findViewById<View>(R.id.user_name_text).visibility = View.GONE
        findViewById<View>(R.id.user_email_text).visibility = View.GONE
        findViewById<View>(R.id.user_id_text).visibility = View.GONE
        findViewById<View>(R.id.logout_button).visibility = View.GONE
        findViewById<View>(R.id.checkBut).visibility = View.GONE
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

        val userName = intent.getStringExtra("USER_NAME")
        val userEmail = intent.getStringExtra("USER_EMAIL")
        val userId = intent.getStringExtra("USER_ID")

        findViewById<TextView>(R.id.user_name_text).text = userName
        findViewById<TextView>(R.id.user_email_text).text = userEmail
        findViewById<TextView>(R.id.user_id_text).text = "User ID: $userId"

        setupButtons(userId, userName, userEmail)
    }

    private fun setupButtons(userId: String?, userName: String?, userEmail: String?) {
        findViewById<Button>(R.id.logout_button).setOnClickListener {
            handleLogout()
        }

        findViewById<Button>(R.id.checkBut).setOnClickListener {
            if (userId != null) {
                login(email = userEmail, displayName = userName, uid = userId, isMerchant = false)
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
                    // You can use the cryptoObject for additional security measures
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
                        BiometricManager.Authenticators.BIOMETRIC_WEAK or  // Added for face unlock
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
                showContent() // Fallback to show content
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
//        setContentView(R.layout.activity_biometric)
        if (checkBiometricAvailability()) {
            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun checkBiometricAvailability(): Boolean {
        val biometricManager = BiometricManager.from(this)

        return when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or  // Added for face unlock
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
                        BiometricManager.Authenticators.BIOMETRIC_WEAK or  // Added for face unlock
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
        findViewById<View>(R.id.imageView).visibility = View.VISIBLE
        findViewById<View>(R.id.user_name_text).visibility = View.VISIBLE
        findViewById<View>(R.id.user_email_text).visibility = View.VISIBLE
        findViewById<View>(R.id.user_id_text).visibility = View.VISIBLE
        findViewById<View>(R.id.logout_button).visibility = View.VISIBLE
        findViewById<View>(R.id.checkBut).visibility = View.VISIBLE
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
            val user = User(email = email, displayName = displayName, uid = uid, isMerchant = false)
            getFirebaseIdToken()?.let { token ->
                loginHelper("Bearer $token", user)
            } ?: Log.e("HomeActivity", "Failed to get Firebase ID token")
        }
    }

    override fun onResume() {
        super.onResume()
        if (findViewById<View>(R.id.imageView).visibility != View.VISIBLE) {
            startBiometricAuthentication()
        }
    }

    companion object {
        private const val BIOMETRIC_ENROLLMENT_REQUEST_CODE = 100
    }
}