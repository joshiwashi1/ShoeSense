package com.shoesense.shoesense.AddSlot

interface AddSlotView {
    fun showName(name: String)
    fun showThreshold(grams: Int)
    fun showToast(msg: String)
    fun closeScreen()
    fun onSaved()
}

