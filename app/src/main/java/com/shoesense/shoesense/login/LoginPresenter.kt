package com.shoesense.shoesense.login

import android.util.Patterns
import com.google.firebase.firestore.FirebaseFirestore

class LoginPresenter(private val view: LoginView) {

    private val db = FirebaseFirestore.getInstance()

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

        // ==== Firestore lookup ====
        db.collection("users")
            .whereEqualTo("email", email)
            .limit(1)
            .get()
            .addOnSuccessListener { qs ->
                if (qs.isEmpty) {
                    view.showPasswordError("Incorrect email or password.")
                    return@addOnSuccessListener
                }

                val doc = qs.documents.first()
                val storedPassword = doc.getString("password") ?: ""

                if (storedPassword == password) {
                    view.showLoginSuccess()
                    view.navigateToDashboard()
                } else {
                    view.showPasswordError("Incorrect email or password.")
                }
            }
            .addOnFailureListener { e ->
                // Network/permission/other error
                view.showPasswordError("Login failed: ${e.message}")
            }
    }
    fun onUserDataReceived(email: String, password: String) {
        view.fillUserData(email, password)
    }
}
