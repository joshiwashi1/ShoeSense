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
        // Hide the AppCompat ActionBar (if your theme still shows one)
        actionBar?.hide()
        // Hide the STATUS BAR (not the nav bar)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_edit_threshold)

        tvTitle = findViewById(R.id.tvTitle)
        tvThresholdValue = findViewById(R.id.tvThresholdValue)
        slider = findViewById(R.id.sliderThreshold)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        btnBack = findViewById(R.id.btnBack)

        val slotId = intent.getStringExtra(EXTRA_SLOT_ID) ?: ""
        val slotName = intent.getStringExtra(EXTRA_SLOT_NAME) ?: "Slot"
        val currentG = intent.getIntExtra(EXTRA_THRESHOLD_G, 500)

        if (slotId.isBlank()) {
            Toast.makeText(this, "Missing slot ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        presenter = EditThresholdPresenter(this, slotId)

        // --- Configure slider ---
        slider.valueFrom = presenter.minG()
        slider.valueTo = presenter.maxG()
        slider.stepSize = presenter.stepG()

        val min = presenter.minG().toInt()
        val max = presenter.maxG().toInt()
        val step = presenter.stepG().toInt()
        val clamped = currentG.coerceIn(min, max)
        val snapped = ((clamped + step / 2) / step) * step

        // ✅ Format the label (tooltip) as kg, e.g. “0.55”
        slider.setLabelFormatter { value ->
            String.format("%.2f", value / 1000f)
        }

        slider.post {
            try {
                slider.value = snapped.toFloat()
            } catch (_: Exception) {
                slider.value = 500f
            }
        }

        presenter.onInit(slotName, snapped)

        slider.addOnChangeListener { _, value, _ ->
            presenter.onSliderChanged(value)
        }

        btnSave.setOnClickListener { presenter.onSaveClicked() }
        btnCancel.setOnClickListener { presenter.onCancelClicked() }
        btnBack.setOnClickListener { presenter.onBackClicked() }
    }

    // ===== EditThresholdView =====
    override fun renderTitle(slotName: String) { tvTitle.text = slotName }

    override fun renderThresholdKgText(kgText: String) { tvThresholdValue.text = kgText }

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
