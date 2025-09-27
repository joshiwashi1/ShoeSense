package com.shoesense.shoesense.register

import com.shoesense.shoesense.Model.AuthRepository

class RegisterPresenter(private val view: RegisterView.View) : RegisterView.Presenter {

    override fun onCreateAccountClicked(name: String, email: String, password: String, confirmPassword: String) {
        // Validate Name
        if (name.isEmpty()) {
            view.showNameError("Name cannot be empty")
            return
        }

        // Validate Email
        if (email.isEmpty() || !isValidEmail(email)) {
            view.showEmailError("Enter a valid email address")
            return
        }

        // Validate Password
        if (password.isEmpty()) {
            view.showPasswordError("Password cannot be empty")
            return
        } else if (password.length < 6) {
            view.showPasswordError("Password must be at least 6 characters")
            return
        }

        // Validate Confirm Password
        if (confirmPassword.isEmpty()) {
            view.showConfirmPasswordError("Confirm Password cannot be empty")
            return
        } else if (confirmPassword != password) {
            view.showConfirmPasswordError("Passwords do not match")
            return
        }

        // Try to register the user
        val success = AuthRepository.registerUser(name, email, password)
        if (success) {
            view.showSuccessMessage("Sign-Up Successful!")
        } else {
            view.showEmailError("An account with this email already exists.")
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
