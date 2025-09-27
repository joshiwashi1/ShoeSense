package com.shoesense.shoesense.register

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import com.shoesense.shoesense.R
import com.shoesense.shoesense.login.LoginActivity

class RegisterActivity : Activity(), RegisterView.View {

    private lateinit var presenter: RegisterView.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        presenter = RegisterPresenter(this)

        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmPasswordEditText)
        val signUpButton = findViewById<Button>(R.id.signUpButton)
        val loginHereButton = findViewById<Button>(R.id.loginhereButton)

        signUpButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            presenter.onCreateAccountClicked(name, email, password, confirmPassword)
        }

        loginHereButton.setOnClickListener {
            // Navigation to login screen
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    override fun showNameError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showEmailError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showPasswordError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showConfirmPasswordError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showSuccessMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
