package com.shoesense.shoesense.home

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.shoesense.shoesense.Model.Slot
import com.shoesense.shoesense.Model.SlotRepository

class HomeDashboardPresenter(private val ctx: Context) {

    private var view: HomeDashboardView? = null
    private val repo = SlotRepository(ctx)
    private val maxSlots = 12
    private var observing = false

    // Keep the latest snapshot so we can compute canAddMore()
    private var latestSlots: List<Slot> = emptyList()

    private val handler = Handler(Looper.getMainLooper())

    fun attach(v: HomeDashboardView) {
        view = v
        if (!observing) observe()
    }

    fun detach() {
        view = null
        repo.stopObserving()
        observing = false
    }

    fun load() {
        // Show loading overlay
        (view as? HomeDashboardActivity)?.showLoadingState()

        // Simulate network / IoT checks with delay
        handler.postDelayed({

            val isNetworkAvailable = true  // replace with real network check
            val isIotConnected = true      // replace with real IoT check

            if (!isNetworkAvailable) {
                (view as? HomeDashboardActivity)?.showNoNetworkOverlay()
                return@postDelayed
            }
            if (!isIotConnected) {
                (view as? HomeDashboardActivity)?.showIotNotFoundOverlay()
                return@postDelayed
            }

            // Normal flow: start observing slots
            if (!observing) observe()

        }, 1500) // 1.5s simulated delay
    }

    private fun observe() {
        observing = true
        repo.observeSlots(
            maxSlots = maxSlots,
            onUpdate = { slots ->
                latestSlots = slots

                val rows = buildRows(slots)
                view?.render(rows)
            },
            onError = { msg ->
                view?.showError(msg)
                // Optionally show no network overlay if repo fails
                (view as? HomeDashboardActivity)?.showNoNetworkOverlay()
            }
        )
    }

    private fun buildRows(slots: List<Slot>): List<SlotRow> {
        val dataRows = slots.map { SlotRow.Data(it) }
        return if (dataRows.size < maxSlots) dataRows + SlotRow.Add else dataRows
    }

    fun nextSlotNumber(): Int = repo.nextSlotNumber(maxSlots)
    fun canAddMore(): Boolean = latestSlots.size < maxSlots
    fun onAddClicked() = view?.openAddSlot()
    fun onSlotClicked(s: Slot) = view?.openSlotDetail(s)
}
