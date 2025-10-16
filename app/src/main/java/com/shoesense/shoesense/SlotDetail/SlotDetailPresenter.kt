package com.shoesense.shoesense.SlotDetail

import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class SlotDetailPresenter(private val view: SlotDetailView) {

    private var slotId: String = ""
    private var ref: DatabaseReference? = null
    private var listener: ValueEventListener? = null

    // local shadow state
    private var slotName: String = "Slot"
    private var status: String = "Empty"      // "Occupied" / "Empty"
    private var notifEnabled: Boolean = false
    private var thresholdGrams: Int = 500     // store in grams for UI; DB keeps kg (Double)
    private var lastUpdatedIso: String? = null

    fun attach(slotId: String) {
        this.slotId = slotId
        ref = FirebaseDatabase.getInstance().reference.child("slots").child(slotId)
        startObserving()
    }

    fun detach() {
        listener?.let { ref?.removeEventListener(it) }
        listener = null
        ref = null
    }

    // ------------------ Observe live ------------------
    private fun startObserving() {
        stopObserving()
        val r = ref ?: return
        listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                // name
                slotName = (snap.child("name").value as? String)?.ifBlank { slotId } ?: slotId

                // status: prefer string field "status" ("occupied"/"empty")
                val statusStr = (snap.child("status").value as? String)?.lowercase(Locale.getDefault())
                status = if (statusStr == "occupied") "Occupied" else "Empty"

                // threshold (kg Double) -> grams Int for UI/editor
                val thKg = when (val v = snap.child("threshold").value) {
                    is Number -> v.toDouble()
                    is String -> v.toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }
                thresholdGrams = (thKg * 1000).toInt()

                // last updated (ISO string)
                lastUpdatedIso = (snap.child("last_updated").value as? String)

                // Render
                view.showSlotName(slotName)
                view.showStatus(status)
                val occAt = if (status == "Occupied") lastUpdatedIso?.let { isoToLocalClock(it) } else null
                val empAt = if (status == "Empty") lastUpdatedIso?.let { isoToLocalClock(it) } else null
                view.showTimeline(occAt, empAt)

                // notifications (optional field)
                notifEnabled = when (val v = snap.child("notif_enabled").value) {
                    is Boolean -> v
                    is Number -> v.toInt() != 0
                    is String -> v.equals("true", true) || v == "1"
                    else -> false
                }
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

    // ------------------ UI reads ------------------
    fun getSlotName(): String = slotName
    fun getThresholdGrams(): Int = thresholdGrams

    // ------------------ Actions ------------------
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

    fun applyNewThreshold(newValueGrams: Int) {
        thresholdGrams = newValueGrams
        val kg = newValueGrams / 1000.0
        write(mapOf("threshold" to kg, "last_updated" to isoNow()))
        view.showToast("Threshold set to ${newValueGrams}g")
    }

    // --------------- Helpers ---------------
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
