// HomeDashboardView.kt
package com.shoesense.shoesense.home

import com.shoesense.shoesense.Model.Slot

interface HomeDashboardView {
    fun render(items: List<SlotRow>)      // <-- change to SlotRow
    fun openAddSlot()
    fun openSlotDetail(slot: Slot)
    fun showError(message: String)
}
