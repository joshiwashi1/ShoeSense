package com.shoesense.shoesense.AddSlot

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.shoesense.shoesense.Repository.AppConfig
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
    private val auth = FirebaseAuth.getInstance()

    private val stepG = 10 // grams

    /**
     * Get the shared slots path for the current site
     */
    private fun getSlotsBasePath(): String {
        val siteId = AppConfig.siteId ?: "home001"
        return "shoe_slots/$siteId"
    }

    /**
     * Get cache key that's shared for the site (not user-specific)
     */
    private fun getCacheKey(slot: Int): String {
        val siteId = AppConfig.siteId ?: "home001"
        return "${siteId}_slot_$slot"
    }

    /**
     * Helper function to get current user ID safely
     */
    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: "unknown"
    }

    /**
     * Load local cached values
     */
    fun load(slot: Int, isEdit: Boolean) {
        val cacheKey = getCacheKey(slot)
        val name = if (isEdit) prefs.getString("${cacheKey}_name", "") ?: "" else ""
        val grams = if (isEdit) prefs.getInt("${cacheKey}_threshold_g", 500) else 500
        view.showName(name)
        view.showThreshold(grams)
    }

    fun setName(slot: Int, name: String) {
        val cacheKey = getCacheKey(slot)
        prefs.edit { putString("${cacheKey}_name", name) }
    }

    fun setThreshold(slot: Int, grams: Int) {
        val cacheKey = getCacheKey(slot)
        prefs.edit { putInt("${cacheKey}_threshold_g", grams) }
    }

    fun updateExisting(slot: Int) {
        val cacheKey = getCacheKey(slot)
        val name = prefs.getString("${cacheKey}_name", "") ?: ""
        val gramsRaw = prefs.getInt("${cacheKey}_threshold_g", 500)

        if (name.isBlank()) {
            view.showToast("Please set a slot name.")
            return
        }

        val gramsSnapped = snapToStep(gramsRaw.toFloat())

        val nodeKey = "slot$slot"
        val payload = mapOf(
            "name" to name,
            "threshold" to gramsSnapped,          // 🔥 integer grams
            "last_updated" to isoNow(),
            "last_updated_by" to getCurrentUserId()
        )

        db.child(getSlotsBasePath()).child(nodeKey).updateChildren(payload)
            .addOnSuccessListener {
                view.showToast("Updated Slot $slot")
                view.onSaved()
            }
            .addOnFailureListener { e ->
                view.showToast("Update failed: ${e.message}")
            }
    }

    /**
     * CREATE PATH: Create a new shared slot
     */
    fun createNew() {
        val cacheKey = getCacheKey(1)
        val name = prefs.getString("${cacheKey}_name", "") ?: ""
        val gramsRaw = prefs.getInt("${cacheKey}_threshold_g", 500)

        if (name.isBlank()) {
            view.showToast("Please set a slot name.")
            return
        }

        val gramsSnapped = snapToStep(gramsRaw.toFloat())

        // Find next available slot number
        db.child(getSlotsBasePath()).get()
            .addOnSuccessListener { snap ->
                val nextSlotNumber = computeNextSlotNumber(snap)
                val slotId = "slot$nextSlotNumber"

                val payload = mapOf(
                    "name" to name,
                    "threshold" to gramsSnapped,      // 🔥 integer grams
                    "current_weight" to 0,
                    "status" to "empty",
                    "last_updated" to isoNow(),
                    "created_by" to getCurrentUserId(),
                    "last_updated_by" to getCurrentUserId()
                )

                db.child(getSlotsBasePath()).child(slotId).updateChildren(payload)
                    .addOnSuccessListener {
                        clearCache(1)
                        view.showToast("Added $name")
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

    private fun computeNextSlotNumber(snap: DataSnapshot): Int {
        if (!snap.exists()) return 1

        var maxSlot = 0
        for (child in snap.children) {
            val slotKey = child.key ?: continue
            if (slotKey.startsWith("slot")) {
                val slotNum = slotKey.removePrefix("slot").toIntOrNull() ?: continue
                if (slotNum > maxSlot) maxSlot = slotNum
            }
        }
        return maxSlot + 1
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

    private fun clearCache(slot: Int) {
        val cacheKey = getCacheKey(slot)
        prefs.edit {
            remove("${cacheKey}_name")
            remove("${cacheKey}_threshold_g")
        }
    }
}
