package com.shoesense.shoesense.Model

import com.google.firebase.database.*
import com.shoesense.shoesense.Repository.AppConfig

class HistoryRepository {

    private var listener: ValueEventListener? = null
    private var ref: DatabaseReference? = null

    fun observeHistory(
        slotId: String?,                            // null = all slots, else specific slot
        onUpdate: (List<SlotEvent>) -> Unit,
        onError: (String) -> Unit
    ) {
        stop()

        val siteId = AppConfig.siteId ?: "home001"
        ref = if (slotId == null) {
            FirebaseDatabase.getInstance().reference.child("shoe_history/$siteId")
        } else {
            FirebaseDatabase.getInstance().reference.child("shoe_history/$siteId/$slotId")
        }

        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<SlotEvent>()

                if (slotId == null) {
                    // all slots: shoe_history/siteId/slotX/eventY
                    snapshot.children.forEach { slotNode ->
                        val idForSlot = slotNode.key ?: return@forEach
                        slotNode.children.forEach { eventNode ->
                            list.add(eventNode.toSlotEvent(idForSlot))
                        }
                    }
                } else {
                    // single slot: shoe_history/siteId/slotX/eventY
                    snapshot.children.forEach { eventNode ->
                        list.add(eventNode.toSlotEvent(slotId))
                    }
                }

                // newest first
                val sorted = list.sortedByDescending { it.lastUpdated }
                onUpdate(sorted)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        }

        ref?.addValueEventListener(listener!!)
    }

    fun stop() {
        listener?.let { ref?.removeEventListener(it) }
        listener = null
        ref = null
    }

    // ---------- mapping snapshot -> SlotEvent ----------

    private fun DataSnapshot.toSlotEvent(slotId: String): SlotEvent {
        return SlotEvent(
            id = key ?: "",
            slotId = slotId,
            slotName = getString("slot_name") ?: "",          // may be blank for old events
            status = getString("status") ?: "",
            lastUpdated = getString("last_updated") ?: "",
            weight = getDouble("weight") ?: 0.0,
            threshold = getDouble("threshold") ?: 0.0
        )
    }

    // ---------- small helpers ----------

    private fun DataSnapshot.getString(key: String): String? {
        val v = child(key).value
        return when (v) {
            is String -> v
            is Number -> v.toString()
            is Boolean -> v.toString()
            else -> null
        }
    }

    private fun DataSnapshot.getDouble(key: String): Double? {
        val v = child(key).value
        return when (v) {
            is Number -> v.toDouble()
            is String -> v.toDoubleOrNull()
            else -> null
        }
    }
}
