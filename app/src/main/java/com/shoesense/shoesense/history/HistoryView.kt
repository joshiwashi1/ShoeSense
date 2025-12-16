package com.shoesense.shoesense.history

import com.shoesense.shoesense.Model.SlotEvent

interface HistoryView {

    /** Show history events (from /shoe_history/...) */
    fun showEvents(events: List<SlotEvent>)

    /** Update the dropdown with "All slots" + slot names */
    fun showSlotFilterOptions(options: List<String>)

    /** Provide latest slotId -> slotName mapping (for rename updates in history list) */
    fun updateSlotNameMap(map: Map<String, String>)

    /** Show error message (Firebase error, etc.) */
    fun showError(message: String)
}
