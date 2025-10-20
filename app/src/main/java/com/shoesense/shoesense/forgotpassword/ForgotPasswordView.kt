package com.shoesense.shoesense.forgotpassword

interface ForgotPasswordView {
    fun closeScreen()
    fun showEmailError(message: String)
    fun showResetLinkSent(message: String)
}
