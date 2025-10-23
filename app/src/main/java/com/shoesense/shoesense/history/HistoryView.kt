package com.shoesense.shoesense.history

import com.shoesense.shoesense.Model.Slot

interface HistoryView {
    fun onSlotsUpdated(slots: List<Slot>)
    fun onError(message: String)
}
