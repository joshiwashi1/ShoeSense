package com.shoesense.shoesense.UpdateProfile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.shoesense.shoesense.R

class UpdateProfileActivity : AppCompatActivity(), UpdateProfileView {

    private lateinit var presenter: UpdateProfilePresenter

    private lateinit var btnBack: ImageButton
    private lateinit var btnSave: Button
    private lateinit var imgProfile: ImageView
    private lateinit var btnChangeImage: ImageView
    private lateinit var etUsername: EditText
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var progressDialog: ProgressBar

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
        setContentView(R.layout.activity_update_profile)

        presenter = UpdateProfilePresenter(this)

        // Bind views
        btnBack = findViewById(R.id.btnBack)
        btnSave = findViewById(R.id.btnSave)
        imgProfile = findViewById(R.id.imgProfile)
        btnChangeImage = findViewById(R.id.btnChangeImage)
        etUsername = findViewById(R.id.username_edit_text)
        etName = findViewById(R.id.name_edit_text)
        etEmail = findViewById(R.id.email_edit_text)
        progressDialog = ProgressBar(this)

        // Load user data from Firestore
        presenter.loadUserProfile()

        btnBack.setOnClickListener { finish() }

        btnChangeImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePicker.launch(intent)
        }

        btnSave.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()

            if (username.isEmpty() || name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            } else {
                presenter.updateUserProfile(username, name, email, selectedImageUri)
            }
        }
    }

    override fun showProfile(username: String, name: String, email: String, photoUrl: String?) {
        etUsername.setText(username)
        etName.setText(name)
        etEmail.setText(email)

        if (!photoUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(photoUrl)
                .placeholder(R.drawable.profile_icon)
                .error(R.drawable.profile_icon)
                .circleCrop()
                .into(imgProfile)
        }
    }

    override fun showLoading(isLoading: Boolean) {
        btnSave.isEnabled = !isLoading
        btnChangeImage.isEnabled = !isLoading
    }

    override fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
