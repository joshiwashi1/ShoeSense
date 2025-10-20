package com.shoesense.shoesense.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.shoesense.shoesense.R
import com.shoesense.shoesense.UpdateProfile.UpdateProfileActivity

class ProfileActivity : AppCompatActivity(), ProfileView.View {

    private lateinit var presenter: ProfileView.Presenter

    // Header (top) fields
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var imgProfile: ImageView

    // Account Info placeholders (middle section)
    private lateinit var profileName: TextView
    private lateinit var profileEmail: TextView
    private lateinit var sectionAccountInfo: View

    // Buttons
    private lateinit var btnBack: ImageView
    private lateinit var btnAccountInfo: Button
    private lateinit var btnUpdateProfile: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // --- Bind views ---
        tvName = findViewById(R.id.tvName)
        tvEmail = findViewById(R.id.tvEmail)
        imgProfile = findViewById(R.id.imgProfile)

        profileName = findViewById(R.id.profileName)
        profileEmail = findViewById(R.id.profileEmail)
        sectionAccountInfo = findViewById(R.id.sectionAccountInfo)

        btnBack = findViewById(R.id.btnBack)
        btnAccountInfo = findViewById(R.id.btnAccountInfo)
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile)

        presenter = ProfilePresenter(this)

        // ✅ Ensure user is logged in
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            showErrorMessage("Not signed in.")
            finish()
            return
        }

        // --- Prefill from Firebase Auth for instant UX ---
        val displayName = user.displayName ?: ""
        val email = user.email ?: ""
        val photoUrl = user.photoUrl?.toString()

        if (displayName.isNotBlank()) {
            tvName.text = displayName
            profileName.text = displayName        // fill placeholder too
        }

        if (email.isNotBlank()) {
            tvEmail.text = email
            profileEmail.text = email            // fill placeholder too
        }

        if (!photoUrl.isNullOrBlank()) {
            Glide.with(this)
                .load(photoUrl)
                .placeholder(R.drawable.profile_icon)
                .error(R.drawable.profile_icon)
                .circleCrop()
                .into(imgProfile)
        } else {
            imgProfile.setImageResource(R.drawable.profile_icon)
        }

        // --- Fetch richer profile (e.g., name override or custom photo) via Presenter ---
        if (email.isNotBlank()) {
            presenter.fetchUserProfile(email)
        } else {
            showErrorMessage("No email found for current user.")
        }

        // --- Button actions ---
        btnBack.setOnClickListener { finish() }

        // Toggle the Account Info section for a simple expansion behavior
        btnAccountInfo.setOnClickListener {
            sectionAccountInfo.visibility =
                if (sectionAccountInfo.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        // Open your Edit Profile screen (if you have one), else simple toast for now
        btnUpdateProfile.setOnClickListener {
            // ✅ Navigate to UpdateProfileActivity instead of showing a toast
            val intent = Intent(this, UpdateProfileActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    // Presenter → View callbacks
    override fun showProfile(name: String, email: String, profileUrl: String?) {
        if (name.isNotBlank()) {
            tvName.text = name
            profileName.text = name
        }
        if (email.isNotBlank()) {
            tvEmail.text = email
            profileEmail.text = email
        }
        if (!profileUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(profileUrl)
                .placeholder(R.drawable.profile_icon)
                .error(R.drawable.profile_icon)
                .circleCrop()
                .into(imgProfile)
        }
    }

    override fun showErrorMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun showLoading(isLoading: Boolean) {
        // Hook to a ProgressBar if you add one later
        if (isLoading) {
            // no-op or small toast/log
        }
    }
}
