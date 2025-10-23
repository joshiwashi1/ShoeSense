package com.shoesense.shoesense.history

import com.google.firebase.database.*
import com.shoesense.shoesense.Model.Slot

class HistoryPresenter(private val view: HistoryView) {

    private val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("slots")
    private var valueEventListener: ValueEventListener? = null

    // Move the logic here
    fun observeSlots(maxSlots: Int) {
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val slots = mutableListOf<Slot>()
                for (child in snapshot.children) {
                    val slot = child.getValue(Slot::class.java)
                    if (slot != null) slots.add(slot)
                }

                // Trim to the maxSlots limit
                val limitedSlots = if (slots.size > maxSlots) {
                    slots.takeLast(maxSlots)
                } else slots

                view.onSlotsUpdated(limitedSlots)
            }

            override fun onCancelled(error: DatabaseError) {
                view.onError(error.message)
            }
        }

        databaseRef.addValueEventListener(valueEventListener!!)
    }

    fun detach() {
        valueEventListener?.let { databaseRef.removeEventListener(it) }
    }
}
