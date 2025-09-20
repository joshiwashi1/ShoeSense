package com.shoesense.shoesense.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.shoesense.shoesense.HomeDashboard
import com.shoesense.shoesense.R
import com.shoesense.shoesense.forgotpassword.ForgotPasswordActivity
import com.shoesense.shoesense.register.RegisterActivity

class LoginActivity : Activity(), LoginView {

    private lateinit var loginEmail: EditText
    private lateinit var loginPassword: EditText
    private lateinit var presenter: LoginPresenter
    private lateinit var forgotPasswordText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginEmail = findViewById(R.id.email_edit_text)
        loginPassword = findViewById(R.id.password_edit_text)
        val loginBtn = findViewById<Button>(R.id.login_Button)
        val registerText = findViewById<TextView>(R.id.register_here)
        forgotPasswordText = findViewById(R.id.forgot_password)

        presenter = LoginPresenter(this)

        loginBtn.setOnClickListener {
            val email = loginEmail.text.toString().trim()
            val password = loginPassword.text.toString().trim()
            presenter.onLoginClicked(email, password)
        }

        registerText.setOnClickListener {
            Toast.makeText(this, "Opening Register Page", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        forgotPasswordText.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    override fun showEmailError(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun showPasswordError(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun showLoginSuccess() {
        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
    }

    override fun navigateToDashboard() {
        startActivity(Intent(this, HomeDashboard::class.java))
        finish()
    }

    override fun fillUserData(email: String, password: String) {
        loginEmail.setText(email)
        loginPassword.setText(password)
    }
}
