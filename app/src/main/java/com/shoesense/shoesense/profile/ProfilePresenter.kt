package com.shoesense.shoesense.profile

import com.google.firebase.database.*

class ProfilePresenter(private var view: ProfileView.View?) : ProfileView.Presenter {

    private val db: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child("users")

    override fun fetchUserProfile(email: String) {
        view?.showLoading(true)

        // Assuming RTDB structure:
        // users/{uid or randomKey} = { "name": "...", "email": "...", "photoUrl": "..." }
        db.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    view?.showLoading(false)
                    if (!snapshot.hasChildren()) return

                    // Get first match
                    val first = snapshot.children.first()
                    val name = first.child("name").getValue(String::class.java) ?: ""
                    val mail = first.child("email").getValue(String::class.java) ?: email
                    val photo = first.child("photoUrl").getValue(String::class.java)

                    view?.showProfile(name, mail, photo)
                }

                override fun onCancelled(error: DatabaseError) {
                    view?.showLoading(false)
                    view?.showErrorMessage("Failed to load profile: ${error.message}")
                }
            })
    }

    override fun detach() {
        view = null
    }
}
