package com.shoesense.shoesense.history

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shoesense.shoesense.Model.HistoryRepository
import com.shoesense.shoesense.Model.SlotEvent
import com.shoesense.shoesense.Model.SlotRepository
import com.shoesense.shoesense.R
import com.shoesense.shoesense.Repository.BottomNavbar
import com.shoesense.shoesense.Utils.LoadingScreenHelper   // 👈 add this
import com.shoesense.shoesense.home.HomeDashboardActivity
import com.shoesense.shoesense.notification.NotificationActivity
import com.shoesense.shoesense.settings.SettingsActivity

class HistoryActivity : AppCompatActivity(), HistoryView {

    private lateinit var presenter: HistoryPresenter

    private lateinit var recyclerHistory: RecyclerView
    private lateinit var spnSlotFilter: Spinner

    private val adapter = HistoryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // ✅ Initialize loading helper for this Activity
        LoadingScreenHelper.init(this)
        LoadingScreenHelper.showLoading("Loading history…")

        // RecyclerView
        recyclerHistory = findViewById(R.id.recyclerHistory)
        recyclerHistory.layoutManager = LinearLayoutManager(this)
        recyclerHistory.adapter = adapter

        // Spinner
        spnSlotFilter = findViewById(R.id.spnSlotFilter)

        // Presenter: uses both SlotRepository (for slots) and HistoryRepository (for events)
        val slotRepo = SlotRepository(this, enableHistoryLogging = false)
        val historyRepo = HistoryRepository()
        presenter = HistoryPresenter(this, slotRepo, historyRepo)
        presenter.observeSlots(maxSlots = 10)

        // Spinner selection → presenter
        spnSlotFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                presenter.onFilterSelectedPosition(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        // Bottom navbar
        BottomNavbar.attach(
            activity = this,
            defaultSelected = BottomNavbar.Item.HISTORY,
            callbacks = BottomNavbar.Callbacks(
                onHome = {
                    LoadingScreenHelper.showLoading("Loading home…")
                    startActivity(Intent(this, HomeDashboardActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                },
                onHistory = { /* already here */ },
                onNotifications = {
                    LoadingScreenHelper.showLoading("Opening notifications…")
                    startActivity(Intent(this, NotificationActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                },
                onSettings = {
                    LoadingScreenHelper.showLoading("Opening settings…")
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }
            ),
            unselectedAlpha = 0.45f
        )
    }

    // ==== HistoryView implementation ====

    override fun showEvents(events: List<SlotEvent>) {
        // ✅ Data arrived → hide loader
        LoadingScreenHelper.hide()
        adapter.submitList(events)
    }

    override fun showSlotFilterOptions(options: List<String>) {
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            options
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnSlotFilter.adapter = spinnerAdapter
    }

    override fun showError(message: String) {
        LoadingScreenHelper.hide()   // also hide on error
        Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detach()
    }
}
