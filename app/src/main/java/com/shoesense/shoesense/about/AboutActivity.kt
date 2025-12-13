package com.shoesense.shoesense.about

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.shoesense.shoesense.R
import com.shoesense.shoesense.utils.BottomNavbar
import com.shoesense.shoesense.history.HistoryActivity
import com.shoesense.shoesense.home.HomeDashboardActivity
import com.shoesense.shoesense.notification.NotificationActivity
// import com.shoesense.shoesense.analytics.AnalyticsActivity
// import com.shoesense.shoesense.notifications.NotificationsActivity
import com.shoesense.shoesense.settings.SettingsActivity

class AboutActivity : AppCompatActivity(), AboutView {

    private lateinit var presenter: AboutPresenter

    // Top bar
    private lateinit var btnBack: ImageButton
    private lateinit var tvTitle: TextView

    // Card content
    private lateinit var tvAppName: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvMembersTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hide the AppCompat ActionBar (if your theme still shows one)
        supportActionBar?.hide()
        // Hide the STATUS BAR (not the nav bar)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_about)

        // 🔹 Make sure views exist before you use them
        bindViews()

        // Now it's safe to use btnBack
        btnBack.setOnClickListener { finish() }

        // Attach navbar AFTER contentView and views are ready
        BottomNavbar.attach(
            activity = this,
            defaultSelected = BottomNavbar.Item.SETTINGS,
            callbacks = BottomNavbar.Callbacks(
                onHome = {
                    startActivity(Intent(this, HomeDashboardActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                },
                onHistory = {startActivity(Intent(this, HistoryActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                },
                onNotifications = { startActivity(Intent(this, NotificationActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                },
                onSettings = {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }
            ),
            unselectedAlpha = 0.45f
        )

        presenter = AboutPresenter().also {
            it.attach(this)
            it.loadAbout()
        }
    }


    private fun bindViews() {
        // Top
        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.title)

        // Content
        tvAppName = findViewById(R.id.appName)
        tvSubtitle = findViewById(R.id.appSubtitle)
        tvDescription = findViewById(R.id.description)
        tvMembersTitle = findViewById(R.id.membersTitle)
    }

    // --- AboutView impl ---
    override fun showAppName(name: String) { tvAppName.text = name }
    override fun showSubtitle(subtitle: String) { tvSubtitle.text = subtitle }
    override fun showDescription(desc: String) { tvDescription.text = desc }
    override fun showMembersTitle(title: String) { tvMembersTitle.text = title }
    override fun setTitleCentered(title: String) { tvTitle.text = title }
}
