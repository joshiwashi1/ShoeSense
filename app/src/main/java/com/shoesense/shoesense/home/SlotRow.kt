package com.shoesense.shoesense.home

import com.shoesense.shoesense.Model.Slot

sealed class SlotRow {
    data class Data(val slot: Slot): SlotRow()
    object Add: SlotRow()
}
