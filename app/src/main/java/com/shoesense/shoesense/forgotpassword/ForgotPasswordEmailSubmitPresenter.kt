package com.shoesense.shoesense.forgotpassword

class ForgotPasswordEmailSubmitPresenter(private val view: ForgotPasswordEmailSubmitView) {

    fun onBackClicked() {
        view.closeScreen()
    }

    fun onResendClicked() {
        // TODO: hook to Firebase or backend for real resend logic
        view.showResendMessage()
    }
}
