package com.shoesense.shoesense.SlotDetail

interface SlotDetailView {
    // Render
    fun showSlotName(name: String)
    fun showStatus(status: String)                 // "Occupied" / "Empty"
    fun showTimeline(occupiedAt: String?, emptyAt: String?)
    fun setNotificationsEnabled(enabled: Boolean)
    fun showToast(message: String)

    // Navigation
    fun navigateBack()

    // Dialog asks
    fun askForNewName(current: String, onResult: (String?) -> Unit)
    fun confirmDelete(slotName: String, onResult: (Boolean) -> Unit)
    fun askForNewThreshold(current: Int, onResult: (Int?) -> Unit)

    // Optional: reflect current threshold in UI if you show it somewhere later
    fun getActivityContext(): android.content.Context
}
