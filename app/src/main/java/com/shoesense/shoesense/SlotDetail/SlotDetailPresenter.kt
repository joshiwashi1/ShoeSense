package com.shoesense.shoesense.SlotDetail

class SlotDetailPresenter(private val view: SlotDetailView) {

    // simple in-memory state; swap with repo/DB later
    private var slotName: String = "Slot 1"
    private var status: String = "Occupied"     // or "Empty"
    private var notifEnabled: Boolean = false
    private var thresholdGrams: Int = 500
    private var occupiedAt: String? = "10:30 AM"
    private var emptyAt: String? = "9:45 AM"

    fun onInit(
        initialName: String? = null,
        initialStatus: String? = null,
        initialNotif: Boolean? = null
    ) {
        initialName?.let { slotName = it }
        initialStatus?.let { status = it }
        initialNotif?.let { notifEnabled = it }
        renderAll()
    }

    private fun renderAll() {
        view.showSlotName(slotName)
        view.showStatus(status)
        view.showTimeline(occupiedAt, emptyAt)
        view.setNotificationsEnabled(notifEnabled)
    }

    // ==== getters used by SlotDetailActivity ====
    fun getSlotName(): String = slotName
    fun getThresholdGrams(): Int = thresholdGrams

    // ==== actions used by SlotDetailActivity ====
    fun applyNewThreshold(newValue: Int) {
        thresholdGrams = newValue
        view.showToast("Threshold set to ${thresholdGrams}g")
        // TODO: persist to repo/DB/cloud if needed
    }

    fun onBackClicked() = view.navigateBack()

    fun onRenameClicked() {
        view.askForNewName(slotName) { newName ->
            val trimmed = newName?.trim().orEmpty()
            if (trimmed.isNotEmpty()) {
                slotName = trimmed
                view.showSlotName(slotName)
                view.showToast("Renamed to \"$slotName\"")
            }
        }
    }

    fun onDeleteClicked() {
        view.confirmDelete(slotName) { confirmed ->
            if (confirmed) {
                view.showToast("Deleted $slotName")
                view.navigateBack()
            }
        }
    }

    fun onNotificationsToggled(enabled: Boolean) {
        notifEnabled = enabled
        view.showToast(if (enabled) "Notifications enabled" else "Notifications disabled")
        // TODO: persist per slot if needed
    }
}
