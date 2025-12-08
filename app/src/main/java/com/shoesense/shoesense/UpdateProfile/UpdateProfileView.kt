package com.shoesense.shoesense.UpdateProfile

interface UpdateProfileView {
    fun showProfile(name: String, photoUrl: String?)
    fun showLoading(isLoading: Boolean)
    fun showMessage(message: String)
    fun onProfileUpdated()
}
