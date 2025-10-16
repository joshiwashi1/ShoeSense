package com.shoesense.shoesense.Model

import android.content.Context
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class SlotRepository(@Suppress("UNUSED_PARAMETER") ctx: Context) {

    private val ref: DatabaseReference = FirebaseDatabase.getInstance().reference.child("slots")
    private var listener: ValueEventListener? = null

    // cache latest list so presenter can compute next slot number
    private var cached: List<Slot> = emptyList()

    /**
     * Observe all slots live (up to maxSlots). Call stopObserving() in onDestroy/onStop.
     */
    fun observeSlots(
        maxSlots: Int,
        onUpdate: (List<Slot>) -> Unit,
        onError: (String) -> Unit
    ) {
        stopObserving()

        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    child.toSlot()
                }
                    // Avoid comparing nullable Int?; map nulls to a large value so they go last
                    .sortedBy { extractSlotNumber(it.name) ?: Int.MAX_VALUE }
                    .take(maxSlots)

                cached = list
                onUpdate(list)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        }
        ref.addValueEventListener(listener as ValueEventListener)
    }

    fun stopObserving() {
        listener?.let { ref.removeEventListener(it) }
        listener = null
    }

    /**
     * Compute next available slot number (1..maxSlots) based on cached snapshot.
     */
    fun nextSlotNumber(maxSlots: Int): Int {
        val used = cached.mapNotNull { extractSlotNumber(it.name) }.toSet()
        for (n in 1..maxSlots) if (!used.contains(n)) return n
        return (cached.size + 1).coerceAtMost(maxSlots)
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

        // read both forms for compatibility
        val threshold = getDoubleFlex("threshold") ?: 0.0
        val weight = getDoubleFlex("current_weight")

        // manual fallback
        val statusValue = getStringFlex("status")?.lowercase(Locale.getDefault())
        val manualOccupied = statusValue == "occupied"

        // hysteresis buffer (50 g) to reduce flicker
        val bufferKg = 0.05
        val autoOccupied = if (weight != null) weight >= (threshold - bufferKg) else null

        // precedence: if we have a weight, trust it; otherwise manual status
        val occupied = autoOccupied ?: manualOccupied

        val lastUpdated = getStringFlex("last_updated")
            ?: getStringFlex("lastUpdated")
            ?: isoNow()

        // OPTIONAL: mirror computed state back to DB only when it changes, to keep status readable
        if (autoOccupied != null && (manualOccupied != autoOccupied)) {
            // write only if different to avoid loops
            try {
                FirebaseDatabase.getInstance().reference
                    .child("slots").child(id)
                    .updateChildren(
                        mapOf(
                            "status" to if (autoOccupied) "occupied" else "empty",
                            "last_updated" to isoNow()
                        )
                    )
            } catch (_: Exception) { /* ignore */ }
        }

        return Slot(
            id = id,
            name = name,
            occupied = occupied,
            lastUpdated = lastUpdated
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
        val n = extractSlotNumber(this)
        return if (n != null) {
            "Slot $n"
        } else {
            replaceFirstChar { it.titlecase(Locale.getDefault()) }
        }
    }

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
