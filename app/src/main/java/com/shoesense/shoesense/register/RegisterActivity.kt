// app/src/main/java/com/shoesense/shoesense/register/RegisterActivity.kt
package com.shoesense.shoesense.register

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import com.shoesense.shoesense.R
import com.shoesense.shoesense.login.LoginActivity

class RegisterActivity : Activity(), RegisterView.View {

    private lateinit var presenter: RegisterView.Presenter

    // Keep refs so we can enable/disable + show loading
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var loginHereButton: Button
    private var progressBar: ProgressBar? = null   // optional if not in XML

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        presenter = RegisterPresenter(this)

        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        signUpButton = findViewById(R.id.signUpButton)
        loginHereButton = findViewById(R.id.loginhereButton)
        // If your XML has a ProgressBar with this id, we'll use it. Otherwise it's just null-safe.
        progressBar = findViewById(R.id.progressBar)

        signUpButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()
            presenter.onCreateAccountClicked(name, email, password, confirmPassword)
        }

        loginHereButton.setOnClickListener {
            navigateToLogin()
        }
    }

    // ===== RegisterView.View implementation =====

    override fun showNameError(message: String) {
        nameEditText.error = message
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showEmailError(message: String) {
        emailEditText.error = message
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showPasswordError(message: String) {
        passwordEditText.error = message
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showConfirmPasswordError(message: String) {
        confirmPasswordEditText.error = message
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showSuccessMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        // Presenter will call navigateToLogin() separately; we avoid double navigation here.
    }

    override fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun showLoading(isLoading: Boolean) {
        progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        signUpButton.isEnabled = !isLoading
        loginHereButton.isEnabled = !isLoading
        nameEditText.isEnabled = !isLoading
        emailEditText.isEnabled = !isLoading
        passwordEditText.isEnabled = !isLoading
        confirmPasswordEditText.isEnabled = !isLoading
    }
}
