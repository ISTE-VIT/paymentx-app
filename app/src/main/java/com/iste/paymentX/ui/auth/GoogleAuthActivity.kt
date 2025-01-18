package com.iste.paymentX.ui.auth

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
import com.iste.paymentX.R
import com.iste.paymentX.data.model.RetrofitInstance
import com.iste.paymentX.data.repository.AuthRepository
import com.iste.paymentX.utils.ViewModelFactory
import com.iste.paymentX.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate

class GoogleAuthActivity : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private val viewModel: AuthViewModel by viewModels { ViewModelFactory(AuthRepository(FirebaseAuth.getInstance())) }
    private lateinit var viewPager: ViewPager2
    private val images = listOf(R.drawable.store, R.drawable.store1, R.drawable.store2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_google_auth)

        apiInitFunc()
        setupViewPager()
        setupGoogleSignIn()

        // Check login status after UI is set up
        if (viewModel.isUserLoggedIn()) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            currentUser?.let { user ->
                navigateToHome(user.displayName, user.email, user.uid)
            }
        }

        // Observe the user's login status for new sign-ins
        viewModel.user.observe(this) { user ->
            user?.let {
                navigateToHome(it.displayName, it.email, it.uid)
            }
        }

        findViewById<Button>(R.id.login_button).setOnClickListener {
            signInWithGoogle()
        }
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
        val intent = Intent(this, Biometric::class.java).apply {
            putExtra("USER_NAME", displayName)
            putExtra("USER_EMAIL", email)
            putExtra("USER_ID", uid)
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
}