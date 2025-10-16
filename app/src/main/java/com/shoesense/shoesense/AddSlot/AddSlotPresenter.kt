package com.shoesense.shoesense.AddSlot

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

class AddSlotPresenter(
    private val ctx: Context,
    private val view: AddSlotView
) {

    private val prefs = ctx.getSharedPreferences("shoe_slots", Context.MODE_PRIVATE)
    private val db = FirebaseDatabase.getInstance().reference.child("slots")

    // Keep your Add screen slider free, but when saving we normalize to 10 g steps
    private val stepG = 10 // grams

    fun load(slot: Int) {
        val name = prefs.getString(keyName(slot), "") ?: ""
        val thKg = prefs.getFloat(keyThreshold(slot), 0f)
        view.showName(name)
        view.showThreshold(thKg)
    }

    fun setName(slot: Int, name: String) {
        prefs.edit { putString(keyName(slot), name) }
    }

    fun getName(slot: Int): String? = prefs.getString(keyName(slot), null)

    fun setThreshold(slot: Int, valueKg: Float) {
        prefs.edit { putFloat(keyThreshold(slot), valueKg) }
    }

    fun save(slot: Int) {
        val name = prefs.getString(keyName(slot), "") ?: ""
        val thKgRaw = prefs.getFloat(keyThreshold(slot), 0f) // stored in kg

        if (name.isBlank()) {
            view.showToast("Please set a slot name.")
            return
        }

        // --- Normalize threshold to 10 g multiples (consistent with Edit Threshold) ---
        val gramsRaw = (thKgRaw * 1000f)
        val gramsSnapped = snapToStep(gramsRaw)
        val thKgSnapped = gramsSnapped / 1000.0  // Double for Firebase

        // node: /slots/slot1, /slots/slot2, ...
        val node = "slot$slot"

        val formattedDate = isoNow()

        val payload = mapOf(
            "name" to name,
            "threshold" to thKgSnapped,  // normalized to 10 g steps
            "current_weight" to 0.0,     // IoT not ready yet
            "status" to "empty",         // manual until sensor updates
            "last_updated" to formattedDate
        )

        db.child(node).updateChildren(payload)
            .addOnSuccessListener {
                view.showToast("Saved: $name (Threshold: ${String.format(Locale.US, "%.2f", thKgSnapped)} kg)")
                view.closeScreen()
                view.onSaved()
            }
            .addOnFailureListener { e ->
                view.showToast("Save failed: ${e.message}")
            }
    }

    // --- Helpers ---

    private fun snapToStep(valueInGrams: Float): Int {
        // round to nearest multiple of 10 g
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
