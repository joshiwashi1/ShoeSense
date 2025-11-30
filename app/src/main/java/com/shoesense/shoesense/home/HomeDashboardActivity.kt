package com.shoesense.shoesense.home

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import com.shoesense.shoesense.BaseActivity
import com.shoesense.shoesense.R
import com.shoesense.shoesense.AddSlot.AddSlotActivity
import com.shoesense.shoesense.Repository.AppConfig
import com.shoesense.shoesense.Repository.BottomNavbar
import com.shoesense.shoesense.SlotDetail.SlotDetailActivity
import com.shoesense.shoesense.settings.SettingsActivity

class HomeDashboardActivity : BaseActivity(), HomeDashboardView {

    private lateinit var rv: RecyclerView
    private lateinit var adapter: SlotAdapter
    private lateinit var presenter: HomeDashboardPresenter

    // Overview counters
    private lateinit var tvActiveSlots: TextView
    private lateinit var tvEmptySlots: TextView
    private lateinit var tvTotalSlots: TextView

    // Retry buttons
    private lateinit var btnLoadingRetry: Button
    private lateinit var btnNoNetworkRetry: Button
    private lateinit var btnIotRetry: Button

    private val refreshLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == AddSlotActivity.RESULT_SAVED) {
            presenter.load()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide ActionBar and status bar
        supportActionBar?.hide()
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)

        // Inflate layout + global loading overlay
        setContentView(R.layout.activity_home_dashboard)

        bindViews()
        attachNavbar()
        attachRetryButtons()

        // Initialize RecyclerView grid
        rv.layoutManager = GridLayoutManager(this, 2)
        adapter = SlotAdapter(
            onAdd = { presenter.onAddClicked() },
            onClick = { slot -> presenter.onSlotClicked(slot) }
        )
        rv.adapter = adapter

        // Initialize presenter
        presenter = HomeDashboardPresenter(this)
        presenter.attach(this)

        // Show loading overlay while loading
        showLoadingState()
        presenter.load()
    }

    private fun bindViews() {
        tvActiveSlots = findViewById(R.id.tvActiveSlots)
        tvEmptySlots = findViewById(R.id.tvNotifications)
        tvTotalSlots = findViewById(R.id.tvTotalSlots)

        rv = findViewById(R.id.slotRecyclerView)

        // Retry buttons from BaseActivity overlay
        btnLoadingRetry = findViewById(R.id.btnLoadingRetry)
        btnNoNetworkRetry = findViewById(R.id.btnNoNetworkRetry)
        btnIotRetry = findViewById(R.id.btnIotRetry)
    }

    private fun attachNavbar() {
        BottomNavbar.attach(
            activity = this,
            defaultSelected = BottomNavbar.Item.HOME,
            callbacks = BottomNavbar.Callbacks(
                onHome = { rv.smoothScrollToPosition(0) },
                onHistory = { /* Optional */ },
                onNotifications = { /* Optional */ },
                onSettings = {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            ),
            selectedTint = getColor(android.R.color.white),
            unselectedTint = getColor(android.R.color.white),
            unselectedAlpha = 0.35f
        )
    }

    private fun attachRetryButtons() {
        btnLoadingRetry.setOnClickListener {
            showLoadingState()
            presenter.load()
        }
        btnNoNetworkRetry.setOnClickListener {
            showLoadingState()
            presenter.load()
        }
        btnIotRetry.setOnClickListener {
            showLoadingState()
            presenter.load()
        }
    }

    // --- HomeDashboardView implementation ---
    override fun render(items: List<SlotRow>) {
        if (!::adapter.isInitialized) return
        adapter.submitList(items)

        val dataRows = items.filterIsInstance<SlotRow.Data>()
        val total = dataRows.size
        val active = dataRows.count { it.slot.occupied }
        val empty = total - active

        tvActiveSlots.text = active.toString()
        tvEmptySlots.text = empty.toString()
        tvTotalSlots.text = total.toString()

        hideAllStates() // hide loading once data is rendered
    }

    override fun openAddSlot() {
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
        val siteId = AppConfig.siteId ?: "home001"
        val intent = Intent(this, SlotDetailActivity::class.java).apply {
            putExtra("slot_id", slot.id)
            putExtra("site_id", siteId)
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        showLoadingState()
        presenter.load()
    }

    override fun onDestroy() {
        presenter.detach()
        super.onDestroy()
    }

    // Optional: show overlays manually
    fun showNoNetworkOverlay() = showNoNetworkState()
    fun showIotNotFoundOverlay() = showIotNotFoundState()
}
