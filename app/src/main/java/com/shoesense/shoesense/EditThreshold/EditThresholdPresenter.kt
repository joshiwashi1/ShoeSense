package com.shoesense.shoesense.EditThreshold

import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

class EditThresholdPresenter(
    private val view: EditThresholdView,
    private val slotId: String
) {

    // Same path as before
    private val db = FirebaseDatabase.getInstance()
        .reference
        .child("slots")

    private val minG = 0
    private val maxG = 1000
    private val stepG = 10

    private var slotName: String = "Slot"
    private var thresholdGrams: Int = 500

    fun onInit(initialSlotName: String?, initialThresholdG: Int?) {
        initialSlotName?.let { slotName = it }

        // Clamp + snap initial value (if provided)
        initialThresholdG?.let {
            val clamped = clampToRange(it)
            thresholdGrams = snapToStep(clamped)
        }

        // Normalize default if nothing was passed
        thresholdGrams = snapToStep(clampToRange(thresholdGrams))

        view.renderTitle(slotName)
        view.renderThresholdKgText(formatG(thresholdGrams))
    }

    fun onSliderChanged(value: Float) {
        // Slider gives float; we keep it as snapped integer grams
        val snapped = snapToStep(value.toInt())
        thresholdGrams = clampToRange(snapped)
        view.renderThresholdKgText(formatG(thresholdGrams))
    }

    fun onSaveClicked() {
        if (slotId.isBlank()) {
            view.showToast("Error: Slot ID missing.")
            view.closeWithoutResult()
            return
        }

        // Safety: normalize again before saving
        thresholdGrams = snapToStep(clampToRange(thresholdGrams))
        saveToFirebase(thresholdGrams)
    }

    fun onCancelClicked() = view.closeWithoutResult()
    fun onBackClicked() = view.closeWithoutResult()

    // --- Save to Firebase (integer grams) ---
    private fun saveToFirebase(grams: Int) {
        val nowIso = isoNow()
        val payload = mapOf(
            "threshold" to grams,      // store integer grams
            "last_updated" to nowIso
        )

        db.child(slotId).updateChildren(payload)
            .addOnSuccessListener {
                view.showToast("Threshold updated to ${formatG(grams)}")
                view.closeWithResult(grams)
            }
            .addOnFailureListener { e ->
                view.showToast("Failed to save: ${e.message}")
            }
    }

    // --- Helpers ---

    private fun isoNow(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(System.currentTimeMillis())
    }

    private fun clampToRange(v: Int): Int = v.coerceIn(minG, maxG)

    private fun snapToStep(v: Int): Int {
        if (stepG <= 1) return v
        val steps = (v.toFloat() / stepG).roundToInt()
        return steps * stepG
    }

    private fun formatG(grams: Int): String = "$grams g"

    fun minG() = minG.toFloat()
    fun maxG() = maxG.toFloat()
    fun stepG() = stepG.toFloat()
}
