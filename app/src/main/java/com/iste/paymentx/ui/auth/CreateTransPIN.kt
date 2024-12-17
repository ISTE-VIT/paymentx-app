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

class CreateTransPIN : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_trans_pin)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val pinInputs = listOf(
            findViewById<EditText>(R.id.uid_input1),
            findViewById<EditText>(R.id.uid_input2),
            findViewById<EditText>(R.id.uid_input3),
            findViewById<EditText>(R.id.uid_input4),
            findViewById<EditText>(R.id.uid_input5),
            findViewById<EditText>(R.id.uid_input6)
        )

        pinInputs.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1) {
                        // Move to the next EditText
                        if (index < pinInputs.size - 1) {
                            pinInputs[index + 1].requestFocus()
                        }
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            editText.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    // Clear current EditText and move to the previous one
                    if (editText.text.isEmpty() && index > 0) {
                        pinInputs[index - 1].requestFocus()
                        pinInputs[index - 1].text.clear()
                    }
                }
                false
            }
        }

        // Set up the "VERIFY" button to navigate to ConfirmPIN activity
        val verifyButton = findViewById<Button>(R.id.btnVerify)
        verifyButton.setOnClickListener {
            // Ensure all inputs are filled
            val pin = pinInputs.joinToString("") { it.text.toString() }
            if (pin.length == pinInputs.size) {
                val intent = Intent(this, ConfirmPIN::class.java)
                intent.putExtra("transactionPin", pin) // Pass the PIN to the next activity
                startActivity(intent)
            } else {
                pinInputs.first().requestFocus() // Focus on the first input if validation fails
            }
        }
    }
}
