package com.shoesense.shoesense.Model

data class SlotEvent(
    val id: String = "",
    val slotId: String = "",
    val slotName: String = "",   // NEW
    val status: String = "",
    val lastUpdated: String = "",
    val weight: Double = 0.0,
    val threshold: Double = 0.0
) {
    // helper for display
    fun displaySlotLabel(): String = if (slotName.isNotBlank()) slotName else slotId
}
