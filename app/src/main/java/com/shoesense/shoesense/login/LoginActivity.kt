package com.shoesense.shoesense.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.shoesense.shoesense.BaseActivity
import com.shoesense.shoesense.R
import com.shoesense.shoesense.Repository.AuthRepository
import com.shoesense.shoesense.home.HomeDashboardActivity
import com.shoesense.shoesense.forgotpassword.ForgotPasswordActivity
import com.shoesense.shoesense.register.RegisterActivity

class LoginActivity : BaseActivity(), LoginView {

    private lateinit var loginEmail: EditText
    private lateinit var loginPassword: EditText
    private lateinit var presenter: LoginPresenter
    private lateinit var forgotPasswordText: TextView
    private lateinit var registerText: TextView
    private lateinit var loginBtn: Button
    private lateinit var btnLoadingRetry: Button
    private lateinit var btnNoNetworkRetry: Button
    private lateinit var btnIotRetry: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide ActionBar & StatusBar
        supportActionBar?.hide()
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)

        // Clear previous session
        AuthRepository.logout()

        setContentView(R.layout.activity_login)

        bindViews()
        setupListeners()

        // Initialize presenter
        presenter = LoginPresenter(this)

        // Wire retry button from BaseActivity overlay
        btnLoadingRetry.setOnClickListener { handleLogin() }
        btnNoNetworkRetry.setOnClickListener { handleLogin() }
        btnIotRetry.setOnClickListener { handleLogin() }
    }

    private fun bindViews() {
        loginEmail = findViewById(R.id.email_edit_text)
        loginPassword = findViewById(R.id.password_edit_text)
        loginBtn = findViewById(R.id.login_Button)
        registerText = findViewById(R.id.register_here)
        forgotPasswordText = findViewById(R.id.forgot_password)
    }

    private fun setupListeners() {
        loginBtn.setOnClickListener { handleLogin() }

        registerText.setOnClickListener {
            Toast.makeText(this, "Opening Register Page", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        forgotPasswordText.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun handleLogin() {
        val email = loginEmail.text.toString().trim()
        val password = loginPassword.text.toString().trim()

        if (email.isEmpty()) {
            showEmailError("Email is required")
            return
        }
        if (password.isEmpty()) {
            showPasswordError("Password is required")
            return
        }

        // Show loading overlay while authenticating
        showLoadingState()

        presenter.onLoginClicked(email, password)
    }

    // --- LoginView implementation ---

    override fun showEmailError(msg: String) {
        hideAllStates()
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun showPasswordError(msg: String) {
        hideAllStates()
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun showLoginSuccess() {
        hideAllStates()
        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
    }

    override fun navigateToDashboard() {
        hideAllStates()
        startActivity(Intent(this, HomeDashboardActivity::class.java))
        finish()
    }

    override fun fillUserData(email: String, password: String) {
        loginEmail.setText(email)
        loginPassword.setText(password)
    }
}
