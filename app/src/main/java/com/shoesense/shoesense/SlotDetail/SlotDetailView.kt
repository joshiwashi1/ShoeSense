package com.shoesense.shoesense.SlotDetail

interface SlotDetailView {
    // Render
    fun showSlotName(name: String)
    fun showStatus(status: String)                 // "Occupied" / "Empty"
    fun showTimeline(occupiedAt: String?, emptyAt: String?)
    fun setNotificationsEnabled(enabled: Boolean)

    // UX helpers
    fun showToast(message: String)
    fun navigateBack()

    // Data asks (Activity owns UI, Presenter owns flow)
    fun askForNewName(current: String, onResult: (String?) -> Unit)
    fun confirmDelete(slotName: String, onResult: (Boolean) -> Unit)
    fun askForNewThreshold(current: Int, onResult: (Int?) -> Unit)
}
