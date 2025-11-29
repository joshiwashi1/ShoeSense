package com.shoesense.shoesense.SlotDetail

import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class SlotDetailPresenter(private val view: SlotDetailView) {

    private var siteId: String = ""
    private var slotId: String = ""
    private var ref: DatabaseReference? = null
    private var listener: ValueEventListener? = null

    private var slotName: String = "Slot"
    private var status: String = "Empty"
    private var notifEnabled: Boolean = false
    private var thresholdGrams: Int = 500
    private var lastUpdatedIso: String? = null

    fun attach(siteId: String, slotId: String) {
        this.siteId = siteId
        this.slotId = slotId

        // /shoe_slots/{siteId}/{slotId}
        ref = FirebaseDatabase.getInstance().reference
            .child("shoe_slots")
            .child(siteId)
            .child(slotId)

        startObserving()
    }

    fun detach() {
        listener?.let { ref?.removeEventListener(it) }
        listener = null
        ref = null
    }

    private fun startObserving() {
        stopObserving()
        val r = ref ?: return

        listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                if (!snap.exists()) {
                    view.showToast("Slot not found")
                    view.navigateBack()
                    return
                }

                // name
                slotName = (snap.child("name").getValue(String::class.java)
                    ?: slotId).ifBlank { slotId }

                // status from "status": "occupied"/"empty"
                val statusStr = (snap.child("status").getValue(String::class.java)
                    ?: "empty").lowercase(Locale.getDefault())
                status = if (statusStr == "occupied") "Occupied" else "Empty"

                // 🔥 threshold stored in GRAMS now
                val thVal = snap.child("threshold").value
                thresholdGrams = when (thVal) {
                    is Number -> thVal.toInt()
                    is String -> thVal.toIntOrNull() ?: 0
                    else -> 0
                }

                // last_updated
                lastUpdatedIso = snap.child("last_updated").getValue(String::class.java)

                // render
                view.showSlotName(slotName)
                view.showStatus(status)

                val occAt = if (status == "Occupied")
                    lastUpdatedIso?.let { isoToLocalClock(it) } else null
                val empAt = if (status == "Empty")
                    lastUpdatedIso?.let { isoToLocalClock(it) } else null

                view.showTimeline(occAt, empAt)

                // (notifEnabled not in your DB yet; default false)
                view.setNotificationsEnabled(notifEnabled)
            }

            override fun onCancelled(error: DatabaseError) {
                view.showToast("Load failed: ${error.message}")
            }
        }

        r.addValueEventListener(listener as ValueEventListener)
    }

    private fun stopObserving() {
        listener?.let { ref?.removeEventListener(it) }
        listener = null
    }

    // --- Public getters for Activity ---
    fun getSlotName(): String = slotName
    fun getThresholdGrams(): Int = thresholdGrams

    // --- Actions ---
    fun onBackClicked() = view.navigateBack()

    fun onRenameClicked() {
        view.askForNewName(slotName) { newName ->
            val trimmed = newName?.trim().orEmpty()
            if (trimmed.isNotEmpty()) {
                write(mapOf("name" to trimmed, "last_updated" to isoNow()))
            }
        }
    }

    fun onDeleteClicked() {
        view.confirmDelete(slotName) { confirmed ->
            if (confirmed) {
                ref?.removeValue()
                    ?.addOnSuccessListener {
                        view.showToast("Deleted $slotName")
                        view.navigateBack()
                    }
                    ?.addOnFailureListener { e ->
                        view.showToast("Delete failed: ${e.message}")
                    }
            }
        }
    }

    fun onNotificationsToggled(enabled: Boolean) {
        notifEnabled = enabled
        write(mapOf("notif_enabled" to enabled, "last_updated" to isoNow()))
    }

    // 🔥 now writes integer grams instead of kg float
    fun applyNewThreshold(newValueGrams: Int) {
        thresholdGrams = newValueGrams
        write(
            mapOf(
                "threshold" to newValueGrams,   // store grams
                "last_updated" to isoNow()
            )
        )
        view.showToast("Threshold set to ${newValueGrams} g")
    }

    private fun write(payload: Map<String, Any>) {
        ref?.updateChildren(payload)
            ?.addOnFailureListener { e -> view.showToast("Save failed: ${e.message}") }
    }

    private fun isoNow(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(System.currentTimeMillis())
    }

    private fun isoToLocalClock(iso: String): String {
        return try {
            val inFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val outFmt = SimpleDateFormat("h:mm a", Locale.getDefault())
            outFmt.format(inFmt.parse(iso)!!)
        } catch (_: Exception) {
            iso
        }
    }
}
