package com.shoesense.shoesense.Model

data class Slot(
    val id: String,
    val name: String,
    val occupied: Boolean,
    val lastUpdated: String
)