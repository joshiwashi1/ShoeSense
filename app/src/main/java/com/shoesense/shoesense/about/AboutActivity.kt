package com.shoesense.shoesense.about

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.shoesense.shoesense.R
import com.shoesense.shoesense.Repository.BottomNavbar
import com.shoesense.shoesense.home.HomeDashboardActivity
import com.shoesense.shoesense.settings.SettingsActivity
import com.shoesense.shoesense.BaseActivity

class AboutActivity : BaseActivity(), AboutView {

    private lateinit var presenter: AboutPresenter

    // Top bar
    private lateinit var btnBack: ImageButton
    private lateinit var tvTitle: TextView

    // Card content
    private lateinit var tvAppName: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvMembersTitle: TextView

    // Retry buttons (loading overlay)
    private lateinit var btnLoadingRetry: Button
    private lateinit var btnNoNetworkRetry: Button
    private lateinit var btnIotRetry: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide ActionBar and status bar
        supportActionBar?.hide()
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)

        // Inflate activity layout + global loading overlay
        setContentView(R.layout.activity_about)

        bindViews()
        attachNavbar()
        attachRetryButtons()

        presenter = AboutPresenter().also {
            it.attach(this)
            // Show loading overlay while data is loading
            showLoadingState()
            it.loadAbout()
        }
    }

    private fun bindViews() {
        // Top bar
        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.title)
        btnBack.setOnClickListener { finish() }

        // Card content
        tvAppName = findViewById(R.id.appName)
        tvSubtitle = findViewById(R.id.appSubtitle)
        tvDescription = findViewById(R.id.description)
        tvMembersTitle = findViewById(R.id.membersTitle)

        // Retry buttons from BaseActivity overlay
        btnLoadingRetry = findViewById(R.id.btnLoadingRetry)
        btnNoNetworkRetry = findViewById(R.id.btnNoNetworkRetry)
        btnIotRetry = findViewById(R.id.btnIotRetry)
    }

    private fun attachNavbar() {
        BottomNavbar.attach(
            activity = this,
            defaultSelected = BottomNavbar.Item.SETTINGS,
            callbacks = BottomNavbar.Callbacks(
                onHome = {
                    startActivity(Intent(this, HomeDashboardActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                },
                onHistory = { /* Optional */ },
                onNotifications = { /* Optional */ },
                onSettings = {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }
            ),
            unselectedAlpha = 0.45f
        )
    }

    private fun attachRetryButtons() {
        btnLoadingRetry.setOnClickListener {
            showLoadingState()
            presenter.loadAbout()
        }
        btnNoNetworkRetry.setOnClickListener {
            showLoadingState()
            presenter.loadAbout()
        }
        btnIotRetry.setOnClickListener {
            showLoadingState()
            presenter.loadAbout()
        }
    }

    // --- AboutView Implementation ---
    override fun showAppName(name: String) {
        tvAppName.text = name
        hideAllStates()
    }

    override fun showSubtitle(subtitle: String) {
        tvSubtitle.text = subtitle
    }

    override fun showDescription(desc: String) {
        tvDescription.text = desc
    }

    override fun showMembersTitle(title: String) {
        tvMembersTitle.text = title
    }

    override fun setTitleCentered(title: String) {
        tvTitle.text = title
    }

    // Optional: Call this if presenter fails to load data
    fun showNoNetworkOverlay() {
        showNoNetworkState()
    }

    fun showIotNotFoundOverlay() {
        showIotNotFoundState()
    }
}
