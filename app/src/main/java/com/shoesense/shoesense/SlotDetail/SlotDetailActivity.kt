// SlotDetailActivity.kt
package com.shoesense.shoesense.SlotDetail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.shoesense.shoesense.EditThreshold.EditThresholdActivity
import com.shoesense.shoesense.R

class SlotDetailActivity : AppCompatActivity(), SlotDetailView {

    private lateinit var presenter: SlotDetailPresenter

    private lateinit var btnBack: ImageButton
    private lateinit var tvTitle: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvTimelineChip: TextView
    private lateinit var btnRename: MaterialButton
    private lateinit var btnDelete: MaterialButton
    private lateinit var btnEditThreshold: MaterialButton
    private lateinit var swNotif: SwitchMaterial

    private var suppressNotifCallback = false

    private val editThresholdLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val newG = result.data!!.getIntExtra(EditThresholdActivity.RESULT_THRESHOLD_G, presenter.getThresholdGrams())
            presenter.applyNewThreshold(newG)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_slot_detail)

        bindViews()
        wireClicks()

        presenter = SlotDetailPresenter(this)

        val initialName = intent.getStringExtra("slot_name")
        val initialStatus = intent.getStringExtra("slot_status")
        val initialNotif = intent.getBooleanExtra("slot_notif", false)
        presenter.onInit(initialName, initialStatus, initialNotif)
    }

    private fun bindViews() {
        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.tvTitle)
        tvStatus = findViewById(R.id.tvStatus)
        tvTimelineChip = findViewById(R.id.tvTimelineChip)
        btnRename = findViewById(R.id.btnRename)
        btnDelete = findViewById(R.id.btnDelete)
        btnEditThreshold = findViewById(R.id.btnEditThreshold)
        swNotif = findViewById(R.id.swNotif)
    }

    private fun wireClicks() {
        btnBack.setOnClickListener { presenter.onBackClicked() }
        btnRename.setOnClickListener { presenter.onRenameClicked() }
        btnDelete.setOnClickListener { presenter.onDeleteClicked() }
        btnEditThreshold.setOnClickListener {
            // Navigate to EditThresholdActivity, passing current values
            val intent = Intent(this, EditThresholdActivity::class.java).apply {
                putExtra(EditThresholdActivity.EXTRA_SLOT_NAME, presenter.getSlotName())
                putExtra(EditThresholdActivity.EXTRA_THRESHOLD_G, presenter.getThresholdGrams())
            }
            editThresholdLauncher.launch(intent)
        }
        swNotif.setOnCheckedChangeListener { _, isChecked ->
            if (suppressNotifCallback) return@setOnCheckedChangeListener
            presenter.onNotificationsToggled(isChecked)
        }
    }

    // ===== SlotDetailView =====

    override fun showSlotName(name: String) { tvTitle.text = name }

    override fun showStatus(status: String) { tvStatus.text = status }

    override fun showTimeline(occupiedAt: String?, emptyAt: String?) {
        val occ = occupiedAt?.let { "Occupied at $it" }
        val emp = emptyAt?.let { "Empty at $it" }
        tvTimelineChip.text = listOfNotNull(occ, emp).joinToString("\n").ifBlank { "No timeline yet" }
    }

    override fun setNotificationsEnabled(enabled: Boolean) {
        if (swNotif.isChecked != enabled) {
            suppressNotifCallback = true
            swNotif.isChecked = enabled
            suppressNotifCallback = false
        }
    }

    override fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun navigateBack() { finish() }

    // If you kept rename/delete dialogs from earlier, keep those methods here.
    override fun askForNewName(current: String, onResult: (String?) -> Unit) { /* ... */ }
    override fun confirmDelete(slotName: String, onResult: (Boolean) -> Unit) { /* ... */ }
    override fun askForNewThreshold(current: Int, onResult: (Int?) -> Unit) { /* not used now */ }
}
