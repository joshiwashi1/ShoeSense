package com.shoesense.shoesense.UpdateProfile

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class UpdateProfilePresenter(private val view: UpdateProfileView) {

    private val auth = FirebaseAuth.getInstance()
    private val db: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child("users")
    private val storage = FirebaseStorage.getInstance().reference

    fun loadUserProfile() {
        val user = auth.currentUser ?: run {
            view.showMessage("No logged-in user found")
            return
        }

        val email = user.email
        if (email.isNullOrBlank()) {
            view.showMessage("No email found for current user")
            return
        }

        view.showLoading(true)

        // Look up user record by email in Realtime DB: users/{pushKey} with field "email"
        db.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    view.showLoading(false)

                    // Fallback values from FirebaseAuth
                    var name: String = user.displayName ?: ""
                    var photoUrl: String? = user.photoUrl?.toString()

                    if (snapshot.hasChildren()) {
                        val first = snapshot.children.first()
                        val nameFromDb = first.child("name").getValue(String::class.java)
                        val photoFromDb = first.child("photoUrl").getValue(String::class.java)

                        if (!nameFromDb.isNullOrBlank()) name = nameFromDb
                        if (!photoFromDb.isNullOrBlank()) photoUrl = photoFromDb
                    }

                    view.showProfile(name, photoUrl)
                }

                override fun onCancelled(error: DatabaseError) {
                    view.showLoading(false)
                    view.showMessage("Failed to load profile: ${error.message}")
                }
            })
    }

    fun updateUserProfile(name: String, imageUri: Uri?) {
        val user = auth.currentUser ?: run {
            view.showMessage("User not found")
            return
        }

        val email = user.email
        if (email.isNullOrBlank()) {
            view.showMessage("No email found for current user")
            return
        }

        view.showLoading(true)

        if (imageUri != null) {
            val imageRef = storage.child("profile_images/${user.uid}.jpg")

            imageRef.putFile(imageUri)
                .addOnSuccessListener {
                    imageRef.downloadUrl
                        .addOnSuccessListener { uri ->
                            saveUserData(email, name, uri.toString())
                        }
                        .addOnFailureListener { e ->
                            view.showLoading(false)
                            view.showMessage("Failed to get image URL: ${e.message}")
                        }
                }
                .addOnFailureListener {
                    view.showLoading(false)
                    view.showMessage("Image upload failed: ${it.message}")
                }
        } else {
            // No image change, just update name
            saveUserData(email, name, null)
        }
    }

    private fun saveUserData(email: String, name: String, photoUrl: String?) {
        // Find existing user node by email, or create one if missing
        db.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val targetRef: DatabaseReference = if (snapshot.hasChildren()) {
                        snapshot.children.first().ref
                    } else {
                        // create new node (key by push)
                        val key = db.push().key ?: email.replace(".", "_")
                        db.child(key)
                    }

                    val updates = mutableMapOf<String, Any>(
                        "email" to email,
                        "name" to name
                    )
                    if (photoUrl != null) {
                        updates["photoUrl"] = photoUrl
                    }

                    targetRef.updateChildren(updates)
                        .addOnSuccessListener {
                            view.showLoading(false)
                            view.showMessage("Profile updated successfully!")
                            view.onProfileUpdated()   // ← TRIGGER NAVIGATION
                        }
                        .addOnFailureListener { e ->
                            view.showLoading(false)
                            view.showMessage("Failed to update: ${e.message}")
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    view.showLoading(false)
                    view.showMessage("Failed to update: ${error.message}")
                }
            })
    }
}
