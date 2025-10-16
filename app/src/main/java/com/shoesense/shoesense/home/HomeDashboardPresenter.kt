package com.shoesense.shoesense.home

import android.content.Context
import com.shoesense.shoesense.Model.Slot
import com.shoesense.shoesense.Model.SlotRepository

class HomeDashboardPresenter(private val ctx: Context) {

    private var view: HomeDashboardView? = null
    private val repo = SlotRepository(ctx)
    private val maxSlots = 12
    private var observing = false

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
        // idempotent: ensures we’re observing after returning from detail/add screens
        if (!observing) observe()
    }

    private fun observe() {
        observing = true
        repo.observeSlots(
            maxSlots = maxSlots,
            onUpdate = { slots ->
                val rows = buildRows(slots)
                view?.render(rows)
            },
            onError = { msg -> view?.showError(msg) }
        )
    }

    private fun buildRows(slots: List<Slot>): List<SlotRow> {
        val dataRows = slots.map { SlotRow.Data(it) }
        return if (dataRows.size < maxSlots) dataRows + SlotRow.Add else dataRows
    }

    fun nextSlotNumber(): Int = repo.nextSlotNumber(maxSlots)
    fun onAddClicked() = view?.openAddSlot()
    fun onSlotClicked(s: Slot) = view?.openSlotDetail(s)
}
