package com.shoesense.shoesense.UpdateProfile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.shoesense.shoesense.R

class UpdateProfileActivity : AppCompatActivity(), UpdateProfileView {

    private lateinit var presenter: UpdateProfilePresenter

    private lateinit var btnBack: ImageButton
    private lateinit var btnSave: Button
    private lateinit var imgProfile: ImageView
    private lateinit var btnChangeImage: ImageView
    private lateinit var etName: EditText

    private var selectedImageUri: Uri? = null

    // Image picker launcher
    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                imgProfile.setImageURI(selectedImageUri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide the AppCompat ActionBar
        supportActionBar?.hide()
        // Hide the STATUS BAR (not the nav bar)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_update_profile)

        presenter = UpdateProfilePresenter(this)

        // Bind views
        btnBack = findViewById(R.id.btnBack)
        btnSave = findViewById(R.id.btnSave)
        imgProfile = findViewById(R.id.imgProfile)
        btnChangeImage = findViewById(R.id.btnChangeImage)
        etName = findViewById(R.id.name_edit_text)

        // Load user data (name + photo) from Realtime DB / Auth
        presenter.loadUserProfile()

        btnBack.setOnClickListener { finish() }

        btnChangeImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            imagePicker.launch(intent)
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show()
            } else {
                presenter.updateUserProfile(name, selectedImageUri)
            }
        }
    }

    // View interface implementations
    override fun showProfile(name: String, photoUrl: String?) {
        etName.setText(name)

        if (!photoUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(photoUrl)
                .placeholder(R.drawable.profile_icon)
                .error(R.drawable.profile_icon)
                .circleCrop()
                .into(imgProfile)
        } else {
            imgProfile.setImageResource(R.drawable.profile_icon)
        }
    }

    override fun showLoading(isLoading: Boolean) {
        btnSave.isEnabled = !isLoading
        btnChangeImage.isEnabled = !isLoading
    }

    override fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onProfileUpdated() {
        // redirect to ProfileActivity
        val intent = Intent(this, com.shoesense.shoesense.profile.ProfileActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

}
