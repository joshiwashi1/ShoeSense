package com.shoesense.shoesense.ManageShelf

import com.shoesense.shoesense.Model.Slot

interface ManageShelfView {
    fun showLoading(isLoading: Boolean)
    fun showCapacity(cap: Int)
    fun showSlots(slots: List<Slot>)
    fun updateStats(total: Int, occupied: Int, empty: Int)
    fun showMessage(msg: String)
    fun openRenameDialog(slot: Slot)
}
