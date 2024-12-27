package com.iste.paymentx.ui.auth

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.iste.paymentx.R
import com.iste.paymentx.data.model.RetrofitInstance
import com.iste.paymentx.data.repository.AuthRepository
import com.iste.paymentx.ui.main.HomeActivity
import com.iste.paymentx.utils.ViewModelFactory
import com.iste.paymentx.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import android.view.ViewGroup

class GoogleAuthActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val viewModel: AuthViewModel by viewModels { ViewModelFactory(AuthRepository(FirebaseAuth.getInstance())) }
    private lateinit var viewPager: ViewPager2
    private val images = listOf(R.drawable.store, R.drawable.store1, R.drawable.store2)
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private var onSuccessfulAuthentication: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiInitFunc()
        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setupBiometricAuthentication()

        // Check if already signed in before setting content view
        if (viewModel.isUserLoggedIn()) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            currentUser?.let {
                startBiometricAuthentication {
                    navigateToHome(it.displayName, it.email, it.uid)
                }
                return
            }
        }

        setContentView(R.layout.activity_google_auth)
        setupViewPager()
        setupGoogleSignIn()

        // Observe the user's login status for new sign-ins
        viewModel.user.observe(this) { user ->
            user?.let {
                startBiometricAuthentication {
                    navigateToHome(it.displayName, it.email, it.uid)
                }
            }
        }

        findViewById<Button>(R.id.login_button).setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun setupBiometricAuthentication() {
        val executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccessfulAuthentication?.invoke()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    handleAuthenticationError(errorCode, errString)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        this@GoogleAuthActivity,
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
                onSuccessfulAuthentication?.invoke()
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

    private fun startBiometricAuthentication(onSuccess: () -> Unit) {
        onSuccessfulAuthentication = onSuccess
        if (checkBiometricAvailability()) {
            biometricPrompt.authenticate(promptInfo)
        } else {
            onSuccess()
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
        onSuccessfulAuthentication?.invoke()
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

    private fun setupViewPager() {
        viewPager = findViewById(R.id.viewPager)
        val adapter = ImageSliderAdapter(images)
        viewPager.adapter = adapter

        viewPager.apply {
            offscreenPageLimit = 1
            setPageTransformer { page, position ->
                page.alpha = 1 - kotlin.math.abs(position)
            }
        }

        val tabLayout = findViewById<TabLayout>(R.id.dots_indicator)
        tabLayout.apply {
            tabGravity = TabLayout.GRAVITY_CENTER
            setSelectedTabIndicator(null)
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, _ -> }.attach()

        for (i in 0 until tabLayout.tabCount) {
            val tab = (tabLayout.getChildAt(0) as ViewGroup).getChildAt(i)
            val params = tab.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(8, 0, 8, 0)
            tab.requestLayout()
        }
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .setHostedDomain("vitstudent.ac.in")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            account?.let { viewModel.signInWithGoogle(it.idToken ?: "") }
        } catch (e: ApiException) {
            Log.e("GoogleAuth", "Google sign-in failed", e)
        }
    }

    private fun navigateToHome(displayName: String?, email: String?, uid: String) {
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("USER_NAME", displayName)
            putExtra("USER_EMAIL", email)
            putExtra("USER_ID", uid)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private suspend fun apiInitHelper() {
        try {
            val response = RetrofitInstance.api.init()
            if (response.isSuccessful && response.body() != null) {
                Log.i("GoogleAuthActivity", "Api Set Successfully")
            } else {
                Log.e("GoogleAuthActivity", "Response not successful: ${response.code()} - ${response.message()}")
            }
        } catch (e: IOException) {
            Log.e("GoogleAuthActivity", "IOException, you might not have internet connection", e)
        } catch (e: HttpException) {
            Log.e("GoogleAuthActivity", "HttpException, unexpected response", e)
        }
    }

    private fun apiInitFunc() {
        lifecycleScope.launch {
            apiInitHelper()
        }
    }

    companion object {
        private const val BIOMETRIC_ENROLLMENT_REQUEST_CODE = 100
    }
}