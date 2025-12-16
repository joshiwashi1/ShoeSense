package com.shoesense.shoesense.Model

import com.google.firebase.database.*
import com.shoesense.shoesense.Repository.AppConfig
import java.util.Locale

class HistoryRepository {

    private var listener: ValueEventListener? = null
    private var ref: DatabaseReference? = null

    fun observeHistory(
        slotId: String?, // null = all slots, else specific slot
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
                    snapshot.children.forEach { slotNode ->
                        val idForSlot = slotNode.key ?: return@forEach

                        slotNode.children.forEach { eventNode ->
                            val hasStatus = eventNode.child("status").exists()
                            val hasLastUpdated = eventNode.child("last_updated").exists()
                            if (hasStatus && hasLastUpdated) {
                                list.add(eventNode.toSlotEvent(idForSlot))
                            }
                        }
                    }
                } else {
                    snapshot.children.forEach { eventNode ->
                        val hasStatus = eventNode.child("status").exists()
                        val hasLastUpdated = eventNode.child("last_updated").exists()
                        if (hasStatus && hasLastUpdated) {
                            list.add(eventNode.toSlotEvent(slotId))
                        }
                    }
                }

                // 1) Sort oldest -> newest (so we can remove consecutive repeats properly)
                val asc = list.sortedBy { it.lastUpdated }

                // 2) Remove consecutive duplicates PER SLOT (same status spam)
                val lastStatusBySlot = mutableMapOf<String, String>()
                val collapsed = mutableListOf<SlotEvent>()

                for (e in asc) {
                    val slotKey = e.slotId
                    val status = e.status.trim().lowercase(Locale.getDefault())
                    val last = lastStatusBySlot[slotKey]

                    // keep only if status changed (or first event for this slot)
                    if (last == null || last != status) {
                        collapsed.add(e)
                        lastStatusBySlot[slotKey] = status
                    }
                }

                // 3) Show newest first in UI
                val desc = collapsed.sortedByDescending { it.lastUpdated }
                onUpdate(desc)
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

    private fun DataSnapshot.toSlotEvent(slotId: String): SlotEvent {
        return SlotEvent(
            id = key ?: "",
            slotId = slotId,
            slotName = getString("slot_name") ?: "",
            status = getString("status") ?: "",
            lastUpdated = getString("last_updated") ?: "",
            weight = getDouble("weight") ?: 0.0,
            threshold = getDouble("threshold") ?: 0.0
        )
    }

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
