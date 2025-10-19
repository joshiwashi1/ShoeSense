package com.shoesense.shoesense.profile

import com.google.firebase.firestore.FirebaseFirestore

class ProfilePresenter(private val view: ProfileView.View) : ProfileView.Presenter {

    private val db = FirebaseFirestore.getInstance()

    override fun fetchUserProfile(email: String) {
        view.showLoading(true)

        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                view.showLoading(false)

                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    val name = doc.getString("name") ?: "No Name"
                    val profileUrl = doc.getString("profileUrl")

                    view.showProfile(name, email, profileUrl)
                } else {
                    view.showErrorMessage("Profile not found.")
                }
            }
            .addOnFailureListener { e ->
                view.showLoading(false)
                view.showErrorMessage(e.message ?: "Failed to fetch profile.")
            }
    }
}
