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
        return "shoe_slots/$siteId" // Shared across all users in the site
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
        val thKg = if (isEdit) prefs.getFloat("${cacheKey}_threshold", 0f) else 0.0f
        view.showName(name)
        view.showThreshold(thKg)
    }

    fun setName(slot: Int, name: String) {
        val cacheKey = getCacheKey(slot)
        prefs.edit { putString("${cacheKey}_name", name) }
    }

    fun setThreshold(slot: Int, valueKg: Float) {
        val cacheKey = getCacheKey(slot)
        prefs.edit { putFloat("${cacheKey}_threshold", valueKg) }
    }

    fun updateExisting(slot: Int) {
        val cacheKey = getCacheKey(slot)
        val name = prefs.getString("${cacheKey}_name", "") ?: ""
        val thKgRaw = prefs.getFloat("${cacheKey}_threshold", 0f)

        if (name.isBlank()) {
            view.showToast("Please set a slot name.")
            return
        }

        val gramsSnapped = snapToStep(thKgRaw * 1000f)
        val thKgSnapped = gramsSnapped / 1000.0

        val nodeKey = "slot$slot"
        val payload = mapOf(
            "name" to name,
            "threshold" to thKgSnapped,
            "last_updated" to isoNow(),
            "last_updated_by" to getCurrentUserId() // Fixed: using helper function
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
        val thKgRaw = prefs.getFloat("${cacheKey}_threshold", 0f)

        if (name.isBlank()) {
            view.showToast("Please set a slot name.")
            return
        }

        val gramsSnapped = snapToStep(thKgRaw * 1000f)
        val thKgSnapped = gramsSnapped / 1000.0

        // Find next available slot number
        db.child(getSlotsBasePath()).get()
            .addOnSuccessListener { snap ->
                val nextSlotNumber = computeNextSlotNumber(snap)
                val slotId = "slot$nextSlotNumber"

                val payload = mapOf(
                    "name" to name,
                    "threshold" to thKgSnapped,
                    "current_weight" to 0.0,
                    "status" to "empty",
                    "last_updated" to isoNow(),
                    "created_by" to getCurrentUserId(), // Fixed: using helper function
                    "last_updated_by" to getCurrentUserId() // Fixed: using helper function
                )

                db.child(getSlotsBasePath()).child(slotId).updateChildren(payload)
                    .addOnSuccessListener {
                        // Clear cache after successful creation
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
            remove("${cacheKey}_threshold")
        }
    }
}