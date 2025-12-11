package com.shoesense.shoesense.Utils

import android.app.Activity
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.shoesense.shoesense.R

object LoadingScreenHelper {

    private var loadingOverlay: FrameLayout? = null
    private var progressBar: ProgressBar? = null
    private var messageText: TextView? = null

    fun init(activity: Activity) {
        loadingOverlay = activity.findViewById(R.id.loading_overlay)
        progressBar = activity.findViewById(R.id.loading_progress)
        messageText = activity.findViewById(R.id.loading_message)
    }

    fun showLoading(message: String = "Loading...") {
        loadingOverlay?.visibility = View.VISIBLE
        progressBar?.visibility = View.VISIBLE
        messageText?.visibility = View.VISIBLE
        messageText?.text = message
    }

    fun showNoInternet() {
        loadingOverlay?.visibility = View.VISIBLE
        progressBar?.visibility = View.GONE
        messageText?.visibility = View.VISIBLE
        messageText?.text = "No Internet Connection"
    }

    fun hide() {
        loadingOverlay?.visibility = View.GONE
    }
}
