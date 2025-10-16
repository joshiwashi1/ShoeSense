package com.shoesense.shoesense.register

interface RegisterView {

    interface View {
        fun showNameError(msg: String)
        fun showEmailError(msg: String)
        fun showPasswordError(msg: String)
        fun showConfirmPasswordError(msg: String)
        fun showSuccessMessage(msg: String)
        fun showErrorMessage(msg: String)
        fun navigateToLogin()

        // 👇 add these 2 lines
        fun showLoading(isLoading: Boolean)
    }

    interface Presenter {
        fun onCreateAccountClicked(
            name: String,
            email: String,
            password: String,
            confirmPassword: String
        )
    }
}
