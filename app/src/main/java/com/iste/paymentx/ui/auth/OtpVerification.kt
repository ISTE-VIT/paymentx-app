package com.iste.paymentx.ui.auth

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.iste.paymentx.R

class OtpVerification : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_otp_verification)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val inputs = listOf(
            findViewById<EditText>(R.id.uid_input1),
            findViewById<EditText>(R.id.uid_input2),
            findViewById<EditText>(R.id.uid_input3),
            findViewById<EditText>(R.id.uid_input4),
            findViewById<EditText>(R.id.uid_input5)
        )
        setUpOtpInputs(inputs)

        val btnVerify = findViewById<Button>(R.id.btnVerify)
        btnVerify.setOnClickListener {
            openCreateTransPINPage()
        }
    }

    private fun setUpOtpInputs(inputs: List<EditText>) {
        for (i in inputs.indices) {
            val current = inputs[i]
            val next = inputs.getOrNull(i + 1) // Get next input or null if last
            val previous = inputs.getOrNull(i - 1) // Get previous input or null if first

            current.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (!s.isNullOrEmpty()) {
                        next?.requestFocus() // Move to the next box
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            current.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (current.text.isEmpty()) {
                        previous?.apply {
                            requestFocus() // Move to the previous box
                            text.clear() // Clear the previous box
                        }
                    }
                }
                false
            }
        }
    }

    private fun openCreateTransPINPage() {
        val intent = Intent(this, CreateTransPIN::class.java)
        startActivity(intent)
    }
}
