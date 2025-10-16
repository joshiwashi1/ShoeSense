package com.shoesense.shoesense.AddSlot

interface AddSlotView {
    fun showName(name: String)
    fun showThreshold(value: Float)
    fun showToast(msg: String)
    fun closeScreen()
    fun onSaved()
}
