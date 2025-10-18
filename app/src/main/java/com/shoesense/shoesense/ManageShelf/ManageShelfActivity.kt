package com.shoesense.shoesense.ManageShelf

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.shoesense.shoesense.Model.Slot
import com.shoesense.shoesense.R
import com.shoesense.shoesense.home.HomeDashboardActivity
import com.shoesense.shoesense.settings.SettingsActivity

class ManageShelfActivity : AppCompatActivity(), ManageShelfView {

    private lateinit var presenter: ManageShelfPresenter

    // Top bar
    private lateinit var btnBack: ImageButton
    private lateinit var pageTitle: TextView

    // Summary numbers
    private lateinit var txtTotalSlots: TextView
    private lateinit var txtOccupied: TextView
    private lateinit var txtEmpty: TextView

    // Settings controls
    private lateinit var inputMaxSlots: TextInputEditText
    private lateinit var btnSaveCapacity: MaterialButton
    private lateinit var switchNotifyDefault: SwitchMaterial

    // Bottom nav (your custom bar views)
    private lateinit var navHome: View
    private lateinit var navAnalytics: View
    private lateinit var navNotifications: View
    private lateinit var navSettings: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manage_shelf)

        // Safe insets
        findViewById<View>(R.id.main)?.let { root ->
            ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
                val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
                insets
            }
        }

        // --- Bind views to IDs in your latest XML ---
        btnBack = findViewById(R.id.btnBack)
        pageTitle = findViewById(R.id.pageTitle)

        txtTotalSlots = findViewById(R.id.txtTotalSlots)
        txtOccupied = findViewById(R.id.txtOccupied)
        txtEmpty = findViewById(R.id.txtEmpty)

        inputMaxSlots = findViewById(R.id.inputMaxSlots)
        btnSaveCapacity = findViewById(R.id.btnSaveCapacity)
        switchNotifyDefault = findViewById(R.id.switchNotifyDefault)

        navHome = findViewById(R.id.navHome)
        navAnalytics = findViewById(R.id.navAnalytics)
        navNotifications = findViewById(R.id.navNotifications)
        navSettings = findViewById(R.id.navSettings)

        // --- List views REMOVED in layout, so nothing to bind for them ---

        // --- Events ---
        btnBack.setOnClickListener { finish() }

        btnSaveCapacity.setOnClickListener {
            presenter.onSaveCapacity(inputMaxSlots.text?.toString().orEmpty())
        }

        // Simple local toggle (store in your own prefs if desired)
        switchNotifyDefault.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, if (isChecked) "Notifications enabled" else "Notifications disabled", Toast.LENGTH_SHORT).show()
        }

        // Bottom nav taps (adjust these routes to your actual activities)
        navHome.setOnClickListener {
            startActivity(Intent(this, HomeDashboardActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
        navAnalytics.setOnClickListener {
            Toast.makeText(this, "Analytics (coming soon)", Toast.LENGTH_SHORT).show()
        }
        navNotifications.setOnClickListener {
            Toast.makeText(this, "Notifications (coming soon)", Toast.LENGTH_SHORT).show()
        }
        navSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        // --- Presenter ---
        presenter = ManageShelfPresenter()
        presenter.attach(this, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detach()
    }

    // ---- ManageShelfView implementation ----

    // No SwipeRefresh anymore — just a no-op (or show a progress dialog if you prefer)
    override fun showLoading(isLoading: Boolean) { /* no-op */ }

    override fun showCapacity(cap: Int) {
        inputMaxSlots.setText(cap.toString())
    }

    // No list in this layout; we just ignore slot list updates for now
    override fun showSlots(slots: List<Slot>) { /* no-op */ }

    // Your new design shows just the numbers (no labels inside these TextViews)
    override fun updateStats(total: Int, occupied: Int, empty: Int) {
        txtTotalSlots.text = total.toString()
        txtOccupied.text = occupied.toString()
        txtEmpty.text = empty.toString()
    }

    override fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    // No list to click; leave this for future use if you add a slot grid/list again
    override fun openRenameDialog(slot: Slot) {
        val input = EditText(this).apply {
            setText(slot.name)
            setSelection(text.length)
            filters = arrayOf(InputFilter.LengthFilter(30))
        }
        AlertDialog.Builder(this)
            .setTitle("Rename ${slot.id.ifBlank { slot.name }}")
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                presenter.onRenameConfirmed(slot.id.ifBlank { slot.name }, input.text.toString())
            }.show()
    }
}
