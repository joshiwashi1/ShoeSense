package com.shoesense.shoesense.AddSlot

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.shoesense.shoesense.R

class AddSlotActivity : AppCompatActivity(), AddSlotView {

    private lateinit var presenter: AddSlotPresenter

    private lateinit var tvSlotLabel: TextView
    private lateinit var tvThresholdValue: TextView
    private lateinit var slider: Slider
    private lateinit var btnAddSlotName: MaterialButton
    private lateinit var btnAddSlot: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnBack: ImageButton

    // Optional: pass in from previous screen; default 6
    private val slotNumber: Int by lazy { intent.getIntExtra("slotNumber", 6) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_slot)

        presenter = AddSlotPresenter(this, this)

        tvSlotLabel = findViewById(R.id.tvSlotLabel)
        tvThresholdValue = findViewById(R.id.tvThresholdValue)
        slider = findViewById(R.id.sliderThreshold)
        btnAddSlotName = findViewById(R.id.btnAddSlotName)
        btnAddSlot = findViewById(R.id.btnAddSlot)
        btnCancel = findViewById(R.id.btnCancel)
        btnBack = findViewById(R.id.btnBack)

        tvSlotLabel.text = "Slot $slotNumber"

        // Load saved state (if returning)
        presenter.load(slotNumber)

        // Slider events
        slider.addOnChangeListener { _, value, _ ->
            presenter.setThreshold(slotNumber, value)
            showThreshold(value)
        }

        // Name dialog
        btnAddSlotName.setOnClickListener {
            showNameDialog()
        }

        // Save
        btnAddSlot.setOnClickListener {
            presenter.save(slotNumber)
        }

        // Cancel/back
        btnCancel.setOnClickListener { finish() }
        btnBack.setOnClickListener { finish() }
    }

    private fun showNameDialog() {
        val til = TextInputLayout(this).apply {
            isHintEnabled = true
            hint = "Enter slot name"
            setPadding(32, 16, 32, 0)
        }
        val edit = TextInputEditText(til.context).apply {
            setText(presenter.getName(slotNumber) ?: "")
            setSelection(text?.length ?: 0)
        }
        til.addView(edit)

        MaterialAlertDialogBuilder(this)
            .setTitle("Slot Name")
            .setView(til)
            .setPositiveButton("Save") { d, _ ->
                val name = edit.text?.toString()?.trim().orEmpty()
                presenter.setName(slotNumber, name)
                showName(name)
                d.dismiss()
            }
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .show()
    }

    /* ==== AddSlotView ==== */
    override fun showName(name: String) {
        btnAddSlotName.text = if (name.isBlank()) "Add Slot Name" else name
    }

    override fun showThreshold(value: Float) {
        tvThresholdValue.text = String.format("%.1f kg", value)
        if (slider.value != value) slider.value = value
    }

    override fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun closeScreen() {
        setResult(RESULT_OK)
        finish()
    }
}
