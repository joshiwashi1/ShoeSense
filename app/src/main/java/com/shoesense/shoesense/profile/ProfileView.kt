package com.shoesense.shoesense.profile

interface ProfileView {
    interface View {
        fun showProfile(name: String, email: String, profileUrl: String?)
        fun showErrorMessage(msg: String)
        fun showLoading(isLoading: Boolean)
    }
    interface Presenter {
        fun fetchUserProfile(email: String)
        fun detach()
    }
}
