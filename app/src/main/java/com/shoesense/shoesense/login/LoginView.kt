package com.shoesense.shoesense.login

interface LoginView {
    fun showEmailError(msg: String)
    fun showPasswordError(msg: String)
    fun showLoginSuccess()
    fun navigateToDashboard()
    fun fillUserData(email: String, password: String)
}
