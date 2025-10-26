package com.shoesense.shoesense.AddSlot

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

class AddSlotPresenter(
    private val ctx: Context,
    private val view: AddSlotView
) {

    private val prefs = ctx.getSharedPreferences("shoe_slots", Context.MODE_PRIVATE)
    private val db = FirebaseDatabase.getInstance().reference

    // UI stores kg in prefs; we snap to 10 g on save to keep parity with your “Edit Threshold”
    private val stepG = 10 // grams

    /**
     * Load local cached values (only meaningful for edit; for create this will be blank/default)
     */
    fun load(slot: Int, isEdit: Boolean) {
        val name = if (isEdit) prefs.getString(keyName(slot), "") ?: "" else ""
        val thKg = if (isEdit) prefs.getFloat(keyThreshold(slot), 0f) else 0.0f
        view.showName(name)
        view.showThreshold(thKg)
    }

    fun setName(slot: Int, name: String) {
        prefs.edit { putString(keyName(slot), name) }
    }

    fun setThreshold(slot: Int, valueKg: Float) {
        prefs.edit { putFloat(keyThreshold(slot), valueKg) }
    }

    fun updateExisting(slot: Int) {
        val name = prefs.getString(keyName(slot), "") ?: ""
        val thKgRaw = prefs.getFloat(keyThreshold(slot), 0f)

        if (name.isBlank()) {
            view.showToast("Please set a slot name.")
            return
        }

        val gramsSnapped = snapToStep(thKgRaw * 1000f)
        val thKgSnapped = gramsSnapped / 1000.0

        val nodeKey = "slot$slot" // deterministic existing node
        val payload = mapOf(
            "name" to name,
            "threshold" to thKgSnapped,
            "last_updated" to isoNow()
        )

        db.child("slots").child(nodeKey).updateChildren(payload)
            .addOnSuccessListener {
                view.showToast("Updated Slot $slot")
                view.onSaved()
            }
            .addOnFailureListener { e ->
                view.showToast("Update failed: ${e.message}")
            }
    }

    /**
     * CREATE PATH: pushes a brand-new child node under /slots with a unique key.
     * Also assigns the next available displayIndex (1-based) so Home can sort or render.
     */
    fun createNew() {
        // Read name/threshold from temp cache slot 1 (or any), since create has no final index yet
        val name = prefs.getString("pending_new_name", null)
            ?: prefs.getString(keyName(1), "") ?: "" // fallback if user already set via UI
        val thKgRaw = prefs.getFloat("pending_new_threshold", prefs.getFloat(keyThreshold(1), 0f))

        if (name.isBlank()) {
            view.showToast("Please set a slot name.")
            return
        }

        val gramsSnapped = snapToStep(thKgRaw * 1000f)
        val thKgSnapped = gramsSnapped / 1000.0

        // Find next display index by scanning existing slots count
        db.child("slots").get()
            .addOnSuccessListener { snap ->
                val nextIndex = computeNextIndex(snap)

                val ref = db.child("slots").push() // unique id (e.g., -NvSg...XyZ)
                val slotId = ref.key ?: System.currentTimeMillis().toString()

                val payload = mapOf(
                    "id" to slotId,
                    "displayIndex" to nextIndex,   // keep a human/ordering index
                    "name" to name,
                    "threshold" to thKgSnapped,
                    "current_weight" to 0.0,
                    "status" to "empty",
                    "last_updated" to isoNow()
                )

                ref.updateChildren(payload)
                    .addOnSuccessListener {
                        view.showToast("Added slot #$nextIndex")
                        view.onSaved()
                    }
                    .addOnFailureListener { e ->
                        view.showToast("Create failed: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                view.showToast("Read failed: ${e.message}")
            }
    }

    // --- Helpers ---

    private fun computeNextIndex(snap: DataSnapshot): Int {
        if (!snap.exists() || !snap.hasChildren()) return 1
        var maxIdx = 0
        for (child in snap.children) {
            val idx = child.child("displayIndex").getValue<Int>() ?: 0
            if (idx > maxIdx) maxIdx = idx
        }
        return maxIdx + 1
    }

    private fun snapToStep(valueInGrams: Float): Int {
        val steps = (valueInGrams / stepG).roundToInt()
        return steps * stepG
    }

    private fun isoNow(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(System.currentTimeMillis())
    }

    private fun keyName(slot: Int) = "slot_${slot}_name"
    private fun keyThreshold(slot: Int) = "slot_${slot}_threshold"
}
