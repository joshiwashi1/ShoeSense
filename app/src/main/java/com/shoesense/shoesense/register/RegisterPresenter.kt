package com.shoesense.shoesense.register

import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase

class RegisterPresenter(private val view: RegisterView.View) : RegisterView.Presenter {

    private val auth = FirebaseAuth.getInstance()
    private val rtdb = FirebaseDatabase.getInstance().reference

    override fun onCreateAccountClicked(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        // === VALIDATION ===
        if (name.isBlank()) { view.showNameError("Name cannot be empty"); return }
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            view.showEmailError("Enter a valid email address"); return
        }
        if (password.isBlank()) { view.showPasswordError("Password cannot be empty"); return }
        if (password.length < 6) { view.showPasswordError("Password must be at least 6 characters"); return }
        if (confirmPassword.isBlank()) { view.showConfirmPasswordError("Confirm Password cannot be empty"); return }
        if (confirmPassword != password) { view.showConfirmPasswordError("Passwords do not match"); return }

        // === CREATE AUTH ACCOUNT (enforces unique emails) ===
        view.showLoading(true)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    view.showLoading(false)
                    view.showErrorMessage(task.exception?.message ?: "Failed to create account")
                    return@addOnCompleteListener
                }

                val user = auth.currentUser
                val uid = user?.uid
                if (uid == null) {
                    view.showLoading(false)
                    view.showErrorMessage("User session not available after sign-up")
                    return@addOnCompleteListener
                }

                // Optional: set display name in Auth profile
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdates)

                // Optional: send verification email
                // user.sendEmailVerification()

                // === SAVE PUBLIC PROFILE IN REALTIME DATABASE (NO PASSWORD) ===
                val userDoc = mapOf(
                    "uid" to uid,
                    "name" to name,
                    "email" to email,
                    "createdAt" to System.currentTimeMillis()
                )

                rtdb.child("users").child(uid)
                    .setValue(userDoc)
                    .addOnSuccessListener {
                        view.showLoading(false)
                        view.showSuccessMessage("Sign-up successful!")
                        view.navigateToLogin()
                    }
                    .addOnFailureListener { e ->
                        // If you want to be strict, you could delete the just-created Auth user here.
                        // user.delete()
                        view.showLoading(false)
                        view.showErrorMessage("Failed to save profile: ${e.message}")
                    }
            }
    }
}
