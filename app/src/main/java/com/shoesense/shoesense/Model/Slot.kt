package com.shoesense.shoesense.Model

data class Slot(
    val name: String,
    var isOccupied: Boolean,
    var lastUpdated: String,
    val isAddButton: Boolean = false
)