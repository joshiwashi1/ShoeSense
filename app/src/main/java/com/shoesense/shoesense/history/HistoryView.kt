package com.shoesense.shoesense.history

import com.shoesense.shoesense.Model.SlotEvent

interface HistoryView {
    /** Show history events (from /shoe_history/...) */
    fun showEvents(events: List<SlotEvent>)

    /** Update the dropdown with "All slots" + slot names */
    fun showSlotFilterOptions(options: List<String>)

    /** Show error message (Firebase error, etc.) */
    fun showError(message: String)
}
