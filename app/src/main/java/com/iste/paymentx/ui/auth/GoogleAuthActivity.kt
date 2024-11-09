package com.iste.paymentx.ui.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.iste.paymentx.R
import com.iste.paymentx.data.repository.AuthRepository
import com.iste.paymentx.utils.ViewModelFactory
import com.iste.paymentx.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.iste.paymentx.ui.main.HomeActivity
import com.google.android.material.tabs.TabLayoutMediator

class GoogleAuthActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val viewModel: AuthViewModel by viewModels { ViewModelFactory(AuthRepository(FirebaseAuth.getInstance())) }
    private lateinit var viewPager: ViewPager2
    private val images = listOf(R.drawable.store, R.drawable.store1, R.drawable.store2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check if already signed in before setting content view
        if (viewModel.isUserLoggedIn()) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            currentUser?.let {
                navigateToHome(it.displayName, it.email, it.uid)
                return  // Return early to prevent setting up the login UI
            }
        }

        setContentView(R.layout.activity_google_auth)
        setupViewPager()
        setupGoogleSignIn()

        // Observe the user's login status for new sign-ins
        viewModel.user.observe(this) { user ->
            user?.let { navigateToHome(it.displayName, it.email, it.uid) }
        }

        findViewById<Button>(R.id.login_button).setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun setupViewPager() {
        viewPager = findViewById(R.id.viewPager)
        val adapter = ImageSliderAdapter(images)
        viewPager.adapter = adapter

        // Reduce page transition sensitivity
        viewPager.apply {
            offscreenPageLimit = 1
            setPageTransformer { page, position ->
                page.alpha = 1 - kotlin.math.abs(position)
            }
        }

        // TabLayout configuration
        val tabLayout = findViewById<TabLayout>(R.id.dots_indicator)
        tabLayout.apply {
            tabGravity = TabLayout.GRAVITY_CENTER
            setSelectedTabIndicator(null)
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, _ -> }.attach()
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
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK  // Clear activity stack
        }
        startActivity(intent)
        finish()
    }
}