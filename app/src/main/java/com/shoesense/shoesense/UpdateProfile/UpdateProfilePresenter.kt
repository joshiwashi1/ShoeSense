    package com.shoesense.shoesense.UpdateProfile

    import android.net.Uri
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.firestore.FirebaseFirestore
    import com.google.firebase.storage.FirebaseStorage

    class UpdateProfilePresenter(private val view: UpdateProfileView) {

        private val auth = FirebaseAuth.getInstance()
        private val db = FirebaseFirestore.getInstance()
        private val storage = FirebaseStorage.getInstance().reference

        fun loadUserProfile() {
            val user = auth.currentUser ?: return view.showMessage("No logged-in user found")

            view.showLoading(true)

            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    view.showLoading(false)
                    if (doc.exists()) {
                        val username = doc.getString("username") ?: ""
                        val name = doc.getString("name") ?: ""
                        val email = doc.getString("email") ?: user.email ?: ""
                        val photoUrl = doc.getString("photoUrl")
                        view.showProfile(username, name, email, photoUrl)
                    } else {
                        view.showProfile("", user.displayName ?: "", user.email ?: "", user.photoUrl?.toString())
                    }
                }
                .addOnFailureListener {
                    view.showLoading(false)
                    view.showMessage("Failed to load profile: ${it.message}")
                }
        }

        fun updateUserProfile(username: String, name: String, email: String, imageUri: Uri?) {
            val user = auth.currentUser ?: return view.showMessage("User not found")
            view.showLoading(true)

            if (imageUri != null) {
                val imageRef = storage.child("profile_images/${user.uid}.jpg")

                // Upload new image first
                imageRef.putFile(imageUri)
                    .addOnSuccessListener {
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            saveUserData(user.uid, username, name, email, uri.toString())
                        }
                    }
                    .addOnFailureListener {
                        view.showLoading(false)
                        view.showMessage("Image upload failed: ${it.message}")
                    }
            } else {
                // No image change, just update data
                saveUserData(user.uid, username, name, email, null)
            }
        }

        private fun saveUserData(uid: String, username: String, name: String, email: String, photoUrl: String?) {
            val userMap = mutableMapOf<String, Any>(
                "username" to username,
                "name" to name,
                "email" to email
            )

            if (photoUrl != null) userMap["photoUrl"] = photoUrl

            db.collection("users").document(uid)
                .set(userMap)
                .addOnSuccessListener {
                    view.showLoading(false)
                    view.showMessage("Profile updated successfully!")
                }
                .addOnFailureListener {
                    view.showLoading(false)
                    view.showMessage("Failed to update: ${it.message}")
                }
        }
    }
