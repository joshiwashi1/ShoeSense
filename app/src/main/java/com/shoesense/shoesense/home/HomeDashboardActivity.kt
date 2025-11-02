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
        // Hide the AppCompat ActionBar (if your theme still shows one)
        actionBar?.hide()
        // Hide the STATUS BAR (not the nav bar)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_home_dashboard)

        // --- bind Overview counters ---
        tvActiveSlots = findViewById(R.id.tvActiveSlots)
        // NOTE: XML id is tvNotifications but label is "Empty Slots"
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
        BottomNavbar.attach(
            activity = this,
            defaultSelected = BottomNavbar.Item.HOME,
            callbacks = BottomNavbar.Callbacks(
                onHome = { rv.smoothScrollToPosition(0) },
                onHistory = {
                    // startActivity(Intent(this, HistoryActivity::class.java))
                    // overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                },
                onNotifications = {
                    // startActivity(Intent(this, NotificationActivity::class.java))
                    // overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                },
                onSettings = {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            ),
            selectedTint   = ContextCompat.getColor(this, android.R.color.white),
            unselectedTint = ContextCompat.getColor(this, android.R.color.white),
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

        val dataRows = items.filterIsInstance<SlotRow.Data>()
        val total = dataRows.size
        val active = dataRows.count { it.slot.occupied }
        val empty = total - active

        tvActiveSlots.text = active.toString()
        tvEmptySlots.text  = empty.toString()
        tvTotalSlots.text  = total.toString()
    }

    override fun openAddSlot() {
        // Prefer a boolean guard; if you must keep nextSlotNumber(), treat <=0 as "no space"
        val canAdd = presenter.canAddMore()
        if (!canAdd) {
            showError("Maximum of 12 slots reached.")
            return
        }

        val intent = Intent(this, AddSlotActivity::class.java)
            .putExtra(AddSlotActivity.EXTRA_IS_EDIT, false) // CREATE mode
        refreshLauncher.launch(intent)
    }

    override fun openSlotDetail(slot: Slot) {
        val intent = Intent(this, SlotDetailActivity::class.java).apply {
            putExtra("slot_id", slot.id)
            putExtra("slot_name", slot.name)
            putExtra("occupied", slot.occupied)
            putExtra("last_updated", slot.lastUpdated)
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun showError(message: String) {
        // TODO: Toast or Snackbar
        // Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        presenter.load()
    }
}
