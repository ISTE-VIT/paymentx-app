package com.iste.paymentX.ui.main

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.Html
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.iste.paymentX.R

class TermsAndConditions : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_terms_and_conditions)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Find TextView for terms text
        val termsTextView = findViewById<TextView>(R.id.terms_text)

        // Set the formatted text with bold section titles
        val termsText = "<b>1. Introduction</b><br>" +
                "Welcome to <b>PaymentX</b>, a secure NFC-based offline payment application developed exclusively for VIT students. By using our app, you agree to abide by the following terms and conditions. Please read them carefully.<br><br>" +

                "<b>2. Acceptance of Terms</b><br>" +
                "By accessing and using PaymentX, you accept and agree to be bound by these terms and conditions. If you do not agree with any part of these terms, you should not use the app.<br><br>" +

                "<b>3. User Responsibilities</b><br>" +
                "- <b>VIT Email Registration:</b> Only students with a valid VIT email ID are allowed to register.<br>" +
                "- <b>PIN Security:</b> You are responsible for keeping your transaction PIN confidential.<br>" +
                "- <b>Device & ID Card Usage:</b> The app must be used only on supported Android devices with NFC enabled and a valid VIT-issued ID card.<br>" +
                "- <b>Respectful Use:</b> Users must not misuse the platform, attempt to exploit vulnerabilities, or impersonate other users.<br><br>" +

                "<b>4. Wallet & Transactions</b><br>" +
                "- <b>Offline NFC Payments:</b> Transactions can be made offline using your ID card. Ensure NFC is turned on.<br>" +
                "- <b>Top-Up and Withdrawals:</b> These actions require an internet connection and can be done through the app's respective options.<br>" +
                "- <b>Wallet Binding:</b> Each wallet is uniquely bound to your ID card. Do not attempt to alter or spoof this binding.<br><br>" +

                "<b>5. App Usage</b><br>" +
                "- <b>Dummy Currency:</b> PaymentX uses simulated currency for educational purposes. No real money is involved.<br>" +
                "- <b>Android-Only Support:</b> The app currently supports Android devices only. iOS and other platforms are not supported.<br>" +
                "- <b>PIN-Protected Transactions:</b> All payment actions require a secure PIN for completion.<br><br>" +

                "<b>6. Privacy Policy</b><br>" +
                "We value your privacy. The app uses Firebase Authentication for user verification. Transactional and wallet data are securely stored in our backend (Node.js & MongoDB). We do not store passwords or sensitive information directly. Please refer to our Privacy Policy for more details.<br><br>" +

                "<b>7. Limitation of Liability</b><br>" +
                "PaymentX is provided on an \"as-is\" basis. We do not guarantee uninterrupted functionality or that all features will always work as expected. We are not liable for any damages arising from misuse, data loss, or security breaches due to user negligence.<br><br>" +

                "<b>8. Changes to Terms</b><br>" +
                "We reserve the right to update or modify these terms at any time. Any changes will be posted within the app, and continued use constitutes acceptance of the new terms.<br><br>" +

                "<b>9. Termination</b><br>" +
                "We reserve the right to suspend or terminate your access to PaymentX without notice if you violate these terms or engage in fraudulent, harmful, or unauthorized activities.<br><br>" +

                "<b>10. Governing Law</b><br>" +
                "These terms and conditions are governed by the laws of the jurisdiction in which VIT is located. Any disputes arising from the use of the app will be resolved under these laws."

        // Set the HTML formatted text
        termsTextView.text = Html.fromHtml(termsText, Html.FROM_HTML_MODE_COMPACT)

        // Setup back button
        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener {
            onBackPressed()
        }
    }
}