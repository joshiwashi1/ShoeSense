package com.shoesense.shoesense.welcome

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.shoesense.shoesense.R
import com.shoesense.shoesense.login.LoginActivity
import com.shoesense.shoesense.register.RegisterActivity

class WelcomeActivity : Activity(), WelcomeView {

    private lateinit var presenter: WelcomePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // Init presenter
        presenter = WelcomePresenter(this)

        // Find views
        val signUpButton = findViewById<Button>(R.id.signUpButton)
        val loginHereText = findViewById<TextView>(R.id.loginHere)

        // Handle Sign Up button click
        signUpButton.setOnClickListener {
            presenter.onSignUpClicked()
        }

        // Handle Login Here click
        loginHereText.setOnClickListener {
            presenter.onLoginClicked()
        }
    }

    // ===== Implementing WelcomeView interface =====
    override fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    override fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}
