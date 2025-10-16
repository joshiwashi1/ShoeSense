package com.shoesense.shoesense.register

import com.google.firebase.firestore.FirebaseFirestore
import com.shoesense.shoesense.Model.AuthRepository

class RegisterPresenter(private val view: RegisterView.View) : RegisterView.Presenter {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateAccountClicked(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        // === VALIDATION ===
        if (name.isEmpty()) {
            view.showNameError("Name cannot be empty")
            return
        }
        if (email.isEmpty() || !isValidEmail(email)) {
            view.showEmailError("Enter a valid email address")
            return
        }
        if (password.isEmpty()) {
            view.showPasswordError("Password cannot be empty")
            return
        } else if (password.length < 6) {
            view.showPasswordError("Password must be at least 6 characters")
            return
        }
        if (confirmPassword.isEmpty()) {
            view.showConfirmPasswordError("Confirm Password cannot be empty")
            return
        } else if (confirmPassword != password) {
            view.showConfirmPasswordError("Passwords do not match")
            return
        }

        // === CHECK IF EMAIL ALREADY EXISTS ===
        view.showLoading(true)
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    view.showLoading(false)
                    view.showEmailError("An account with this email already exists.")
                } else {
                    // === CREATE NEW USER DOCUMENT ===
                    val userData = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "password" to password, // ⚠️ plain text for now
                        "createdAt" to System.currentTimeMillis()
                    )

                    db.collection("users")
                        .add(userData)
                        .addOnSuccessListener {
                            view.showLoading(false)
                            view.showSuccessMessage("Sign-Up Successful!")
                            view.navigateToLogin()
                        }
                        .addOnFailureListener { e ->
                            view.showLoading(false)
                            view.showErrorMessage("Failed to save user: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                view.showLoading(false)
                view.showErrorMessage("Error checking email: ${e.message}")
            }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
