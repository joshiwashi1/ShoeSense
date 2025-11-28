package com.shoesense.shoesense.SlotDetail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
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

    private var slotId: String = ""
    private var siteId: String = ""
    private var suppressNotifCallback = false

    private val editThresholdLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val newG = result.data!!.getIntExtra(
                EditThresholdActivity.RESULT_THRESHOLD_G,
                presenter.getThresholdGrams()
            )
            presenter.applyNewThreshold(newG)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide the AppCompat ActionBar
        supportActionBar?.hide()
        // Hide STATUS bar
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_slot_detail)
        bindViews()

        // 🔥 Get required IDs from Home
        slotId = intent.getStringExtra("slot_id") ?: ""
        siteId = intent.getStringExtra("site_id") ?: ""

        if (slotId.isBlank() || siteId.isBlank()) {
            showToast("Missing slot/site info.")
            finish()
            return
        }

        presenter = SlotDetailPresenter(this)
        presenter.attach(siteId, slotId)

        wireClicks()
    }

    override fun onDestroy() {
        presenter.detach()
        super.onDestroy()
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
            val intent = Intent(this, EditThresholdActivity::class.java).apply {
                putExtra(EditThresholdActivity.EXTRA_SLOT_NAME, presenter.getSlotName())
                putExtra(EditThresholdActivity.EXTRA_THRESHOLD_G, presenter.getThresholdGrams())
                putExtra(EditThresholdActivity.EXTRA_SLOT_ID, slotId)
            }
            editThresholdLauncher.launch(intent)
        }

        swNotif.setOnCheckedChangeListener { _, isChecked ->
            if (suppressNotifCallback) return@setOnCheckedChangeListener
            presenter.onNotificationsToggled(isChecked)
        }
    }

    // ===== SlotDetailView Implementation =====

    override fun showSlotName(name: String) {
        tvTitle.text = name
    }

    override fun showStatus(status: String) {
        tvStatus.text = status
        // optional:
        // tvStatus.setBackgroundResource(if (status == "Occupied") R.drawable.bg_green else R.drawable.bg_red)
    }

    override fun showTimeline(occupiedAt: String?, emptyAt: String?) {
        val occ = occupiedAt?.let { "Occupied at $it" }
        val emp = emptyAt?.let { "Empty at $it" }

        tvTimelineChip.text = listOfNotNull(occ, emp)
            .joinToString("\n")
            .ifBlank { "No timeline yet" }
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

    override fun navigateBack() {
        finish()
    }

    override fun askForNewName(current: String, onResult: (String?) -> Unit) {
        val input = EditText(this).apply { setText(current) }
        AlertDialog.Builder(this)
            .setTitle("Rename Slot")
            .setView(input)
            .setPositiveButton("Save") { _, _ -> onResult(input.text.toString()) }
            .setNegativeButton("Cancel") { _, _ -> onResult(null) }
            .show()
    }

    override fun confirmDelete(slotName: String, onResult: (Boolean) -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Delete $slotName?")
            .setMessage("This will permanently remove the slot.")
            .setPositiveButton("Delete") { _, _ -> onResult(true) }
            .setNegativeButton("Cancel") { _, _ -> onResult(false) }
            .show()
    }

    override fun askForNewThreshold(current: Int, onResult: (Int?) -> Unit) {
        // Not used because EditThresholdActivity handles it.
        onResult(null)
    }

    override fun getActivityContext() = this
}
