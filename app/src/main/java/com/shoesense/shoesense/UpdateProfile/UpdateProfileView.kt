package com.shoesense.shoesense.UpdateProfile

interface UpdateProfileView {
    fun showProfile(username: String, name: String, email: String, photoUrl: String?)
    fun showLoading(isLoading: Boolean)
    fun showMessage(message: String)
}
