package com.shoesense.shoesense.history

import com.shoesense.shoesense.Model.HistoryRepository
import com.shoesense.shoesense.Model.Slot
import com.shoesense.shoesense.Model.SlotRepository

class HistoryPresenter(
    private val view: HistoryView,
    private val slotRepo: SlotRepository,
    private val historyRepo: HistoryRepository
) {

    private var allSlots: List<Slot> = emptyList()
    private var selectedSlotId: String? = null

    fun observeSlots(maxSlots: Int) {
        slotRepo.observeSlots(
            maxSlots = maxSlots,
            onUpdate = { slots ->
                allSlots = slots

                // Build dropdown options: "All slots" + slot display names
                val options = mutableListOf<String>()
                options.add("All slots")
                options.addAll(slots.map { it.getDisplayName() })
                view.showSlotFilterOptions(options)

                // Start by showing ALL slots history
                observeHistory(null)
            },
            onError = { msg ->
                view.showError(msg)
            }
        )
    }

    /**
     * Spinner index:
     * 0 = All slots
     * 1..n = specific slot at (index - 1) in allSlots
     */
    fun onFilterSelectedPosition(position: Int) {
        selectedSlotId = if (position == 0) {
            null
        } else {
            allSlots.getOrNull(position - 1)?.id
        }
        observeHistory(selectedSlotId)
    }

    private fun observeHistory(slotId: String?) {
        historyRepo.observeHistory(
            slotId = slotId,
            onUpdate = { events ->
                view.showEvents(events)
            },
            onError = { msg ->
                view.showError(msg)
            }
        )
    }

    fun detach() {
        slotRepo.stopObserving()
        historyRepo.stop()
    }
}
