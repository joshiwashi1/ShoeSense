package com.shoesense.shoesense.settings

import android.app.Activity
import android.os.Bundle
import android.widget.LinearLayout
import com.shoesense.shoesense.R

class SettingsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        var myAccountLayout: LinearLayout
        var changePasswordLayout: LinearLayout
        var manageShelfLayout: LinearLayout
        var helpLayout: LinearLayout
        var notificationLayout: LinearLayout
        var signOutLayout: LinearLayout
        // Initialize layouts
        myAccountLayout = findViewById(R.id.myaccountLayout)
        changePasswordLayout = findViewById(R.id.changePasswordLayout)
        manageShelfLayout = findViewById(R.id.manageshelfLayout)
        helpLayout = findViewById(R.id.helpLayout)
        notificationLayout = findViewById(R.id.notificationLayout)
        signOutLayout = findViewById(R.id.signoutLayout)

        // Set click listeners
        myAccountLayout.setOnClickListener {
            //startActivity(Intent(this, MyAccountActivity::class.java))
        }

        changePasswordLayout.setOnClickListener {
            //startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        manageShelfLayout.setOnClickListener {
            // startActivity(Intent(this, ManageShelfActivity::class.java))
        }

        helpLayout.setOnClickListener {
            // startActivity(Intent(this, HelpActivity::class.java))
        }

        notificationLayout.setOnClickListener {
            // startActivity(Intent(this, NotificationActivity::class.java))
        }

        signOutLayout.setOnClickListener {
            // Example: clear session or shared preferences
            // Then go back to login page
            // val intent = Intent(this, LoginActivity::class.java)
            // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // startActivity(intent)
            // finish()
        }

    }
}