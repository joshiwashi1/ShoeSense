package com.shoesense.shoesense.home

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shoesense.shoesense.Model.Slot
import com.shoesense.shoesense.R
import com.shoesense.shoesense.AddSlot.AddSlotActivity
import com.shoesense.shoesense.Repository.AppConfig
import com.shoesense.shoesense.utils.BottomNavbar
import com.shoesense.shoesense.Utils.LoadingScreenHelper     // 👈 ADD THIS
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
            LoadingScreenHelper.showLoading("Refreshing your shelf…")  // optional
            presenter.load()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_home_dashboard)

        // ✅ Init loading helper for this activity
        LoadingScreenHelper.init(this)
        LoadingScreenHelper.showLoading("Loading your shelf…")

        tvActiveSlots = findViewById(R.id.tvActiveSlots)
        tvEmptySlots = findViewById(R.id.tvNotifications)
        tvTotalSlots = findViewById(R.id.tvTotalSlots)

        rv = findViewById(R.id.slotRecyclerView)
        rv.layoutManager = GridLayoutManager(this, 2)
        adapter = SlotAdapter(
            onAdd = {
                LoadingScreenHelper.showLoading("Opening add slot…")
                presenter.onAddClicked()
            },
            onClick = { slot ->
                LoadingScreenHelper.showLoading("Opening slot details…")
                presenter.onSlotClicked(slot)
            }
        )
        rv.adapter = adapter

        BottomNavbar.attach(
            activity = this,
            defaultSelected = BottomNavbar.Item.HOME,
            callbacks = BottomNavbar.Callbacks(
                onHome = {
                    // Already on home — no need to show loader
                    rv.smoothScrollToPosition(0)
                },
                onHistory = {
                    LoadingScreenHelper.showLoading("Loading history…")
                    startActivity(Intent(this, HistoryActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                },
                onNotifications = {
                    LoadingScreenHelper.showLoading("Opening notifications…")
                    startActivity(Intent(this, NotificationActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                },
                onSettings = {
                    LoadingScreenHelper.showLoading("Opening settings…")
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            ),
            selectedTint   = ContextCompat.getColor(this, android.R.color.white),
            unselectedTint = ContextCompat.getColor(this, android.R.color.white),
            unselectedAlpha = 0.35f,
            useLoadingOverlay = false  // ⬅️ Let *this* activity control when to show loader
        )

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

        // ✅ Data finished loading → hide loader
        LoadingScreenHelper.hide()

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
        val canAdd = presenter.canAddMore()
        if (!canAdd) {
            val cap = presenter.getMaxSlots()
            showError("Maximum of $cap slots reached. You can change this in Manage Shelf.")
            return
        }

        val intent = Intent(this, AddSlotActivity::class.java)
            .putExtra(AddSlotActivity.EXTRA_IS_EDIT, false) // CREATE mode
        refreshLauncher.launch(intent)
    }

    override fun openSlotDetail(slot: Slot) {
        val siteId = AppConfig.siteId ?: "home001"

        val intent = Intent(this, SlotDetailActivity::class.java).apply {
            putExtra("slot_id", slot.id)
            putExtra("site_id", siteId)
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun showError(message: String) {
        LoadingScreenHelper.hide()  // also hide on error
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        LoadingScreenHelper.hide()   // 👈 add this
        presenter.load()
    }
}
