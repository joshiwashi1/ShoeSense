package com.shoesense.shoesense.ChangePassword

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordPresenter(private val view: ChangePasswordView.View) : ChangePasswordView.Presenter {

    private val auth = FirebaseAuth.getInstance()

    override fun changePassword(oldPassword: String, newPassword: String, confirmPassword: String) {
        val currentUser = auth.currentUser

        if (newPassword.isEmpty() || confirmPassword.isEmpty() || oldPassword.isEmpty()) {
            view.showError("Please fill out all fields.")
            return
        }

        if (newPassword != confirmPassword) {
            view.showError("Passwords do not match.")
            return
        }

        if (newPassword.length < 6) {
            view.showError("Password must be at least 6 characters.")
            return
        }

        if (currentUser == null) {
            view.showError("User not logged in.")
            return
        }

        val email = currentUser.email
        if (email == null) {
            view.showError("No email found for current user.")
            return
        }

        // Re-authenticate user before updating password
        val credential = EmailAuthProvider.getCredential(email, oldPassword)
        currentUser.reauthenticate(credential)
            .addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    // Update the password in Firebase Authentication
                    currentUser.updatePassword(newPassword)
                        .addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                view.showSuccess("Password changed successfully.")
                            } else {
                                view.showError("Failed to change password: ${updateTask.exception?.message}")
                            }
                        }
                } else {
                    view.showError("Old password is incorrect.")
                }
            }
    }
}
