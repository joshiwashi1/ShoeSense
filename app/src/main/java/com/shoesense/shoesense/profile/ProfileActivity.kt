package com.shoesense.shoesense.profile

import android.app.Activity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.shoesense.shoesense.R

class ProfileActivity : Activity(), ProfileView.View {

    private lateinit var presenter: ProfileView.Presenter
    private lateinit var pName: TextView
    private lateinit var pEmail: TextView
    private lateinit var imgProfile: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        pName = findViewById(R.id.tvName)
        pEmail = findViewById(R.id.tvEmail)
        imgProfile = findViewById(R.id.imgProfile)

        presenter = ProfilePresenter(this)

        // Retrieve saved email (from SharedPreferences)
        val email = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            .getString("email", null)

        if (email != null) {
            presenter.fetchUserProfile(email)
        } else {
            showErrorMessage("No email found.")
        }
    }

    override fun showProfile(name: String, email: String, profileUrl: String?) {
        pName.text = name
        pEmail.text = email

        if (!profileUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(profileUrl)
                .placeholder(R.drawable.profile_icon)
                .error(R.drawable.profile_icon)
                .circleCrop()
                .into(imgProfile)
        } else {
            imgProfile.setImageResource(R.drawable.profile_icon)
        }
    }

    override fun showErrorMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun showLoading(isLoading: Boolean) {
        // Optional: Add a progress bar later if needed
        if (isLoading) {
            Toast.makeText(this, "Loading profile...", Toast.LENGTH_SHORT).show()
        }
    }
}
