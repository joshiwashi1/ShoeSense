// app/src/main/java/com/shoesense/shoesense/EditThreshold/EditThresholdActivity.kt
package com.shoesense.shoesense.EditThreshold

import android.app.Activity
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
        const val EXTRA_THRESHOLD_G = "extra_threshold_g"   // incoming
        const val RESULT_THRESHOLD_G = "result_threshold_g" // outgoing
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
        setContentView(R.layout.activity_edit_threshold)

        tvTitle = findViewById(R.id.tvTitle)
        tvThresholdValue = findViewById(R.id.tvThresholdValue)
        slider = findViewById(R.id.sliderThreshold)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        btnBack = findViewById(R.id.btnBack)

        presenter = EditThresholdPresenter(this)

        // Configure slider FIRST (range + step)
        slider.valueFrom = presenter.minG()
        slider.valueTo = presenter.maxG()
        slider.stepSize = presenter.stepG()

        // Init presenter with incoming extras
        val slotName = intent.getStringExtra(EXTRA_SLOT_NAME)
        val currentG = intent.getIntExtra(EXTRA_THRESHOLD_G, 500)
        presenter.onInit(slotName, currentG)

        // After presenter sets current value, reflect it to the slider
        slider.value = presenter.currentG()

        // Listeners
        slider.addOnChangeListener { _, value, _ ->
            presenter.onSliderChanged(value)
        }
        btnSave.setOnClickListener { presenter.onSaveClicked() }
        btnCancel.setOnClickListener { presenter.onCancelClicked() }
        btnBack.setOnClickListener { presenter.onBackClicked() }
    }

    // ===== EditThresholdView =====
    override fun renderTitle(slotName: String) {
        tvTitle.text = slotName
    }

    override fun renderThresholdKgText(kgText: String) {
        tvThresholdValue.text = kgText
    }

    override fun closeWithResult(thresholdGrams: Int) {
        intent.putExtra(RESULT_THRESHOLD_G, thresholdGrams)
        setResult(Activity.RESULT_OK, intent)
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
