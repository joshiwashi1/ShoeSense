package com.shoesense.shoesense.home

import android.content.Context
import com.shoesense.shoesense.Model.Slot
import com.shoesense.shoesense.Model.SlotRepository

class HomeDashboardPresenter(private val ctx: Context) {

    private var view: HomeDashboardView? = null
    private val repo = SlotRepository(ctx)
    private val maxSlots = 12

    fun attach(v: HomeDashboardView) { view = v }
    fun detach() { view = null }

    fun load() {
        // If readSlots(...) might return null, .orEmpty() prevents NPE/compile errors
        val data: List<SlotRow> = repo.readSlots(maxSlots)
            .orEmpty()                         // <-- remove if your function is non-nullable
            .map { slot -> SlotRow.Data(slot) }

        // Append the Add card immutably when there’s still room
        val rows: List<SlotRow> =
            if (data.size < maxSlots) data + SlotRow.Add else data

        view?.render(rows)                     // HomeDashboardView.render(List<SlotRow>)
    }


    fun nextSlotNumber(): Int = repo.nextSlotNumber(maxSlots)
    fun onAddClicked() = view?.openAddSlot()
    fun onSlotClicked(s: Slot) = view?.openSlotDetail(s)
}
