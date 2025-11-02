package com.shoesense.shoesense.forgotpassword

import ForgotPasswordPresenter
import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.shoesense.shoesense.R
import com.google.firebase.FirebaseApp

class ForgotPasswordActivity : Activity(), ForgotPasswordView {

    private lateinit var backButton: ImageButton
    private lateinit var emailEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var presenter: ForgotPasswordPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hide the AppCompat ActionBar (if your theme still shows one)
        actionBar?.hide()
        // Hide the STATUS BAR (not the nav bar)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_forgot_password)

        // Initialize views
        backButton = findViewById(R.id.backButton)
        emailEditText = findViewById(R.id.emailEditText)
        submitButton = findViewById(R.id.submitButton)

        FirebaseApp.initializeApp(this)
        // Initialize presenter
        presenter = ForgotPasswordPresenter(this)

        // Back button → close this screen
        backButton.setOnClickListener {
            presenter.onBackClicked()
        }

        // Handle submit button
        submitButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            presenter.onSubmitClicked(email)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    // --- Callbacks from Presenter ---
    override fun closeScreen() {
        finish()
    }

    override fun showEmailError(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun showResetLinkSent(message: String) {
        runOnUiThread {
            // ✅ Success dialog for better UX
            AlertDialog.Builder(this)
                .setTitle("Password Reset Email Sent")
                .setMessage(
                    "$message\n\nPlease check your inbox or spam folder for the reset link."
                )
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    finish() // optional: close screen after acknowledging
                }
                .setCancelable(false)
                .show()
        }
    }
}
