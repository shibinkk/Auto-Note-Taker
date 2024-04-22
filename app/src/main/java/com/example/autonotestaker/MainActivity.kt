package com.example.autonotestaker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE_PICK_AUDIO = 101
    private val PERMISSION_REQUEST_CODE = 102

    private var isAudioUploaded = false // Flag to track if an audio is uploaded

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnUpload = findViewById<Button>(R.id.btnUpload)
        btnUpload.isEnabled = true // Enable the upload button initially
    }

    fun onUploadClicked(view: android.view.View) {
        if (!isAudioUploaded) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                pickAudio()
            }
        }
    }

    private fun pickAudio() {
        val intent = Intent()
        intent.type = "audio/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Audio"), REQUEST_CODE_PICK_AUDIO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_AUDIO && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                displayAudio(uri)
                isAudioUploaded = true // Set the flag to true after uploading audio
                val btnUpload = findViewById<Button>(R.id.btnUpload)
                btnUpload.isEnabled = false // Disable the upload button
            }
        }
    }

    private fun displayAudio(uri: Uri) {
        val audioListLayout = findViewById<LinearLayout>(R.id.audioListLayout)

        // Get the file name from the URI
        val fileName = getFileName(uri)

        // Create a new TextView to display the name of the audio file
        val textView = TextView(this)
        textView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        textView.text = fileName
        textView.setBackgroundResource(R.drawable.bordered_layout_bg) // Set the background

        // Adjust padding and margin for the TextView
        textView.setPadding(24, 24, 24, 24) // Increased padding
        val marginParams = textView.layoutParams as LinearLayout.LayoutParams
        marginParams.setMargins(0, 16, 0, 16)
        textView.layoutParams = marginParams

        // Add the TextView to the LinearLayout
        audioListLayout.addView(textView)
    }


    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            applicationContext.contentResolver.query(uri, null, null, null, null)?.use {
                if (it.moveToFirst()) {
                    val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        result = it.getString(displayNameIndex)
                    }
                }
            }
        }
        return result ?: uri.path ?: ""
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickAudio()
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
