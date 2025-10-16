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
    private val db = FirebaseDatabase.getInstance().reference.child("slots")

    private val minG = 0
    private val maxG = 1000
    private val stepG = 10

    private var slotName: String = "Slot"
    private var thresholdGrams: Int = 500

    fun onInit(initialSlotName: String?, initialThresholdG: Int?) {
        initialSlotName?.let { slotName = it }
        // ✅ clamp + snap the incoming value so it always matches stepSize(10)
        initialThresholdG?.let {
            val clamped = clampToRange(it)
            thresholdGrams = snapToStep(clamped)   // <--- important
        }
        // ✅ also normalize the default (if no extra passed)
        thresholdGrams = snapToStep(clampToRange(thresholdGrams))

        view.renderTitle(slotName)
        view.renderThresholdKgText(formatKg(thresholdGrams))
    }

    fun onSliderChanged(value: Float) {
        val snapped = snapToStep(value.toInt())      // already snaps
        thresholdGrams = clampToRange(snapped)
        view.renderThresholdKgText(formatKg(thresholdGrams))
    }

    fun onSaveClicked() {
        if (slotId.isBlank()) {
            view.showToast("Error: Slot ID missing.")
            view.closeWithoutResult()
            return
        }
        // ✅ double-ensure normalization before saving
        thresholdGrams = snapToStep(clampToRange(thresholdGrams))
        saveToFirebase(thresholdGrams)
    }

    fun onCancelClicked() = view.closeWithoutResult()
    fun onBackClicked() = view.closeWithoutResult()

    // --- Save to Firebase ---
    private fun saveToFirebase(grams: Int) {
        val thresholdKg = grams / 1000.0
        val isoNow = isoNow()
        val payload = mapOf(
            "threshold" to thresholdKg,
            "last_updated" to isoNow
        )

        db.child(slotId).updateChildren(payload)
            .addOnSuccessListener {
                view.showToast("Threshold updated to ${formatKg(grams)}")
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

    private fun clampToRange(v: Int) = v.coerceIn(minG, maxG)

    private fun snapToStep(v: Int): Int {
        if (stepG <= 1) return v
        val steps = (v.toFloat() / stepG).roundToInt()  // nearest multiple of 10
        return steps * stepG
    }

    private fun formatKg(grams: Int): String {
        val kg = grams / 1000f
        return String.format("%.1f kg", kg)
    }

    fun minG() = minG.toFloat()
    fun maxG() = maxG.toFloat()
    fun stepG() = stepG.toFloat()
}
