package com.shoesense.shoesense.register

import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.shoesense.shoesense.Repository.AppConfig

class RegisterPresenter(private val view: RegisterView.View) : RegisterView.Presenter {

    private val auth = FirebaseAuth.getInstance()
    private val rtdb = FirebaseDatabase.getInstance().reference

    override fun onCreateAccountClicked(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        siteCode: String
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

        if (siteCode.isBlank()) {
            view.showErrorMessage("Enter your site code.")
            return
        }

        // ✅ Normalize to lowercase — all site IDs stored this way
        val siteId = siteCode.trim().lowercase()

        // === Check if site exists before proceeding ===
        view.showLoading(true)
        rtdb.child("sites").child(siteId).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    view.showLoading(false)
                    view.showErrorMessage("Site '$siteId' does not exist. Please enter a valid site code.")
                    return@addOnSuccessListener
                }

                // Site exists — proceed to create Auth user
                createAccountAndJoinSite(name, email, password, siteId)
            }
            .addOnFailureListener { e ->
                view.showLoading(false)
                view.showErrorMessage("Unable to verify site: ${e.message}")
            }
    }

    private fun createAccountAndJoinSite(
        name: String,
        email: String,
        password: String,
        siteId: String
    ) {
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

                // === SAVE PROFILE IN DATABASE ===
                val now = System.currentTimeMillis()
                val userDoc = mapOf(
                    "uid" to uid,
                    "name" to name,
                    "email" to email,
                    "createdAt" to now
                )

                rtdb.child("users").child(uid)
                    .setValue(userDoc)
                    .addOnSuccessListener {
                        // === JOIN EXISTING SITE (all lowercase) ===
                        val updates = hashMapOf<String, Any>(
                            "users/$uid/sites/$siteId" to "member",
                            "siteMembers/$siteId/$uid" to mapOf(
                                "role" to AppConfig.DEFAULT_ROLE,
                                "joinedAt" to now
                            )
                        )
                        rtdb.updateChildren(updates)
                            .addOnSuccessListener {
                                // Save globally for later
                                AppConfig.siteId = siteId
                                view.showLoading(false)
                                view.showSuccessMessage("Sign-up successful! Joined site: $siteId")
                                view.navigateToLogin()
                            }
                            .addOnFailureListener { e ->
                                view.showLoading(false)
                                view.showErrorMessage("Failed to join site: ${e.message}")
                            }
                    }
                    .addOnFailureListener { e ->
                        view.showLoading(false)
                        view.showErrorMessage("Failed to save profile: ${e.message}")
                    }
            }
    }
}
