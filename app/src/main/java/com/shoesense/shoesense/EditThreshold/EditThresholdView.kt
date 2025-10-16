package com.shoesense.shoesense.EditThreshold

interface EditThresholdView {
    // render
    fun renderTitle(slotName: String)
    fun renderThresholdKgText(kgText: String)

    // close
    fun closeWithResult(thresholdGrams: Int)
    fun closeWithoutResult()

    // feedback (optional)
    fun showToast(msg: String)
}
