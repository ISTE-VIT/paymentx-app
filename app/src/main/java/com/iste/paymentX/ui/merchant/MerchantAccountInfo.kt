package com.iste.paymentX.ui.merchant

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.iste.paymentX.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MerchantAccountInfo : AppCompatActivity() {
    private lateinit var imageCardView: CardView
    private lateinit var shopImageView: ImageView
    private var currentPhotoPath: String = ""
    private lateinit var confirmbtn: Button

    private lateinit var shopNameEditText: EditText
    private lateinit var shopkeeperNameEditText: EditText
    private lateinit var shopLocationEditText: EditText

    private var userName: String? = null
    private var userEmail: String? = null
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_merchant_account_info)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Get the user information from intent
        userName = intent.getStringExtra("USER_NAME")
        userEmail = intent.getStringExtra("USER_EMAIL")
        userId = intent.getStringExtra("USER_ID")

        shopNameEditText = findViewById(R.id.shop_name)
        shopkeeperNameEditText = findViewById(R.id.shopkeeper_name)
        shopLocationEditText = findViewById(R.id.shop_location)

        imageCardView = findViewById(R.id.card4)
        imageCardView.setOnClickListener {
            showImagePickerDialog()
        }

        confirmbtn = findViewById(R.id.merchconfirmButton)
        confirmbtn.setOnClickListener {
            val intent = Intent(this, MerchantMainScreen::class.java).apply {
                // Pass new shop details
                putExtra("SHOP_NAME", shopNameEditText.text.toString())
                putExtra("SHOPKEEPER_NAME", shopkeeperNameEditText.text.toString())
                putExtra("SHOP_LOCATION", shopLocationEditText.text.toString())

                // Pass existing user details
                putExtra("USER_NAME", userName)
                putExtra("USER_EMAIL", userEmail)
                putExtra("USER_ID", userId)

            }
            startActivity(intent)
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // Add ImageView to display the selected image
                if (!::shopImageView.isInitialized) {
                    shopImageView = ImageView(this)
                    imageCardView.addView(shopImageView)
                }
                shopImageView.setImageURI(uri)
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Add ImageView to display the captured image
            if (!::shopImageView.isInitialized) {
                shopImageView = ImageView(this)
                imageCardView.addView(shopImageView)
            }
            shopImageView.setImageURI(Uri.parse(currentPhotoPath))
        }
    }


    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")

        MaterialAlertDialogBuilder(this)
            .setTitle("Select Option")
            .setItems(options) { _, index ->
                when (index) {
                    0 -> checkCameraPermission()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        val photoFile = createImageFile()
        val photoURI = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.provider",
            photoFile
        )

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        }

        cameraLauncher.launch(takePictureIntent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(null)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }
}