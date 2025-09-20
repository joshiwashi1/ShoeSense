package com.shoesense.shoesense.Presenter

import android.util.Patterns
import com.shoesense.shoesense.Model.AuthRepository
import com.shoesense.shoesense.RegisterActivity

class RegistrationPresenter(private val view: RegisterActivity) {

    private val authRepository = AuthRepository

    fun onRegisterClicked(name: String, email: String, password: String, confirmPassword: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            view.showError("Please fill out all fields.")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            view.showError("Invalid email format.")
            return
        }

        if (password.length < 6) {
            view.showError("Password must be at least 6 characters.")
            return
        }

        if (password != confirmPassword) {
            view.showError("Passwords do not match.")
            return
        }

        val success = authRepository.registerUser(name, email, password)
        if (success) {
            view.showSuccess("Registration successful!")
        } else {
            view.showError("User already exists.")
        }
    }
}

