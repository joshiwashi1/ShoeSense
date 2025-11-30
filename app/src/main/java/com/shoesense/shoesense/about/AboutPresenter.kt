package com.shoesense.shoesense.about

import android.os.Handler
import android.os.Looper

class AboutPresenter {

    private var view: AboutView? = null
    private val handler = Handler(Looper.getMainLooper())

    fun attach(view: AboutView) {
        this.view = view
    }

    fun detach() {
        view = null
    }

    /** Populate the About screen (with simulated delay for loading). */
    fun loadAbout() {
        // Show loading overlay
        view?.let {
            it.setTitleCentered("About")
        }

        // Simulate network or IoT check (2-second delay)
        handler.postDelayed({

            val isNetworkAvailable = true   // <-- replace with real network check
            val isIotConnected = true      // <-- replace with real IoT status check

            if (!isNetworkAvailable) {
                // Show no network overlay
                (view as? AboutActivity)?.showNoNetworkOverlay()
            } else if (!isIotConnected) {
                // Show IoT not found overlay
                (view as? AboutActivity)?.showIotNotFoundOverlay()
            } else {
                // Normal flow: hide overlays and populate screen
                (view as? AboutActivity)?.hideAllStates()
                view?.apply {
                    showAppName("ShoeSense")
                    showSubtitle("Smart Shoe Monitoring System")
                    showDescription(
                        "ShoeSense is an IoT-based system that detects shoe placement using " +
                                "load-cell sensors on a smart shelf. An ESP32 sends real-time updates to " +
                                "Firebase so the app can show live slot status, trigger notifications, and " +
                                "provide simple usage analysis."
                    )
                    showMembersTitle("Developers")
                }
            }

        }, 2000) // simulate 2s loading
    }
}
