package com.iste.paymentx.ui.auth

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.NfcF
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.iste.paymentx.R

class ScanId : AppCompatActivity() {

    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var pendingIntent: PendingIntent
    private lateinit var intentFilter: IntentFilter
    private lateinit var or_text: TextView
    private lateinit var vibrator: Vibrator
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var submitButton: Button
    private var audioFocusRequest: AudioFocusRequest? = null
    private var audioManager: AudioManager? = null

    // store credientials
    private var userName: String? = null
    private var userEmail: String? = null
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scan_id)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Get the user information from intent
        userName = intent.getStringExtra("USER_NAME")
        userEmail = intent.getStringExtra("USER_EMAIL")
        userId = intent.getStringExtra("USER_ID")

        // Initialize TextView
        or_text = findViewById(R.id.or_text)
        
        submitButton = findViewById(R.id.submit_button)
        submitButton.setOnClickListener(){
            val intent = Intent(this, Display::class.java).apply {
                putExtra("USER_NAME", userName)
                putExtra("USER_EMAIL", userEmail)
                putExtra("USER_ID", userId)
            }
            startActivity(intent)
        }

        // Initialize NFC adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Check if NFC is available and enabled
        if (nfcAdapter == null || !nfcAdapter.isEnabled) {
            or_text.text = "NFC is not available or not enabled."
            return
        }

        // Configure PendingIntent for foreground dispatch
        pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, this::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        // Set up the intent filter to detect ALL NFC tags
        intentFilter = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
        }

        // Initialize the Vibrator
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Initialize the MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.beep_sound)
        // Set the audio attributes
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        mediaPlayer.setAudioAttributes(audioAttributes)

        // Initialize the AudioManager and AudioFocusRequest
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener { focusChange ->
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_GAIN -> {
                        // Resume playback
                        mediaPlayer.start()
                    }
                    AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                        // Pause playback
                        mediaPlayer.pause()
                    }
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                        // Lower the volume
                        mediaPlayer.setVolume(0.2f, 0.2f)
                    }
                }
            }
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        val filters = arrayOf(
            IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        )
        val techListsArray = arrayOf(arrayOf(NfcF::class.java.name))

        // Enable foreground dispatch
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, techListsArray)
    }


    override fun onPause() {
        super.onPause()
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcAdapter?.disableForegroundDispatch(this)
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("NFC", "New Intent Received: ${intent.action}")

        if (intent.action == NfcAdapter.ACTION_TAG_DISCOVERED ||
            intent.action == NfcAdapter.ACTION_TECH_DISCOVERED ||
            intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED) {

            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            tag?.let {
                displayUid(it)
            } ?: run {
                Log.e("NFC", "No tag data found")
                Toast.makeText(this, "No tag data found", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d("NFC", "NFC action not recognized: ${intent.action}")
        }
    }


    private fun displayUid(tag: Tag) {
        try {
            // Extract and format the UID from the Tag object
            val uidBytes = tag.id
            val uid = uidBytes.joinToString(":") { String.format("%02X", it) }
            Log.d("NFC", "Attempting to open Display activity with UID: $uid")

            // Play a beep sound
            playBeepSound()

            // Vibrate the device
            vibrator.vibrate(200)

            // Log the UID
            Log.d("NFC", "Card UID: $uid")

            // Show a toast for debugging
            Toast.makeText(this, "Card detected: $uid", Toast.LENGTH_SHORT).show()

            // Create an intent to start DisplayUidActivity and pass the CARD_UID
            val displayIntent = Intent(this, Display::class.java).apply {
                putExtra("CARD_UID", uid)
                putExtra("USER_NAME", userName)
                putExtra("USER_EMAIL", userEmail)
                putExtra("USER_ID", userId)
            }
            startActivity(displayIntent)
        } catch (e: Exception) {
            Log.e("NFC", "Error processing NFC tag: ${e.message}")
            Toast.makeText(this, "Error reading NFC tag", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playBeepSound() {
        val audioFocusResult = audioManager?.requestAudioFocus(audioFocusRequest!!)
        if (audioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mediaPlayer.start()
        }
    }
}