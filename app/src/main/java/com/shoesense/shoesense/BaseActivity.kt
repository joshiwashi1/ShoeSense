package com.shoesense.shoesense

import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.shoesense.shoesense.R.*

open class BaseActivity : AppCompatActivity() {

    private lateinit var loadingContainer: View
    private lateinit var stateLoading: View
    private lateinit var stateNoNetwork: View
    private lateinit var stateIotNotFound: View

    override fun setContentView(layoutResID: Int) {

        // Inflate the base layout
        val base = layoutInflater.inflate(layout.activity_base, null)
        val container = base.findViewById<FrameLayout>(id.container)

        // Inflate the Activity's own layout
        layoutInflater.inflate(layoutResID, container, true)
        super.setContentView(base)

        // Attach loading states overlay
        val states = layoutInflater.inflate(layout.loading_states, container, false)
        container.addView(states)

        // Link Views
        loadingContainer = states.findViewById(id.globalLoadingContainer)
        stateLoading = states.findViewById(id.stateLoadingContainer)
        stateNoNetwork = states.findViewById(id.stateNoNetworkContainer)
        stateIotNotFound = states.findViewById(id.stateIotNotFoundContainer)
    }

    fun showLoadingState() {
        showOnly(stateLoading)
    }

    fun showNoNetworkState() {
        showOnly(stateNoNetwork)
    }

    fun showIotNotFoundState() {
        showOnly(stateIotNotFound)
    }

    fun hideAllStates() {
        loadingContainer.visibility = View.GONE
    }

    private fun showOnly(viewToShow: View) {
        loadingContainer.visibility = View.VISIBLE

        stateLoading.visibility = View.GONE
        stateNoNetwork.visibility = View.GONE
        stateIotNotFound.visibility = View.GONE

        viewToShow.visibility = View.VISIBLE
    }
}
