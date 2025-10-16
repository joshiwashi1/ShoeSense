package com.shoesense.shoesense.settings

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.appcompat.app.AppCompatActivity
import com.shoesense.shoesense.R
import com.shoesense.shoesense.Repository.BottomNavbar
import com.shoesense.shoesense.home.HomeDashboardActivity

class SettingsActivity : AppCompatActivity(), SettingsView {

    private lateinit var myAccountLayout: LinearLayout
    private lateinit var changePasswordLayout: LinearLayout
    private lateinit var manageShelfLayout: LinearLayout
    private lateinit var helpLayout: LinearLayout
    private lateinit var notificationLayout: LinearLayout
    private lateinit var signOutLayout: LinearLayout
    private lateinit var switchNotifications: SwitchMaterial

    private lateinit var presenter: SettingsPresenter
    private var isUserTogglingSwitch = false  // avoid feedback loop

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // --- Bottom nav hookup (must be AFTER setContentView) ---
        BottomNavbar.attach(
            activity = this,
            defaultSelected = BottomNavbar.Item.SETTINGS,
            callbacks = BottomNavbar.Callbacks(
                onHome = {
                    // Navigate back to Home
                    startActivity(Intent(this, HomeDashboardActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                },
                onAnalytics = {
                    // startActivity(Intent(this, AnalyticsActivity::class.java))
                },
                onNotifications = {
                    // startActivity(Intent(this, NotificationsActivity::class.java))
                },
                onSettings = { /* already here */ }
            ),
            // unselectedAlpha is fine; or pass tints if you prefer colorful icons
            unselectedAlpha = 0.45f
        )

        // --- Bind views ---
        myAccountLayout = findViewById(R.id.myaccountLayout)
        changePasswordLayout = findViewById(R.id.changePasswordLayout)
        manageShelfLayout = findViewById(R.id.manageshelfLayout)
        helpLayout = findViewById(R.id.helpLayout)
        notificationLayout = findViewById(R.id.notificationLayout)
        signOutLayout = findViewById(R.id.signoutLayout)
        switchNotifications = findViewById(R.id.switchNotifications)

        // --- Presenter ---
        presenter = SettingsPresenter(this /* context */, this /* view */)

        // Initialize switch from stored preference
        setNotificationEnabled(presenter.isNotificationsEnabled())

        // --- Clicks ---
        myAccountLayout.setOnClickListener { navigateToMyAccount() }
        changePasswordLayout.setOnClickListener { navigateToChangePassword() }
        manageShelfLayout.setOnClickListener { navigateToManageShelf() }
        helpLayout.setOnClickListener { navigateToHelp() }
        notificationLayout.setOnClickListener {
            // Tap on row toggles the switch too
            switchNotifications.toggle()
        }
        signOutLayout.setOnClickListener { signOut() }

        // Switch change
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (!isUserTogglingSwitch) {
                presenter.setNotificationsEnabled(isChecked)
                showMessage(if (isChecked) "Notifications enabled" else "Notifications disabled")
            }
        }
    }

    // --- SettingsView implementation ---
    override fun setNotificationEnabled(enabled: Boolean) {
        // Prevent feedback loop when setting programmatically
        isUserTogglingSwitch = true
        switchNotifications.isChecked = enabled
        isUserTogglingSwitch = false
    }

    override fun navigateToMyAccount() {
        // startActivity(Intent(this, MyAccountActivity::class.java))
    }

    override fun navigateToChangePassword() {
        // startActivity(Intent(this, ChangePasswordActivity::class.java))
    }

    override fun navigateToManageShelf() {
        // startActivity(Intent(this, ManageShelfActivity::class.java))
    }

    override fun navigateToHelp() {
        // startActivity(Intent(this, HelpActivity::class.java))
    }

    override fun signOut() {
        // TODO: clear session/shared prefs and go to login
        // val intent = Intent(this, LoginActivity::class.java)
        // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        // startActivity(intent)
        // finish()
    }

    override fun showMessage(msg: String) {
        // You can use Toast/Snackbar; keeping it simple:
        // Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
