package com.shoesense.shoesense.login

import android.util.Patterns
import com.shoesense.shoesense.Repository.AuthRepository

class LoginPresenter(private val view: LoginView) {

    fun onLoginClicked(email: String, password: String) {
        // ==== basic validation ====
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

        // ==== secure login via Firebase Auth ====
        AuthRepository.validateLogin(email, password) { success, error ->
            if (success) {
                view.showLoginSuccess()
                view.navigateToDashboard()
            } else {
                view.showPasswordError(error ?: "Incorrect email or password.")
            }
        }
    }

    fun onUserDataReceived(email: String, password: String) {
        view.fillUserData(email, password)
    }
}
