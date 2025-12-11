package com.shoesense.shoesense.notification

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.shoesense.shoesense.R
import com.shoesense.shoesense.Repository.BottomNavbar
import com.shoesense.shoesense.Utils.LoadingScreenHelper
import com.shoesense.shoesense.history.HistoryActivity
import com.shoesense.shoesense.home.HomeDashboardActivity
import com.shoesense.shoesense.settings.SettingsActivity

class NotificationActivity : AppCompatActivity(), NotificationView {

    private lateinit var presenter: NotificationPresenter
    private lateinit var notificationList: LinearLayout
    private var allMarkedRead: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_notification)

        // ✅ init loading helper for this activity
        LoadingScreenHelper.init(this)
        LoadingScreenHelper.showLoading("Loading notifications…")

        notificationList = findViewById(R.id.notificationList)

        // "Mark all as read" click
        val markAllReadView = findViewById<TextView>(R.id.markAllRead)
        markAllReadView.setOnClickListener {
            allMarkedRead = true
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
                    // already here – no action
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

    override fun renderNotifications(notifications: List<String>) {
        // data arrived → hide loader
        LoadingScreenHelper.hide()

        notificationList.removeAllViews()

        if (notifications.isEmpty()) {
            val emptyView = TextView(this).apply {
                text = "No notifications yet."
                textSize = 14f
                setTextColor(resources.getColor(android.R.color.darker_gray, theme))
                setPadding(8, 16, 8, 16)
            }
            notificationList.addView(emptyView)
            return
        }

        for (msg in notifications) {
            val cardView = layoutInflater.inflate(
                R.layout.item_notification_card,
                notificationList,
                false
            )

            val titleView = cardView.findViewById<TextView>(R.id.tvNotifTitle)
            val messageView = cardView.findViewById<TextView>(R.id.tvNotifMessage)
            val timeView = cardView.findViewById<TextView>(R.id.tvNotifTime)
            val unreadDot = cardView.findViewById<View>(R.id.dotUnread)

            // Expecting format: "10:32 AM • Shoe detected in Slot A3"
            val parts = msg.split("•", limit = 2)
            if (parts.size == 2) {
                val timePart = parts[0].trim()
                val bodyPart = parts[1].trim()

                timeView.text = timePart
                messageView.text = bodyPart

                // 👉 Handle both "Shoe detected in X" and "Shoe removed from X"
                val slotLabel = when {
                    bodyPart.startsWith("Shoe detected in ") ->
                        bodyPart.removePrefix("Shoe detected in ").trim()

                    bodyPart.startsWith("Shoe removed from ") ->
                        bodyPart.removePrefix("Shoe removed from ").trim()

                    else ->
                        bodyPart // fallback: just show full message as title
                }

                titleView.text = slotLabel
            } else {
                // fallback if string not in expected format
                titleView.text = "Notification"
                messageView.text = msg
                timeView.text = ""
            }

            // If already marked all read in this session, hide dot immediately
            unreadDot.visibility = if (allMarkedRead) View.GONE else View.VISIBLE

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
        val childCount = notificationList.childCount
        for (i in 0 until childCount) {
            val child = notificationList.getChildAt(i)
            val dot = child.findViewById<View?>(R.id.dotUnread)
            dot?.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        presenter.detach()
        super.onDestroy()
    }
}
