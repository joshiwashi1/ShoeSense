package com.shoesense.shoesense.AddSlot

import android.app.Activity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.shoesense.shoesense.R

class AddSlotActivity : AppCompatActivity(), AddSlotView {

    companion object {
        const val EXTRA_SLOT_NUMBER = "slot_number"   // 1-based index you pass in from Home
        const val RESULT_SAVED = 1001                 // optional result code to trigger refresh
    }

    private lateinit var presenter: AddSlotPresenter

    private lateinit var tvSlotLabel: TextView
    private lateinit var tvThresholdValue: TextView
    private lateinit var slider: Slider
    private lateinit var btnAddSlotName: MaterialButton
    private lateinit var btnAddSlot: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnBack: ImageButton

    private var slotNumber: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_slot) // your XML file name

        slotNumber = intent.getIntExtra(EXTRA_SLOT_NUMBER, 1)

        tvSlotLabel = findViewById(R.id.tvSlotLabel)
        tvThresholdValue = findViewById(R.id.tvThresholdValue)
        slider = findViewById(R.id.sliderThreshold)
        btnAddSlotName = findViewById(R.id.btnAddSlotName)
        btnAddSlot = findViewById(R.id.btnAddSlot)
        btnCancel = findViewById(R.id.btnCancel)
        btnBack = findViewById(R.id.btnBack)

        tvSlotLabel.text = "Slot $slotNumber"

        presenter = AddSlotPresenter(this, this)
        presenter.load(slotNumber)

        slider.addOnChangeListener { _, value, _ ->
            tvThresholdValue.text = String.format("%.1f kg", value)
            presenter.setThreshold(slotNumber, value)
        }

        btnAddSlotName.setOnClickListener {
            val input = EditText(this).apply { hint = "e.g., Dad’s Sneakers" }
            AlertDialog.Builder(this)
                .setTitle("Slot Name")
                .setView(input)
                .setPositiveButton("Save") { _, _ ->
                    val name = input.text?.toString()?.trim().orEmpty()
                    presenter.setName(slotNumber, name)
                    showName(name)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        btnAddSlot.setOnClickListener {
            presenter.save(slotNumber)
            setResult(RESULT_SAVED)
        }

        btnCancel.setOnClickListener { finish() }
        btnBack.setOnClickListener { finish() }
    }

    // --- AddSlotView ---
    override fun showName(name: String) {
        // reflect chosen name under the big button label if you want
        btnAddSlotName.text = if (name.isBlank()) "Add Slot Name" else name
    }

    override fun showThreshold(value: Float) {
        slider.value = value
        tvThresholdValue.text = String.format("%.1f kg", value)
    }

    override fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun closeScreen() {
        finish()
    }
    override fun onSaved() {
        setResult(AddSlotActivity.RESULT_SAVED)
    }

}
