package com.shoesense.shoesense.changepassword

interface ChangePasswordView {
    interface View {
        fun showError(message: String)
        fun showSuccess(message: String)
    }
    interface Presenter {
        fun changePassword(oldPassword: String, newPassword: String, confirmPassword: String)
    }
}
