package com.shoesense.shoesense.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shoesense.shoesense.Model.Slot
import com.shoesense.shoesense.R
import com.shoesense.shoesense.AddSlot.AddSlotActivity
import com.shoesense.shoesense.Repository.BottomNavbar
import com.shoesense.shoesense.SlotDetail.SlotDetailActivity
import com.shoesense.shoesense.settings.SettingsActivity


class HomeDashboardActivity : AppCompatActivity(), HomeDashboardView {

    private lateinit var rv: RecyclerView
    private lateinit var adapter: SlotAdapter
    private lateinit var presenter: HomeDashboardPresenter

    // refresh list after returning from AddSlotActivity or SlotDetailActivity
    private val refreshLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        presenter.load()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_dashboard)

        presenter = HomeDashboardPresenter(this)
        presenter.attach(this)

        rv = findViewById(R.id.slotRecyclerView)
        rv.layoutManager = GridLayoutManager(this, 2)
        adapter = SlotAdapter(
            onAdd = { presenter.onAddClicked() },
            onClick = { slot -> presenter.onSlotClicked(slot) }
        )
        rv.adapter = adapter

        // ✅ Call the navbar AFTER setContentView so the views exist
        BottomNavbar.attach(
            activity = this,
            defaultSelected = BottomNavbar.Item.HOME,
            callbacks = BottomNavbar.Callbacks(
                onHome = { /* already here; maybe scroll to top */ rv.smoothScrollToPosition(0) },
                //onAnalytics = { startActivity(Intent(this, AnalyticsActivity::class.java)) },
                //onNotifications = { startActivity(Intent(this, NotificationsActivity::class.java)) },
                onSettings = { startActivity(Intent(this, SettingsActivity::class.java)) }
            ),
            // Optional visual config:
            // selectedTint = ContextCompat.getColor(this, android.R.color.black),
            // unselectedTint = ContextCompat.getColor(this, R.color.gray_500),
            unselectedAlpha = 0.45f
        )

        presenter.load()
    }

    override fun onDestroy() {
        presenter.detach()
        super.onDestroy()
    }

    // --- HomeDashboardView Implementation ---
    override fun render(items: List<SlotRow>) {
        adapter.submitList(items)
    }

    override fun openAddSlot() {
        val slotNum = presenter.nextSlotNumber()
        val intent = Intent(this, AddSlotActivity::class.java)
            .putExtra(AddSlotActivity.EXTRA_SLOT_NUMBER, slotNum)
        refreshLauncher.launch(intent)
    }

    override fun openSlotDetail(slot: Slot) {
        val intent = Intent(this, SlotDetailActivity::class.java).apply {
            putExtra("slot_id", slot.id)
            putExtra("slot_name", slot.name)
            putExtra("occupied", slot.occupied)
            putExtra("last_updated", slot.lastUpdated)
        }
        refreshLauncher.launch(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun showError(message: String) {
        // Toast / Snackbar here if you want
    }

    override fun onResume() {
        super.onResume()
        presenter.load()
    }
}
