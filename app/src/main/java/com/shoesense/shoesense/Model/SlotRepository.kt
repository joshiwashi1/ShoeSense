package com.shoesense.shoesense.Model

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.shoesense.shoesense.Repository.AppConfig
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class SlotRepository(private val ctx: Context) {

    private val auth = FirebaseAuth.getInstance()
    private var ref: DatabaseReference? = null
    private var listener: ValueEventListener? = null

    // cache latest list so presenter can compute next slot number
    private var cached: List<Slot> = emptyList()

    /**
     * Get the shared slots path for the current site
     */
    private fun getSlotsBasePath(): String {
        val siteId = AppConfig.siteId ?: "home001"
        return "shoe_slots/$siteId" // Shared across all users in the site
    }

    /**
     * Initialize database reference with proper path
     */
    private fun getDatabaseReference(): DatabaseReference {
        return FirebaseDatabase.getInstance().reference.child(getSlotsBasePath())
    }

    /**
     * Observe all slots live (up to maxSlots). Call stopObserving() in onDestroy/onStop.
     */
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
                    // Sort by slot number extracted from slot ID (slot1, slot2, etc.)
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

    /**
     * Compute next available slot number (1..maxSlots) based on cached snapshot.
     */
    fun nextSlotNumber(maxSlots: Int): Int {
        val used = cached.mapNotNull { extractSlotNumberFromId(it.id) }.toSet()
        for (n in 1..maxSlots) if (!used.contains(n)) return n
        return (cached.size + 1).coerceAtMost(maxSlots)
    }

    /**
     * Get a specific slot by ID
     */
    fun getSlot(slotId: String, onResult: (Slot?) -> Unit) {
        getDatabaseReference().child(slotId).get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.toSlot())
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    /**
     * Update slot status
     */
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

    /**
     * Update slot threshold
     */
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

    /**
     * Update slot name
     */
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

    /**
     * Delete a slot
     */
    fun deleteSlot(slotId: String, onComplete: (Boolean) -> Unit) {
        getDatabaseReference().child(slotId).removeValue()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Get all slots once (non-reactive)
     */
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

        // read values
        val threshold = getDoubleFlex("threshold") ?: 0.0
        val weight = getDoubleFlex("current_weight") ?: getDoubleFlex("currentWeight") ?: 0.0

        // manual fallback
        val statusValue = getStringFlex("status")?.lowercase(Locale.getDefault())
        val manualOccupied = statusValue == "occupied"

        // hysteresis to reduce flicker (50g)
        val bufferKg = 0.05
        val autoOccupied = weight >= (threshold - bufferKg)

        // 🔥 always trust weight-based detection (0 should also mean empty)
        val occupied = autoOccupied

        val lastUpdated = getStringFlex("last_updated")
            ?: getStringFlex("lastUpdated")
            ?: isoNow()

        val lastUpdatedBy = getStringFlex("last_updated_by") ?: "unknown"
        val createdBy = getStringFlex("created_by") ?: "unknown"

        // 🔥 Mirror back status when different (even when weight = 0)
        if (manualOccupied != autoOccupied) {
            try {
                getDatabaseReference().child(id).updateChildren(
                    mapOf(
                        "status" to if (autoOccupied) "occupied" else "empty",
                        "last_updated" to isoNow(),
                        "last_updated_by" to (auth.currentUser?.uid ?: "unknown")
                    )
                )
            } catch (_: Exception) { /* ignore */ }
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


    // -------- Flexible getters that never crash on type mismatch --------
    private fun DataSnapshot.getStringFlex(vararg keys: String, default: String? = null): String? {
        for (k in keys) {
            val ch = child(k)
            if (!ch.exists()) continue
            when (val v = ch.value) {
                null -> continue
                is String -> return v
                is Number -> {
                    // Avoid 123.0 when it was an integer
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
        return if (n != null) {
            "Slot $n"
        } else {
            replaceFirstChar { it.titlecase(Locale.getDefault()) }
        }
    }

    /**
     * Extract slot number from slot ID (e.g., "slot1" -> 1, "slot2" -> 2)
     */
    private fun extractSlotNumberFromId(slotId: String): Int? {
        val regex = Regex("""^slot(\d+)$""", RegexOption.IGNORE_CASE)
        val m = regex.find(slotId) ?: return null
        return m.groupValues.getOrNull(1)?.toIntOrNull()
    }

    /**
     * Extract slot number from slot name (e.g., "Slot 1" -> 1, "slot2" -> 2)
     */
    fun extractSlotNumber(name: String): Int? {
        // match "Slot 3" or "slot3"
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