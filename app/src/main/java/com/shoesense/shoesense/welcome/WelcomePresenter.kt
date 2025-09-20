package com.shoesense.shoesense.welcome

class WelcomePresenter(private val view: WelcomeView) {

    fun onSignUpClicked() {
        view.navigateToRegister()
    }

    fun onLoginClicked() {
        view.navigateToLogin()
    }
}
