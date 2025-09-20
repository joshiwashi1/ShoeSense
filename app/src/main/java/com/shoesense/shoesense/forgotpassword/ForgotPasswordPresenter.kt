package com.shoesense.shoesense.forgotpassword

class ForgotPasswordPresenter(private val view: ForgotPasswordView) {

    fun onBackClicked() {
        view.closeScreen()
    }

    fun onSubmitClicked(email: String) {
        if (email.isEmpty()) {
            view.showEmailError("Please enter your email address")
            return
        }

        // TODO: Hook to Firebase or API later
        view.showResetLinkSent(email)
    }
}
