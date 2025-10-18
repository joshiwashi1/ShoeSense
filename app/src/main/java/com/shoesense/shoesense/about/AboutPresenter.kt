package com.shoesense.shoesense.about

class AboutPresenter {

    private var view: AboutView? = null

    fun attach(view: AboutView) {
        this.view = view
    }

    fun detach() {
        view = null
    }

    /** Populate the About screen (kept here for easy localization/dynamic data). */
    fun loadAbout() {
        view?.apply {
            setTitleCentered("About")
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
}
