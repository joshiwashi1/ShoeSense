package com.shoesense.shoesense

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class WelcomeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // Find views
        val signUpButton = findViewById<Button>(R.id.signUpButton)
        val loginHereText = findViewById<TextView>(R.id.loginHere)

        // Handle Sign Up button click
        signUpButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Handle Login Here click
        loginHereText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
