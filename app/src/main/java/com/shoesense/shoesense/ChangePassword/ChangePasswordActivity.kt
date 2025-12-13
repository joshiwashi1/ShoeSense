package com.shoesense.shoesense.ChangePassword

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.shoesense.shoesense.R
import com.shoesense.shoesense.settings.SettingsActivity

class ChangePasswordActivity : Activity(), ChangePasswordView.View {

    private lateinit var presenter: ChangePasswordView.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hide the AppCompat ActionBar (if your theme still shows one)
        actionBar?.hide()
        // Hide the STATUS BAR (not the nav bar)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_change_password)

        presenter = ChangePasswordPresenter(this)

        val oldPasswordEditText = findViewById<EditText>(R.id.oldPasswordEditText)
        val newPasswordEditText = findViewById<EditText>(R.id.newPasswordEditText)
        val confirmNewPasswordEditText = findViewById<EditText>(R.id.confirmNewPasswordEditText)
        val confirmChangeButton = findViewById<Button>(R.id.confirmChangeButton)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        // ✅ Back button closes the screen
        btnBack.setOnClickListener {
            finish()
        }

        // ✅ Confirm button triggers presenter logic
        confirmChangeButton.setOnClickListener {
            presenter.changePassword(
                oldPasswordEditText.text.toString(),
                newPasswordEditText.text.toString(),
                confirmNewPasswordEditText.text.toString()
            )
        }
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

        // Navigate back to Settings screen
        val intent = Intent(this, SettingsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
}
