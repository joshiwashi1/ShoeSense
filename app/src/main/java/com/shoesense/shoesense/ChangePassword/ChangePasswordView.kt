package com.shoesense.shoesense.ChangePassword

interface ChangePasswordView {
    interface View {
        fun showError(message: String)
        fun showSuccess(message: String)
    }
    interface Presenter {
        fun changePassword(oldPassword: String, newPassword: String, confirmPassword: String)
    }
}
