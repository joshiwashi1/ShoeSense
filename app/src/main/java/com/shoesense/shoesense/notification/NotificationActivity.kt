package com.shoesense.shoesense.notification

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.shoesense.shoesense.R
import com.shoesense.shoesense.Repository.BottomNavbar
import com.shoesense.shoesense.history.HistoryActivity
import com.shoesense.shoesense.home.HomeDashboardActivity
import com.shoesense.shoesense.settings.SettingsActivity

class NotificationActivity : AppCompatActivity(), NotificationView {

    private lateinit var presenter: NotificationPresenter
    private lateinit var notificationList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hide the AppCompat ActionBar (if your theme still shows one)
        actionBar?.hide()
        // Hide the STATUS BAR (not the nav bar)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_notification)

        notificationList = findViewById(R.id.notificationList)

        presenter = NotificationPresenter(this)
        presenter.attach(this)

        BottomNavbar.attach(
            activity = this,
            defaultSelected = BottomNavbar.Item.NOTIFICATIONS,
            callbacks = BottomNavbar.Callbacks(
                onHome = {
                    startActivity(Intent(this, HomeDashboardActivity::class.java))
                    finish()
                },
                onHistory = {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    finish()
                },
                onNotifications = {},
                onSettings = {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    finish()
                }
            ),
            unselectedAlpha = 0.45f
        )
    }

    override fun renderNotifications(notifications: List<String>) {
        notificationList.removeAllViews()
        for (msg in notifications) {
            val textView = TextView(this).apply {
                text = msg
                textSize = 16f
                setTextColor(resources.getColor(android.R.color.black, theme))
                setPadding(8, 8, 8, 8)
            }
            notificationList.addView(textView)
        }
    }

    override fun showError(message: String) {
        val textView = TextView(this).apply {
            text = message
            textSize = 16f
            setTextColor(resources.getColor(android.R.color.holo_red_dark, theme))
        }
        notificationList.addView(textView)
    }

    override fun onDestroy() {
        presenter.detach()
        super.onDestroy()
    }
}
