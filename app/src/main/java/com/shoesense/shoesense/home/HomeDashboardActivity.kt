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

        // 1) INIT UI FIRST
        rv = findViewById(R.id.slotRecyclerView)
        rv.layoutManager = GridLayoutManager(this, 2)
        adapter = SlotAdapter(
            onAdd = { presenter.onAddClicked() },
            onClick = { slot -> presenter.onSlotClicked(slot) }
        )
        rv.adapter = adapter

        BottomNavbar.attach(
            activity = this,
            defaultSelected = BottomNavbar.Item.HOME,
            callbacks = BottomNavbar.Callbacks(
                onHome = { rv.smoothScrollToPosition(0) },
                onSettings = { startActivity(Intent(this, SettingsActivity::class.java)) }
            ),
            unselectedAlpha = 0.45f
        )

        // 2) THEN PRESENTER
        presenter = HomeDashboardPresenter(this)
        presenter.attach(this)  // observe can safely render now
        presenter.load()
    }

    override fun onDestroy() {
        presenter.detach()
        super.onDestroy()
    }

    // --- HomeDashboardView ---
    override fun render(items: List<SlotRow>) {
        // (Optional extra guard; harmless and prevents future regressions)
        if (!::adapter.isInitialized) return
        adapter.submitList(items)
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
        // You can Toast/Snackbar here if you want
        // Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        presenter.load()
    }
}
