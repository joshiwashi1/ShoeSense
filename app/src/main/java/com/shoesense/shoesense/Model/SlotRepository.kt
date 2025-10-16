package com.shoesense.shoesense.Model

import android.content.Context

class SlotRepository(context: Context) {
    private val prefs = context.getSharedPreferences("shoe_slots", Context.MODE_PRIVATE)

    fun readSlots(maxSlots: Int = 12): List<Slot> {
        val list = mutableListOf<Slot>()
        for (i in 1..maxSlots) {
            val name = prefs.getString(keyName(i), "") ?: ""
            val th = prefs.getFloat(keyThreshold(i), 0f)
            if (name.isNotBlank()) {
                // you can compute "occupied" based on threshold or a separate flag; here demo uses threshold>0
                val occupied = th > 0f
                list.add(Slot(id = i.toString(), name = name, occupied = occupied, lastUpdated = "—"))
            }
        }
        return list
    }

    fun nextSlotNumber(maxSlots: Int = 12): Int {
        for (i in 1..maxSlots) {
            val name = prefs.getString(keyName(i), "") ?: ""
            if (name.isBlank()) return i
        }
        return maxSlots + 1 // indicates full
    }

    private fun keyName(slot: Int) = "slot_${slot}_name"
    private fun keyThreshold(slot: Int) = "slot_${slot}_threshold"
}
