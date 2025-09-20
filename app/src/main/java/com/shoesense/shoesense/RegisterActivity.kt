package com.shoesense.shoesense

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import com.shoesense.shoesense.Presenter.RegistrationPresenter

class RegisterActivity : Activity() {

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var loginTextView: TextView
    private lateinit var signupButton: Button
    private lateinit var presenter: RegistrationPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize views
        nameEditText = findViewById(R.id.NameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        loginTextView = findViewById(R.id.loginTextview)
        signupButton = findViewById(R.id.signUpButton)

        // Initialize presenter
        presenter = RegistrationPresenter(this)

        // Handle sign-up button click
        signupButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            presenter.onRegisterClicked(name, email, password, confirmPassword)
        }

        // Handle login text click
        loginTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // Presenter callbacks
    fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
