package com.shoesense.shoesense.notification

import android.content.Context
import com.shoesense.shoesense.Model.SlotRepository
import com.shoesense.shoesense.Model.Slot

class NotificationPresenter(private val ctx: Context) {
    private val repo = SlotRepository(ctx)
    private var observing = false
    private var view: NotificationView? = null

    fun attach(v: NotificationView) {
        view = v
        if (!observing) observeSlots()
    }

    fun detach() {
        view = null
        repo.stopObserving()
        observing = false
    }

    private fun observeSlots() {
        observing = true
        repo.observeSlots(
            maxSlots = 12,
            onUpdate = { slots ->
                val notifications = buildNotifications(slots)
                view?.renderNotifications(notifications)
            },
            onError = { msg -> view?.showError(msg) }
        )
    }

    private fun buildNotifications(slots: List<Slot>): List<String> {
        val list = mutableListOf<String>()
        for (slot in slots) {
            val status = if (slot.occupied) "Occupied" else "Empty"
            list.add("${slot.name} is now $status.")
        }
        return list
    }
}
