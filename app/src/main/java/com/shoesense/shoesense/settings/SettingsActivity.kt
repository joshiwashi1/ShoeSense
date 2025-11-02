package com.shoesense.shoesense.settings

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.button.MaterialButton
import com.shoesense.shoesense.ChangePassword.ChangePasswordActivity
import com.shoesense.shoesense.ManageShelf.ManageShelfActivity
import com.shoesense.shoesense.R
import com.shoesense.shoesense.Repository.AuthRepository
import com.shoesense.shoesense.Repository.BottomNavbar
import com.shoesense.shoesense.about.AboutActivity
import com.shoesense.shoesense.history.HistoryActivity
import com.shoesense.shoesense.notification.NotificationActivity
import com.shoesense.shoesense.home.HomeDashboardActivity
import com.shoesense.shoesense.login.LoginActivity
import com.shoesense.shoesense.profile.ProfileActivity

class SettingsActivity : AppCompatActivity(), SettingsView {

    private lateinit var myAccountLayout: LinearLayout
    private lateinit var changePasswordLayout: LinearLayout
    private lateinit var manageShelfLayout: LinearLayout
    private lateinit var aboutLayout: LinearLayout
    private lateinit var notificationLayout: LinearLayout
    private lateinit var signOutLayout: LinearLayout
    private lateinit var switchNotifications: SwitchMaterial

    private lateinit var presenter: SettingsPresenter
    private var isUserTogglingSwitch = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hide the AppCompat ActionBar (if your theme still shows one)
        actionBar?.hide()
        // Hide the STATUS BAR (not the nav bar)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_settings)

        BottomNavbar.attach(
            activity = this,
            defaultSelected = BottomNavbar.Item.SETTINGS,
            callbacks = BottomNavbar.Callbacks(
                onHome = {
                    startActivity(Intent(this, HomeDashboardActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                },
                onHistory = { navigateToHistory() },
                onNotifications = { navigateToNotifications() },
                onSettings = { /* already here */ }
            ),
            unselectedAlpha = 0.45f
        )

        // --- Bind views ---
        myAccountLayout = findViewById(R.id.myaccountLayout)
        changePasswordLayout = findViewById(R.id.changePasswordLayout)
        manageShelfLayout = findViewById(R.id.manageshelfLayout)
        aboutLayout = findViewById(R.id.aboutLayout)
        notificationLayout = findViewById(R.id.notificationLayout)
        signOutLayout = findViewById(R.id.signoutLayout)
        switchNotifications = findViewById(R.id.switchNotifications)

        presenter = SettingsPresenter(this, this)
        setNotificationEnabled(presenter.isNotificationsEnabled())

        // --- Clicks ---
        myAccountLayout.setOnClickListener { navigateToMyAccount() }
        changePasswordLayout.setOnClickListener { navigateToChangePassword() }
        manageShelfLayout.setOnClickListener { navigateToManageShelf() }
        aboutLayout.setOnClickListener { navigateToAbout() }

        // This row toggles the switch
        notificationLayout.setOnClickListener { switchNotifications.toggle() }

        // Show the custom sign-out confirmation dialog
        signOutLayout.setOnClickListener { showSignOutDialog() }

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (!isUserTogglingSwitch) {
                presenter.setNotificationsEnabled(isChecked)
                showMessage(if (isChecked) "Notifications enabled" else "Notifications disabled")
            }
        }
    }

    // --- SettingsView implementation ---
    override fun setNotificationEnabled(enabled: Boolean) {
        isUserTogglingSwitch = true
        switchNotifications.isChecked = enabled
        isUserTogglingSwitch = false
    }

    override fun navigateToMyAccount() {
        startActivity(Intent(this, ProfileActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun navigateToChangePassword() {
        startActivity(Intent(this, ChangePasswordActivity::class.java))
    }

    override fun navigateToManageShelf() {
        startActivity(Intent(this, ManageShelfActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun navigateToAbout() {
        startActivity(Intent(this, AboutActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    fun navigateToHistory() {
        startActivity(Intent(this, HistoryActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    fun navigateToNotifications() {
        startActivity(Intent(this, NotificationActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun signOut() {
        AuthRepository.logout()
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    private fun showSignOutDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_signout, null, false)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(view)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnYes = view.findViewById<MaterialButton>(R.id.btnYes)
        val btnNo  = view.findViewById<MaterialButton>(R.id.btnNo)

        btnYes.setOnClickListener {
            dialog.dismiss()
            signOut()
        }
        btnNo.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    override fun showMessage(msg: String) {
        // Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
