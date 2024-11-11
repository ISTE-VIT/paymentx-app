package com.iste.paymentx.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.iste.paymentx.R
import com.iste.paymentx.ui.auth.GoogleAuthActivity
import com.iste.paymentx.ui.auth.ScanId

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

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

        findViewById<Button>(R.id.checkBut).setOnClickListener(){
            val intent = Intent(this,ScanId::class.java)
            startActivity(intent)
        }
    }
}