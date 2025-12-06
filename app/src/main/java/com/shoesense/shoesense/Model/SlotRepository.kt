package com.shoesense.shoesense.Model

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.shoesense.shoesense.Repository.AppConfig
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class SlotRepository(
    private val ctx: Context,
    private val enableHistoryLogging: Boolean = true   // dashboard: true, history screen: false
) {

    private val auth = FirebaseAuth.getInstance()
    private var ref: DatabaseReference? = null
    private var listener: ValueEventListener? = null

    // cache latest list so presenter can compute next slot number
    private var cached: List<Slot> = emptyList()

    // last status we logged per slot
    private val lastLoggedStatus: MutableMap<String, String> = mutableMapOf()

    // NEW: last time (ms) we logged anything for that slot
    private val lastLoggedTime: MutableMap<String, Long> = mutableMapOf()

    private fun getSlotsBasePath(): String {
        val siteId = AppConfig.siteId ?: "home001"
        return "shoe_slots/$siteId"
    }

    private fun getDatabaseReference(): DatabaseReference {
        return FirebaseDatabase.getInstance().reference.child(getSlotsBasePath())
    }

    fun observeSlots(
        maxSlots: Int,
        onUpdate: (List<Slot>) -> Unit,
        onError: (String) -> Unit
    ) {
        stopObserving()

        ref = getDatabaseReference()
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    child.toSlot()
                }
                    .sortedBy { extractSlotNumberFromId(it.id) ?: Int.MAX_VALUE }
                    .take(maxSlots)

                cached = list
                onUpdate(list)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        }
        ref?.addValueEventListener(listener as ValueEventListener)
    }

    fun stopObserving() {
        listener?.let { ref?.removeEventListener(it) }
        listener = null
    }

    fun nextSlotNumber(maxSlots: Int): Int {
        val used = cached.mapNotNull { extractSlotNumberFromId(it.id) }.toSet()
        for (n in 1..maxSlots) if (!used.contains(n)) return n
        return (cached.size + 1).coerceAtMost(maxSlots)
    }

    fun getSlot(slotId: String, onResult: (Slot?) -> Unit) {
        getDatabaseReference().child(slotId).get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.toSlot())
            }
            .addOnFailureListener { onResult(null) }
    }

    fun updateSlotStatus(slotId: String, occupied: Boolean, onComplete: (Boolean) -> Unit) {
        val updates = mapOf(
            "status" to if (occupied) "occupied" else "empty",
            "last_updated" to isoNow(),
            "last_updated_by" to (auth.currentUser?.uid ?: "unknown")
        )

        getDatabaseReference().child(slotId).updateChildren(updates)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun updateSlotThreshold(slotId: String, threshold: Double, onComplete: (Boolean) -> Unit) {
        val updates = mapOf(
            "threshold" to threshold,
            "last_updated" to isoNow(),
            "last_updated_by" to (auth.currentUser?.uid ?: "unknown")
        )

        getDatabaseReference().child(slotId).updateChildren(updates)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun updateSlotName(slotId: String, name: String, onComplete: (Boolean) -> Unit) {
        val updates = mapOf(
            "name" to name,
            "last_updated" to isoNow(),
            "last_updated_by" to (auth.currentUser?.uid ?: "unknown")
        )

        getDatabaseReference().child(slotId).updateChildren(updates)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun deleteSlot(slotId: String, onComplete: (Boolean) -> Unit) {
        getDatabaseReference().child(slotId).removeValue()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getAllSlots(onResult: (List<Slot>) -> Unit, onError: (String) -> Unit) {
        getDatabaseReference().get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.children.mapNotNull { child ->
                    child.toSlot()
                }.sortedBy { extractSlotNumberFromId(it.id) ?: Int.MAX_VALUE }
                onResult(list)
            }
            .addOnFailureListener { error ->
                onError(error.message ?: "Unknown error")
            }
    }

    // ================= Helpers =================
    private fun DataSnapshot.getDoubleFlex(key: String): Double? {
        val ch = child(key); if (!ch.exists()) return null
        return when (val v = ch.value) {
            is Number -> v.toDouble()
            is String -> v.toDoubleOrNull()
            else -> null
        }
    }

    private fun DataSnapshot.toSlot(): Slot? {
        val id = key ?: return null
        val name = getStringFlex("name") ?: id.capitalizeSlot()

        val threshold = getDoubleFlex("threshold") ?: 0.0
        val weight = getDoubleFlex("current_weight") ?: getDoubleFlex("currentWeight") ?: 0.0

        val statusValue = getStringFlex("status")?.lowercase(Locale.getDefault())
        val manualOccupied = statusValue == "occupied"

        val bufferKg = 0.05
        val autoOccupied = weight >= (threshold - bufferKg)
        val occupied = autoOccupied

        val lastUpdated = getStringFlex("last_updated")
            ?: getStringFlex("lastUpdated")
            ?: isoNow()

        val lastUpdatedBy = getStringFlex("last_updated_by") ?: "unknown"
        val createdBy = getStringFlex("created_by") ?: "unknown"

        // only the "logging" repo is allowed to write history
        if (enableHistoryLogging && manualOccupied != autoOccupied) {
            val newStatus = if (autoOccupied) "occupied" else "empty"
            val nowIso = isoNow()

            try {
                getDatabaseReference().child(id).updateChildren(
                    mapOf(
                        "status" to newStatus,
                        "last_updated" to nowIso,
                        "last_updated_by" to (auth.currentUser?.uid ?: "unknown")
                    )
                )

                // 🔴 add name here
                pushHistoryEvent(
                    slotId = id,
                    slotName = name,     // <--- NEW
                    status = newStatus,
                    weight = weight,
                    threshold = threshold
                )
            } catch (_: Exception) {
                // ignore
            }
        }

        return Slot(
            id = id,
            name = name,
            occupied = occupied,
            lastUpdated = lastUpdated,
            lastUpdatedBy = lastUpdatedBy,
            threshold = threshold,
            currentWeight = weight,
            status = if (occupied) "occupied" else "empty",
            createdBy = createdBy
        )
    }

    // ============ HISTORY LOGGER (TIME-DEBOUNCED) ============
    // ============ HISTORY LOGGER (NO DUPLICATES, BUT LOGS REAL CHANGES) ============
    private fun pushHistoryEvent(
        slotId: String,
        slotName: String,
        status: String,
        weight: Double,
        threshold: Double
    ) {
        val now = System.currentTimeMillis()
        val lastStatus = lastLoggedStatus[slotId]
        val lastTime = lastLoggedTime[slotId] ?: 0L

        // debounce only SAME status to avoid spam (e.g. occupied, occupied, occupied...)
        val debounceSameStatusMs = 5_000L

        // 1) If SAME status and very recent -> skip duplicate
        if (lastStatus != null &&
            status.equals(lastStatus, ignoreCase = true) &&
            now - lastTime < debounceSameStatusMs
        ) {
            return
        }

        // ✅ If status is different (occupied -> empty or empty -> occupied),
        // we ALWAYS log it, no time blocking.

        // update caches
        lastLoggedStatus[slotId] = status
        lastLoggedTime[slotId] = now

        val siteId = AppConfig.siteId ?: "home001"
        val ref = FirebaseDatabase.getInstance()
            .reference
            .child("shoe_history")
            .child(siteId)
            .child(slotId)
            .push()

        val data = mapOf(
            "status" to status,
            "last_updated" to isoNow(),
            "weight" to weight,
            "threshold" to threshold,
            "slot_name" to slotName        // 🔴 store name with event
        )

        ref.updateChildren(data)
    }


    private fun DataSnapshot.getStringFlex(vararg keys: String, default: String? = null): String? {
        for (k in keys) {
            val ch = child(k)
            if (!ch.exists()) continue
            when (val v = ch.value) {
                null -> continue
                is String -> return v
                is Number -> {
                    return if (v is Double && v % 1.0 == 0.0) v.toLong().toString() else v.toString()
                }
                is Boolean -> return v.toString()
                else -> return v.toString()
            }
        }
        return default
    }

    private fun DataSnapshot.getBooleanFlex(vararg keys: String, default: Boolean? = null): Boolean? {
        for (k in keys) {
            val ch = child(k)
            if (!ch.exists()) continue
            when (val v = ch.value) {
                is Boolean -> return v
                is Number -> return v.toInt() != 0
                is String -> return v.equals("true", ignoreCase = true) || v == "1"
            }
        }
        return default
    }

    private fun DataSnapshot.getLongFlex(vararg keys: String): Long? {
        for (k in keys) {
            val ch = child(k)
            if (!ch.exists()) continue
            when (val v = ch.value) {
                is Number -> return v.toLong()
                is String -> v.toLongOrNull()?.let { return it }
            }
        }
        return null
    }

    private fun String.capitalizeSlot(): String {
        if (!startsWith("slot", ignoreCase = true)) {
            return replaceFirstChar { it.titlecase(Locale.getDefault()) }
        }
        val n = extractSlotNumberFromId(this)
        return if (n != null) "Slot $n" else replaceFirstChar { it.titlecase(Locale.getDefault()) }
    }

    private fun extractSlotNumberFromId(slotId: String): Int? {
        val regex = Regex("""^slot(\d+)$""", RegexOption.IGNORE_CASE)
        val m = regex.find(slotId) ?: return null
        return m.groupValues.getOrNull(1)?.toIntOrNull()
    }

    fun extractSlotNumber(name: String): Int? {
        val regex = Regex("""slot\s*(\d+)""", RegexOption.IGNORE_CASE)
        val m = regex.find(name) ?: return null
        return m.groupValues.getOrNull(1)?.toIntOrNull()
    }

    private fun isoNow(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(System.currentTimeMillis())
    }
}
