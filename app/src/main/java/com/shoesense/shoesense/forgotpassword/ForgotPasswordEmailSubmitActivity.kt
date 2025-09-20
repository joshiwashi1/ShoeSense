package com.shoesense.shoesense.forgotpassword

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import com.shoesense.shoesense.R

class ForgotPasswordEmailSubmitActivity : Activity(), ForgotPasswordEmailSubmitView {

    private lateinit var backButton: ImageButton
    private lateinit var resendButton: Button
    private lateinit var presenter: ForgotPasswordEmailSubmitPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password_email_submit)

        // Init views
        backButton = findViewById(R.id.backButton)
        resendButton = findViewById(R.id.resendButton)

        // Init presenter
        presenter = ForgotPasswordEmailSubmitPresenter(this)

        // Back button
        backButton.setOnClickListener {
            presenter.onBackClicked()
        }

        // Resend email
        resendButton.setOnClickListener {
            presenter.onResendClicked()
        }
    }

    // --- Callbacks from Presenter ---
    override fun closeScreen() {
        finish()
    }

    override fun showResendMessage() {
        Toast.makeText(this, "Resent password reset email", Toast.LENGTH_SHORT).show()
    }
}
