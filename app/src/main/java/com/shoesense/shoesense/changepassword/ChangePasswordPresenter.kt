package com.shoesense.shoesense.changepassword

class ChangePasswordPresenter(private val view: ChangePasswordView.View) : ChangePasswordView.Presenter {

    override fun changePassword(oldPassword: String, newPassword: String, confirmPassword: String) {
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            view.showError("Fill out the field properly.")
            return
        }

        if (newPassword != confirmPassword) {
            view.showError("Passwords do not match.")
            return
        }

        if (newPassword.length < 6) {
            view.showError("Password must be at least 6 characters.")
            return
        }

        // You can add more logic here (e.g., calling backend, etc.)
        view.showSuccess("Password Changed Successfully")
    }
}
