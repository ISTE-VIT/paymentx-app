package com.iste.paymentx.ui.auth

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.iste.paymentx.R
import com.iste.paymentx.ui.main.HomeActivity

class Biometric : AppCompatActivity() {
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var vibrator: Vibrator
    private lateinit var unlockText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_biometric)

        unlockText = findViewById(R.id.textView9)

        // Initialize vibrator
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Handle back button
        handleBackButton()

        // Setup click listener for unlock text
        unlockText.setOnClickListener {
            startBiometricAuthentication()
        }
        setupBiometricAuthentication()
        // Start biometric authentication immediately
        startBiometricAuthentication()
    }

    private fun handleBackButton() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing to prevent app from closing
                Toast.makeText(
                    this@Biometric,
                    "Please authenticate to proceed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setupBiometricAuthentication() {
        val executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // Get extras from intent
                    val userName = intent.getStringExtra("USER_NAME")
                    val userEmail = intent.getStringExtra("USER_EMAIL")
                    val userId = intent.getStringExtra("USER_ID")

                    navigateToHome(userName, userEmail, userId)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    when (errorCode) {
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_USER_CANCELED -> {
                            // Instead of finishing, show a message
                            Toast.makeText(
                                this@Biometric,
                                "Authentication cancelled. Tap Unlock to try again",
                                Toast.LENGTH_SHORT
                            ).show()
                            vibratePhone()
                        }
                        BiometricPrompt.ERROR_HW_NOT_PRESENT -> {
                            Toast.makeText(
                                this@Biometric,
                                "This device doesn't support biometric authentication",
                                Toast.LENGTH_LONG
                            ).show()
                            vibratePhone()
                            proceedWithoutBiometric()
                        }
                        else -> {
                            Toast.makeText(
                                this@Biometric,
                                "Authentication error: $errString",
                                Toast.LENGTH_SHORT
                            ).show()
                            vibratePhone()
                        }
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        this@Biometric,
                        "Authentication failed. Tap Unlock to try again",
                        Toast.LENGTH_SHORT
                    ).show()
                    vibratePhone()
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

    private fun startBiometricAuthentication() {
        if (checkBiometricAvailability()) {
            biometricPrompt.authenticate(promptInfo)
        } else {
            proceedWithoutBiometric()
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
        vibratePhone()
        proceedWithoutBiometric()
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

    private fun proceedWithoutBiometric() {
        val userName = intent.getStringExtra("USER_NAME")
        val userEmail = intent.getStringExtra("USER_EMAIL")
        val userId = intent.getStringExtra("USER_ID")
        navigateToHome(userName, userEmail, userId)
    }

    private fun navigateToHome(userName: String?, userEmail: String?, userId: String?) {
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("USER_NAME", userName)
            putExtra("USER_EMAIL", userEmail)
            putExtra("USER_ID", userId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    companion object {
        private const val BIOMETRIC_ENROLLMENT_REQUEST_CODE = 100
    }
}