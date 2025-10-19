package com.shoesense.shoesense.forgotpassword

import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordPresenter(private val view: ForgotPasswordView) {

    private val auth = FirebaseAuth.getInstance()

    fun onBackClicked() {
        view.closeScreen()
    }

    fun onSubmitClicked(email: String) {
        if (email.isEmpty()) {
            view.showEmailError("Please enter your email address")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            view.showEmailError("Please enter a valid email address")
            return
        }

        // Start loading indicator
       // view.showLoading(true)

        // Send password reset email through Firebase
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                //view.showLoading(false)
                view.showResetLinkSent("A password reset link has been sent to your email.")
            }
            .addOnFailureListener { e: Exception ->
               // view.showLoading(false)
                view.showEmailError(e.message ?: "Something went wrong. Please try again.")
            }
    }
}
