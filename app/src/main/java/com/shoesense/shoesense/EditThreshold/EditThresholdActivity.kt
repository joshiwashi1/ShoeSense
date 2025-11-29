package com.shoesense.shoesense.EditThreshold

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.shoesense.shoesense.R

class EditThresholdActivity : AppCompatActivity(), EditThresholdView {

    companion object {
        const val EXTRA_SLOT_NAME = "extra_slot_name"
        const val EXTRA_SLOT_ID = "extra_slot_id"
        const val EXTRA_THRESHOLD_G = "extra_threshold_g"
        const val RESULT_THRESHOLD_G = "result_threshold_g"
    }

    private lateinit var presenter: EditThresholdPresenter

    private lateinit var tvTitle: TextView
    private lateinit var tvThresholdValue: TextView
    private lateinit var slider: Slider
    private lateinit var btnSave: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_edit_threshold)

        bindViews()

        val slotId = intent.getStringExtra(EXTRA_SLOT_ID) ?: ""
        val slotName = intent.getStringExtra(EXTRA_SLOT_NAME) ?: "Slot"
        val currentG = intent.getIntExtra(EXTRA_THRESHOLD_G, 500)

        if (slotId.isBlank()) {
            Toast.makeText(this, "Missing slot ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        presenter = EditThresholdPresenter(this, slotId)

        // --- Configure slider for GRAMS ---
        slider.valueFrom = 0f
        slider.valueTo = 1000f
        slider.stepSize = 10f  // integer increments of 10g

        val clamped = currentG.coerceIn(0, 1000)
        val snapped = ((clamped + 5) / 10) * 10 // snap to nearest 10g

        // 🔥 Label now shows grams only (integer)
        slider.setLabelFormatter { value ->
            "${value.toInt()} g"
        }

        slider.post {
            slider.value = snapped.toFloat()
        }

        presenter.onInit(slotName, snapped)

        // Apply updated value live
        slider.addOnChangeListener { _, value, _ ->
            presenter.onSliderChanged(value.toInt().toFloat())  // enforce integer
        }

        btnSave.setOnClickListener { presenter.onSaveClicked() }
        btnCancel.setOnClickListener { presenter.onCancelClicked() }
        btnBack.setOnClickListener { presenter.onBackClicked() }
    }

    private fun bindViews() {
        tvTitle = findViewById(R.id.tvTitle)
        tvThresholdValue = findViewById(R.id.tvThresholdValue)
        slider = findViewById(R.id.sliderThreshold)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        btnBack = findViewById(R.id.btnBack)
    }

    // ===== EditThresholdView =====

    override fun renderTitle(slotName: String) {
        tvTitle.text = slotName
    }

    override fun renderThresholdKgText(kgText: String) {
        tvThresholdValue.text = kgText   // presenter sends "xxx g"
    }

    override fun closeWithResult(thresholdGrams: Int) {
        val data = Intent().apply { putExtra(RESULT_THRESHOLD_G, thresholdGrams) }
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    override fun closeWithoutResult() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
