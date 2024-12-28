package com.iste.paymentx.ui.merchant

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.iste.paymentx.R

class MerchantOtpVerification : AppCompatActivity() {
    private lateinit var backarrow: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_merchant_otp_verification)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

//        val phoneNumber = intent.getStringExtra("phoneNumber")
//        val backarrow = findViewById<ImageView>(R.id.merchback)
//
//        // Add this line to update the TextView
//        val otpText = findViewById<TextView>(R.id.merchenter_otp_text)
//        otpText.text = getString(R.string.otp_message, phoneNumber)
//
//        val inputs = listOf(
//            findViewById<EditText>(R.id.merchuid_input1),
//            findViewById<EditText>(R.id.merchuid_input2),
//            findViewById<EditText>(R.id.merchuid_input3),
//            findViewById<EditText>(R.id.merchuid_input4),
//            findViewById<EditText>(R.id.merchuid_input5)
//        )
////        setUpOtpInputs(inputs)
    }
}