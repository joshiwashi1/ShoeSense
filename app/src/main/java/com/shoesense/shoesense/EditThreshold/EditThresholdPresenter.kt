package com.shoesense.shoesense.EditThreshold
import kotlin.math.roundToInt

class EditThresholdPresenter(
    private val view: EditThresholdView
) {
    // 0..1000 g (<= 1 kg), 10 g steps
    private val minG = 0
    private val maxG = 1000
    private val stepG = 10

    private var slotName: String = "Slot"
    private var thresholdGrams: Int = 500  // default 0.5 kg, will be clamped

    fun onInit(initialSlotName: String?, initialThresholdG: Int?) {
        initialSlotName?.let { slotName = it }
        initialThresholdG?.let { thresholdGrams = clampToRange(it) }
        // Safety clamp in case default changed elsewhere
        thresholdGrams = clampToRange(thresholdGrams)

        view.renderTitle(slotName)
        view.renderThresholdKgText(formatKg(thresholdGrams))
    }

    fun onSliderChanged(value: Float) {
        // snap to step and clamp
        val snapped = snapToStep(value.toInt())
        thresholdGrams = clampToRange(snapped)
        view.renderThresholdKgText(formatKg(thresholdGrams))
    }

    fun onSaveClicked() {
        view.closeWithResult(thresholdGrams)
    }

    fun onCancelClicked() {
        view.closeWithoutResult()
    }

    fun onBackClicked() {
        view.closeWithoutResult()
    }

    // ===== helpers =====
    private fun clampToRange(v: Int) = v.coerceIn(minG, maxG)

    private fun snapToStep(v: Int): Int {
        if (stepG <= 1) return v
        val steps = (v.toFloat() / stepG).roundToInt()
        return steps * stepG
    }

    private fun formatKg(grams: Int): String {
        val kg = grams / 1000f
        return String.format("%.1f kg", kg) // e.g., 0.5 kg
    }

    // expose for Activity slider config
    fun minG() = minG.toFloat()
    fun maxG() = maxG.toFloat()
    fun stepG() = stepG.toFloat()
    fun currentG() = thresholdGrams.toFloat()
}