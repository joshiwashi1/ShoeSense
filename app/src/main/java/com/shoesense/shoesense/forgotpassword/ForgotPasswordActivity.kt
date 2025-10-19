package com.shoesense.shoesense.forgotpassword

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.shoesense.shoesense.R

class ForgotPasswordActivity : Activity(), ForgotPasswordView {

    private lateinit var backButton: ImageButton
    private lateinit var emailEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var presenter: ForgotPasswordPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Initialize views
        backButton = findViewById(R.id.backButton)
        emailEditText = findViewById(R.id.emailEditText)
        submitButton = findViewById(R.id.submitButton)

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


    // --- Callbacks from Presenter ---
    override fun closeScreen() {
        finish()
    }

    override fun showEmailError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showResetLinkSent(email: String) {
        Toast.makeText(this, "Password reset link sent to $email", Toast.LENGTH_LONG).show()
    }
}
