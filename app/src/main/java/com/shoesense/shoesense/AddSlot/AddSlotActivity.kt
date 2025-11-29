package com.shoesense.shoesense.AddSlot

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.shoesense.shoesense.R

class AddSlotActivity : AppCompatActivity(), AddSlotView {

    companion object {
        const val EXTRA_SLOT_NUMBER = "slot_number"   // for edit mode (1-based)
        const val EXTRA_IS_EDIT = "is_edit"           // false = create, true = edit
        const val RESULT_SAVED = 1001
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
    private var isEdit: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hide the ActionBar (for AppCompatActivity)
        supportActionBar?.hide()
        // Hide the STATUS BAR (not nav bar)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_add_slot)

        // Read intent
        isEdit = intent.getBooleanExtra(EXTRA_IS_EDIT, false)
        slotNumber = intent.getIntExtra(EXTRA_SLOT_NUMBER, 1)

        // Bind
        tvSlotLabel = findViewById(R.id.tvSlotLabel)
        tvThresholdValue = findViewById(R.id.tvThresholdValue)
        slider = findViewById(R.id.sliderThreshold)
        btnAddSlotName = findViewById(R.id.btnAddSlotName)
        btnAddSlot = findViewById(R.id.btnAddSlot)
        btnCancel = findViewById(R.id.btnCancel)
        btnBack = findViewById(R.id.btnBack)

        tvSlotLabel.text = if (isEdit) "Slot $slotNumber" else "New Slot"

        presenter = AddSlotPresenter(this, this)
        presenter.load(slotNumber, isEdit)

        // Slider config: 0–1000 g, step 10 g
        slider.valueFrom = 0f
        slider.valueTo = 1000f
        slider.stepSize = 10f

        // Display slider value live in GRAMS
        slider.addOnChangeListener { _, value, _ ->
            val grams = value.toInt()
            tvThresholdValue.text = "$grams g"
            presenter.setThreshold(slotNumber, grams) // cache in grams
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
            if (isEdit) {
                presenter.updateExisting(slotNumber)
            } else {
                presenter.createNew()
            }
        }

        btnCancel.setOnClickListener { finish() }
        btnBack.setOnClickListener { finish() }
    }

    // --- AddSlotView impl ---

    override fun showName(name: String) {
        btnAddSlotName.text = if (name.isBlank()) "Add Slot Name" else name
    }

    override fun showThreshold(grams: Int) {
        // Ensure slider is configured
        if (slider.valueFrom == 0f && slider.valueTo == 0f) {
            slider.valueFrom = 0f
            slider.valueTo = 1000f
            slider.stepSize = 10f
        }

        val clamped = grams.coerceIn(0, 1000)
        slider.value = clamped.toFloat()
        tvThresholdValue.text = "$clamped g"
    }

    override fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun closeScreen() {
        finish()
    }

    override fun onSaved() {
        setResult(RESULT_SAVED)
        finish()
    }
}
