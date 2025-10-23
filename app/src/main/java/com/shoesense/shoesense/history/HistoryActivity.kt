package com.shoesense.shoesense.history

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.shoesense.shoesense.Model.Slot
import com.shoesense.shoesense.R
import com.shoesense.shoesense.Repository.BottomNavbar
import com.shoesense.shoesense.home.HomeDashboardActivity
import com.shoesense.shoesense.notification.NotificationActivity
import com.shoesense.shoesense.settings.SettingsActivity

class HistoryActivity : AppCompatActivity(), HistoryView {

    private lateinit var presenter: HistoryPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        presenter = HistoryPresenter(this)
        presenter.observeSlots(maxSlots = 10)

        BottomNavbar.attach(
            activity = this,
            defaultSelected = BottomNavbar.Item.HISTORY,
            callbacks = BottomNavbar.Callbacks(
                onHome = {
                    startActivity(Intent(this, HomeDashboardActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                },
                onHistory = {},
                onNotifications = {
                    startActivity(Intent(this, NotificationActivity::class.java))
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
    }

    override fun onSlotsUpdated(slots: List<Slot>) {
        // Update RecyclerView or UI
        Toast.makeText(this, "Fetched ${slots.size} slots", Toast.LENGTH_SHORT).show()
    }

    override fun onError(message: String) {
        Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detach()
    }
}