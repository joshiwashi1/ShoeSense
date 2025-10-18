package com.shoesense.shoesense.ManageShelf

import android.content.Context
import com.google.firebase.database.*
import com.shoesense.shoesense.Model.Slot
import com.shoesense.shoesense.Model.SlotRepository
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.Date

class ManageShelfPresenter {

    private var view: ManageShelfView? = null
    private lateinit var repo: SlotRepository

    private val db: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }
    private val cfgRef: DatabaseReference by lazy { db.reference.child("shelf_config") }
    private val slotsRef: DatabaseReference by lazy { db.reference.child("slots") }

    private var capListener: ValueEventListener? = null
    private var currentCap = 6

    fun attach(v: ManageShelfView, ctx: Context) {
        view = v
        repo = SlotRepository(ctx)
        startObserving()
    }

    fun detach() {
        capListener?.let { cfgRef.removeEventListener(it) }
        if (this::repo.isInitialized) repo.stopObserving()
        view = null
    }

    fun onRefresh() {
        view?.showLoading(true)
        // one-time read; live observer will also push the latest list
        slotsRef.get().addOnCompleteListener { view?.showLoading(false) }
    }

    fun onSaveCapacity(text: String) {
        val value = text.toIntOrNull()
        if (value == null || value !in 1..24) {
            view?.showMessage("Enter a capacity from 1–24")
            return
        }
        cfgRef.child("max_slots").setValue(value)
            .addOnSuccessListener { view?.showMessage("Capacity saved") }
            .addOnFailureListener { e -> view?.showMessage(e.message ?: "Save failed") }
    }

    fun onAddSlot() {
        // Use repository's cached snapshot to compute next number
        val n = repo.nextSlotNumber(currentCap)
        if (n > currentCap) {
            view?.showMessage("Reached max capacity ($currentCap)")
            return
        }
        val id = "slot$n"
        val payload = mapOf(
            "name" to "Slot $n",
            "status" to "empty",
            "last_updated" to isoNow()
        )
        slotsRef.child(id).setValue(payload)
            .addOnSuccessListener { view?.showMessage("Added $id") }
            .addOnFailureListener { e -> view?.showMessage(e.message ?: "Add failed") }
    }

    fun onSlotClicked(slot: Slot) {
        view?.openRenameDialog(slot)
    }

    fun onRenameConfirmed(slotId: String, newName: String) {
        val finalName = newName.ifBlank { slotId }
        slotsRef.child(slotId).updateChildren(
            mapOf(
                "name" to finalName,
                "last_updated" to isoNow()
            )
        ).addOnSuccessListener { view?.showMessage("Saved") }
            .addOnFailureListener { e -> view?.showMessage(e.message ?: "Update failed") }
    }

    // ---- internal ----
    private fun startObserving() {
        view?.showLoading(true)

        // observe capacity
        capListener?.let { cfgRef.removeEventListener(it) }
        capListener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                currentCap = snap.child("max_slots").getValue(Int::class.java) ?: 6
                view?.showCapacity(currentCap)

                // observe slots with cap
                repo.observeSlots(
                    maxSlots = currentCap,
                    onUpdate = { list ->
                        view?.showSlots(list)
                        val total = list.size
                        val occupied = list.count { it.occupied }
                        val empty = total - occupied
                        view?.updateStats(total, occupied, empty)
                        view?.showLoading(false)
                    },
                    onError = { err ->
                        view?.showMessage(err)
                        view?.showLoading(false)
                    }
                )
            }
            override fun onCancelled(error: DatabaseError) {
                view?.showMessage(error.message)
                view?.showLoading(false)
            }
        }
        cfgRef.addValueEventListener(capListener as ValueEventListener)
    }

    private fun isoNow(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }
}
