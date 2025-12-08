package com.shoesense.shoesense.home

import android.content.Context
import com.google.firebase.database.*
import com.shoesense.shoesense.Model.Slot
import com.shoesense.shoesense.Model.SlotRepository
import com.shoesense.shoesense.Repository.AppConfig

class HomeDashboardPresenter(private val ctx: Context) {

    private var view: HomeDashboardView? = null
    private val repo = SlotRepository(ctx)

    // Firebase for capacity
    private val db: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }
    private lateinit var cfgRef: DatabaseReference

    private var latestSlots: List<Slot> = emptyList()
    private var maxSlots: Int = 6

    private var capacityListener: ValueEventListener? = null
    private var observingSlots = false

    fun attach(v: HomeDashboardView) {
        view = v

        val siteId = AppConfig.siteId ?: "home001"
        cfgRef = db.reference.child("shelf_config").child(siteId)

        observeCapacity()
    }

    fun detach() {
        capacityListener?.let { cfgRef.removeEventListener(it) }
        repo.stopObserving()
        observingSlots = false
        view = null
    }

    fun load() {
        // live listeners handle updates; this is just a safety to reattach if needed
        if (!observingSlots) {
            observeCapacity()
        }
    }

    /** Listen to max_slots changes for this site */
    private fun observeCapacity() {
        if (::cfgRef.isInitialized && capacityListener == null) {
            capacityListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    maxSlots = snapshot.child("max_slots").getValue(Int::class.java) ?: 6

                    // When capacity changes, restart slot observation with new limit
                    if (observingSlots) {
                        repo.stopObserving()
                        observingSlots = false
                    }
                    observeSlots()
                }

                override fun onCancelled(error: DatabaseError) {
                    view?.showError(error.message)
                }
            }
            cfgRef.addValueEventListener(capacityListener as ValueEventListener)
        } else if (::cfgRef.isInitialized && !observingSlots) {
            // if listener already exists but slots not yet observed
            observeSlots()
        }
    }

    /** Observe slots with the current maxSlots */
    private fun observeSlots() {
        observingSlots = true
        repo.observeSlots(
            maxSlots = maxSlots,
            onUpdate = { slots ->
                latestSlots = slots
                renderCurrent()
            },
            onError = { msg -> view?.showError(msg) }
        )
    }

    private fun renderCurrent() {
        val rows = buildRows(latestSlots)
        view?.render(rows)
    }

    private fun buildRows(slots: List<Slot>): List<SlotRow> {
        val dataRows = slots.map { SlotRow.Data(it) }
        // Only show "Add" card if we are below the configured capacity
        return if (dataRows.size < maxSlots) dataRows + SlotRow.Add else dataRows
    }

    fun nextSlotNumber(): Int = repo.nextSlotNumber(maxSlots)

    // Used by Activity before launching Add in CREATE mode
    fun canAddMore(): Boolean = latestSlots.size < maxSlots

    fun getMaxSlots(): Int = maxSlots

    fun onAddClicked() = view?.openAddSlot()
    fun onSlotClicked(s: Slot) = view?.openSlotDetail(s)
}
