package com.shoesense.shoesense.register

interface RegisterView {

    interface View {
        fun showNameError(message: String)
        fun showEmailError(message: String)
        fun showPasswordError(message: String)
        fun showConfirmPasswordError(message: String)
        fun showSuccessMessage(message: String)
    }

    interface Presenter {
        fun onCreateAccountClicked(name: String, email: String, password: String, confirmPassword: String)
    }
}
