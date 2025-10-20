package com.shoesense.shoesense.home

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shoesense.shoesense.Model.Slot
import com.shoesense.shoesense.R
import com.shoesense.shoesense.AddSlot.AddSlotActivity
import com.shoesense.shoesense.Repository.BottomNavbar
import com.shoesense.shoesense.SlotDetail.SlotDetailActivity
import com.shoesense.shoesense.history.HistoryActivity
import com.shoesense.shoesense.notification.NotificationActivity
import com.shoesense.shoesense.settings.SettingsActivity

class HomeDashboardActivity : AppCompatActivity(), HomeDashboardView {

    private lateinit var rv: RecyclerView
    private lateinit var adapter: SlotAdapter
    private lateinit var presenter: HomeDashboardPresenter

    // Overview counters
    private lateinit var tvActiveSlots: TextView
    private lateinit var tvEmptySlots: TextView
    private lateinit var tvTotalSlots: TextView

    private val refreshLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == AddSlotActivity.RESULT_SAVED) {
            presenter.load()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_dashboard)

        // --- bind Overview counters ---
        tvActiveSlots = findViewById(R.id.tvActiveSlots)
        // NOTE: XML id is tvNotifications but the label is "Empty Slots"
        // (feel free to rename the id in XML to tvEmptySlots later)
        tvEmptySlots = findViewById(R.id.tvNotifications)
        tvTotalSlots = findViewById(R.id.tvTotalSlots)

        // --- init grid ---
        rv = findViewById(R.id.slotRecyclerView)
        rv.layoutManager = GridLayoutManager(this, 2)
        adapter = SlotAdapter(
            onAdd = { presenter.onAddClicked() },
            onClick = { slot -> presenter.onSlotClicked(slot) }
        )
        rv.adapter = adapter

        // --- bottom nav ---
        // --- bottom nav hookup (AFTER setContentView) ---
        BottomNavbar.attach(
            activity = this,
            defaultSelected = BottomNavbar.Item.HOME,
            callbacks = BottomNavbar.Callbacks(
                onHome = {
                    // already here — just scroll to top
                    rv.smoothScrollToPosition(0)
                },
                onHistory = {
                    // TODO: start AnalyticsActivity
                    // startActivity(Intent(this, AnalyticsActivity::class.java))
                    // overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                },
                onNotifications = {
                    // TODO: start NotificationsActivity
                    // startActivity(Intent(this, NotificationsActivity::class.java))
                    // overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                },
                onSettings = {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    // don't finish() so Back returns to Home
                }
            ),
            // same visual rules as Settings: selected=solid white, others faded
            selectedTint   = resources.getColor(android.R.color.white, theme),
            unselectedTint = resources.getColor(android.R.color.white, theme),
            unselectedAlpha = 0.35f
        )

        // --- presenter ---
        presenter = HomeDashboardPresenter(this)
        presenter.attach(this)
        presenter.load()
    }

    override fun onDestroy() {
        presenter.detach()
        super.onDestroy()
    }

    // --- HomeDashboardView ---
    override fun render(items: List<SlotRow>) {
        if (!::adapter.isInitialized) return
        adapter.submitList(items)

        // derive counts from what we actually render
        val dataRows = items.filterIsInstance<SlotRow.Data>()
        val total = dataRows.size
        val active = dataRows.count { it.slot.occupied }
        val empty = total - active

        tvActiveSlots.text = active.toString()
        tvEmptySlots.text  = empty.toString()
        tvTotalSlots.text  = total.toString()
    }

    override fun openAddSlot() {
        val slotNum = presenter.nextSlotNumber()
        if (slotNum <= 0) {
            showError("Maximum of 12 slots reached.")
            return
        }
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
        // Toast/Snackbar if desired
    }

    override fun onResume() {
        super.onResume()
        presenter.load()
    }
}
