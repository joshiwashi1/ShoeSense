package com.shoesense.shoesense.Presenter

import android.util.Patterns
import com.shoesense.shoesense.LoginActivity
import com.shoesense.shoesense.Model.AuthRepository

class LoginPresenter(private val view: LoginActivity) {

    private val authRepository = AuthRepository

    fun onLoginClicked(email: String, password: String) {
        if (email.isBlank()) {
            view.showEmailError("Email cannot be empty.")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            view.showEmailError("Invalid email format.")
            return
        }

        if (password.isBlank()) {
            view.showPasswordError("Password cannot be empty.")
            return
        }

        val isValid = authRepository.validateLogin(email, password)
        if (isValid) {
            view.showLoginSuccess()
            view.navigateToDashboard()
        } else {
            view.showPasswordError("Incorrect email or password.")
        }
    }

    fun onUserDataReceived(email: String, password: String) {
        view.fillUserData(email, password)
    }
}

