package com.shoesense.shoesense.notification

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.shoesense.shoesense.R
import com.shoesense.shoesense.Utils.LoadingScreenHelper
import com.shoesense.shoesense.history.HistoryActivity
import com.shoesense.shoesense.home.HomeDashboardActivity
import com.shoesense.shoesense.settings.SettingsActivity
import com.shoesense.shoesense.utils.BottomNavbar


class NotificationActivity : AppCompatActivity(), NotificationView {

    private lateinit var presenter: NotificationPresenter
    private lateinit var notificationList: LinearLayout
    private lateinit var readStore: ReadStateStore

    private val currentIds = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ AppCompatActivity uses supportActionBar
        supportActionBar?.hide()

        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_notification)

        LoadingScreenHelper.init(this)
        LoadingScreenHelper.showLoading("Loading notifications…")

        readStore = ReadStateStore(this)
        notificationList = findViewById(R.id.notificationList)

        findViewById<TextView>(R.id.markAllRead).setOnClickListener {
            readStore.markAllRead(currentIds)
            markAllCardsAsRead()
        }

        presenter = NotificationPresenter(applicationContext)
        presenter.attach(this)

        BottomNavbar.attach(
            activity = this,
            defaultSelected = BottomNavbar.Item.NOTIFICATIONS,
            callbacks = BottomNavbar.Callbacks(
                onHome = {
                    LoadingScreenHelper.showLoading("Going home…")
                    startActivity(Intent(this, HomeDashboardActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                },
                onHistory = {
                    LoadingScreenHelper.showLoading("Opening history…")
                    startActivity(Intent(this, HistoryActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                },
                onNotifications = {
                    // already here
                },
                onSettings = {
                    LoadingScreenHelper.showLoading("Opening settings…")
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }
            ),
            unselectedAlpha = 0.45f,
            useLoadingOverlay = false
        )
    }

    override fun renderNotifications(items: List<NotificationItem>) {
        LoadingScreenHelper.hide()
        notificationList.removeAllViews()
        currentIds.clear()

        if (items.isEmpty()) {
            val emptyView = TextView(this).apply {
                text = "No notifications yet."
                textSize = 14f
                setTextColor(resources.getColor(android.R.color.darker_gray, theme))
                setPadding(8, 16, 8, 16)
            }
            notificationList.addView(emptyView)
            return
        }

        for (item in items) {
            currentIds.add(item.id)

            val cardView = layoutInflater.inflate(
                R.layout.item_notification_card,
                notificationList,
                false
            )

            val titleView = cardView.findViewById<TextView>(R.id.tvNotifTitle)
            val messageView = cardView.findViewById<TextView>(R.id.tvNotifMessage)
            val timeView = cardView.findViewById<TextView>(R.id.tvNotifTime)
            val unreadDot = cardView.findViewById<View>(R.id.dotUnread)

            titleView.text = item.title
            messageView.text = item.message
            timeView.text = item.time

            unreadDot.visibility = if (item.isRead) View.GONE else View.VISIBLE

            cardView.setOnClickListener {
                readStore.markRead(item.id)
                unreadDot.visibility = View.GONE
            }

            notificationList.addView(cardView)
        }
    }

    override fun showError(message: String) {
        LoadingScreenHelper.hide()
        notificationList.removeAllViews()

        val textView = TextView(this).apply {
            text = message
            textSize = 16f
            setTextColor(resources.getColor(android.R.color.holo_red_dark, theme))
            setPadding(8, 16, 8, 16)
        }
        notificationList.addView(textView)
    }

    private fun markAllCardsAsRead() {
        for (i in 0 until notificationList.childCount) {
            val child = notificationList.getChildAt(i)
            child.findViewById<View?>(R.id.dotUnread)?.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        presenter.detach()
        super.onDestroy()
    }
}
