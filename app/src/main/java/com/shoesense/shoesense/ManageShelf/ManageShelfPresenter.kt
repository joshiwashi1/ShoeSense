package com.shoesense.shoesense.ManageShelf

import android.content.Context
import com.google.firebase.database.*
import com.shoesense.shoesense.Model.Slot
import com.shoesense.shoesense.Model.SlotRepository
import com.shoesense.shoesense.Repository.AppConfig

class ManageShelfPresenter {

    private var view: ManageShelfView? = null
    private lateinit var repo: SlotRepository

    private val db: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }

    // Config is now per-site, same idea as SlotRepository
    private lateinit var cfgRef: DatabaseReference

    private var capListener: ValueEventListener? = null
    private var currentCap = 6

    fun attach(v: ManageShelfView, ctx: Context) {
        view = v
        repo = SlotRepository(ctx)

        val siteId = AppConfig.siteId ?: "home001"
        cfgRef = db.reference.child("shelf_config").child(siteId)

        startObserving()
    }

    fun detach() {
        capListener?.let { cfgRef.removeEventListener(it) }
        if (this::repo.isInitialized) repo.stopObserving()
        view = null
    }

    // If you ever hook a pull-to-refresh, you can keep this no-op or
    // re-trigger observe. For now, it's effectively not used.
    fun onRefresh() {
        // no-op, live listeners already keep things updated
    }

    /**
     * User tapped "Save" on Maximum Slot Capacity.
     * This ONLY updates the config; other screens (e.g., AddSlot)
     * should respect this max when creating new slots.
     */
    fun onSaveCapacity(text: String) {
        val value = text.toIntOrNull()
        if (value == null || value !in 1..24) {
            view?.showMessage("Enter a limit from 1–24")
            return
        }

        cfgRef.child("max_slots").setValue(value)
            .addOnSuccessListener {
                currentCap = value
                view?.showCapacity(currentCap)
                view?.showMessage("Limit saved")
            }
            .addOnFailureListener { e ->
                view?.showMessage(e.message ?: "Save failed")
            }
    }

    // ---- internal ----
    private fun startObserving() {
        view?.showLoading(true)

        capListener?.let { cfgRef.removeEventListener(it) }
        capListener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                // 1) Get current capacity (default 6 if nothing yet)
                currentCap = snap.child("max_slots").getValue(Int::class.java) ?: 6
                view?.showCapacity(currentCap)

                // 2) Observe slots for this site via repository,
                //    only for stats (total / occupied / empty)
                repo.observeSlots(
                    maxSlots = currentCap,
                    onUpdate = { list ->
                        view?.showSlots(list)   // your Activity currently no-ops this

                        val total = list.size
                        val occupied = list.count { it.occupied }
                        val empty = currentCap - occupied
                            .coerceAtLeast(0)  // avoid negative just in case

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
}
