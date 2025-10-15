package com.shoesense.shoesense.AddSlot

import android.content.Context
import androidx.core.content.edit

class AddSlotPresenter(
    private val ctx: Context,
    private val view: AddSlotView
) {

    private val prefs = ctx.getSharedPreferences("shoe_slots", Context.MODE_PRIVATE)

    fun load(slot: Int) {
        val name = prefs.getString(keyName(slot), "") ?: ""
        val th = prefs.getFloat(keyThreshold(slot), 0f)
        view.showName(name)
        view.showThreshold(th)
    }

    fun setName(slot: Int, name: String) {
        prefs.edit { putString(keyName(slot), name) }
    }

    fun getName(slot: Int): String? = prefs.getString(keyName(slot), null)

    fun setThreshold(slot: Int, value: Float) {
        prefs.edit { putFloat(keyThreshold(slot), value) }
    }

    fun save(slot: Int) {
        val name = prefs.getString(keyName(slot), "") ?: ""
        val th = prefs.getFloat(keyThreshold(slot), 0f)

        if (name.isBlank()) {
            view.showToast("Please set a slot name.")
            return
        }

        // Persist already done in setters; here we could also sync to backend if needed
        view.showToast("Saved: $name (Threshold: ${"%.1f".format(th)} kg)")
        view.closeScreen()
    }

    private fun keyName(slot: Int) = "slot_${slot}_name"
    private fun keyThreshold(slot: Int) = "slot_${slot}_threshold"
}
