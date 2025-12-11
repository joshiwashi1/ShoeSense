package com.shoesense.shoesense.Repository

import android.app.Activity
import android.content.res.ColorStateList
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import com.shoesense.shoesense.R
import com.shoesense.shoesense.Utils.LoadingScreenHelper   // 👈 import the helper

object BottomNavbar {

    // 🔁 Renamed ANALYTICS -> HISTORY
    enum class Item { HOME, HISTORY, NOTIFICATIONS, SETTINGS }

    // 🔁 Added onHistory, removed onAnalytics
    data class Callbacks(
        val onHome: () -> Unit = {},
        val onHistory: () -> Unit = {},
        val onNotifications: () -> Unit = {},
        val onSettings: () -> Unit = {}
    )

    /**
     * Call this from your Activity after:
     *   setContentView(...)
     *   LoadingScreenHelper.init(this)
     */
    fun attach(
        activity: Activity,
        defaultSelected: Item,
        callbacks: Callbacks,
        @ColorInt selectedTint: Int? = null,
        @ColorInt unselectedTint: Int? = null,
        unselectedAlpha: Float = 0.45f,
        useLoadingOverlay: Boolean = true   // 👈 control loading from here
    ) {
        val navHome = activity.findViewById<LinearLayout>(R.id.navHome)

        // ✅ Prefer new id; fallback to old one if layout not updated yet
        val navHistory = activity.findViewById<LinearLayout?>(R.id.navHistory)
            ?: activity.findViewById(R.id.navHistory)

        val navNotifications = activity.findViewById<LinearLayout>(R.id.navNotifications)
        val navSettings = activity.findViewById<LinearLayout>(R.id.navSettings)

        val items = listOf(
            Pair(Item.HOME, navHome),
            Pair(Item.HISTORY, navHistory),
            Pair(Item.NOTIFICATIONS, navNotifications),
            Pair(Item.SETTINGS, navSettings)
        )

        fun iconOf(container: LinearLayout): ImageView? {
            // Finds the first ImageView inside the container
            for (i in 0 until container.childCount) {
                val v = container.getChildAt(i)
                if (v is ImageView) return v
                if (v is ViewGroup) {
                    val found = findImageViewInGroup(v)
                    if (found != null) return found
                }
            }
            return null
        }

        fun applySelectedState(selected: Item) {
            items.forEach { (item, container) ->
                val icon = iconOf(container) ?: return@forEach
                val isSelected = (item == selected)

                // 1) trigger selector: filled when selected, outline otherwise
                icon.isSelected = isSelected

                // 2) keep tint (often white); fade others via alpha
                if (selectedTint != null && unselectedTint != null) {
                    icon.imageTintList = ColorStateList.valueOf(
                        if (isSelected) selectedTint else unselectedTint
                    )
                }
                icon.alpha = if (isSelected) 1f else unselectedAlpha

                // accessibility
                val label = item.name.lowercase().replaceFirstChar { it.titlecase() }
                icon.contentDescription = "$label${if (isSelected) " (selected)" else ""}"
            }
        }

        fun loadingMessageFor(item: Item): String =
            when (item) {
                Item.HOME -> "Loading your home shelf…"
                Item.HISTORY -> "Loading shoe history…"
                Item.NOTIFICATIONS -> "Fetching notifications…"
                Item.SETTINGS -> "Opening settings…"
            }

        fun onClick(item: Item) {
            // 👟 Optional: show cute loader when user taps a tab
            if (useLoadingOverlay) {
                LoadingScreenHelper.showLoading(loadingMessageFor(item))
            }

            applySelectedState(item)

            when (item) {
                Item.HOME -> callbacks.onHome()
                Item.HISTORY -> callbacks.onHistory()
                Item.NOTIFICATIONS -> callbacks.onNotifications()
                Item.SETTINGS -> callbacks.onSettings()
            }
            // ⚠️ Do NOT hide here — let the screen that loads data call LoadingScreenHelper.hide()
            // when Firebase / API / DB is finished.
        }

        // Hook listeners
        navHome.setOnClickListener { onClick(Item.HOME) }
        navHistory.setOnClickListener { onClick(Item.HISTORY) }
        navNotifications.setOnClickListener { onClick(Item.NOTIFICATIONS) }
        navSettings.setOnClickListener { onClick(Item.SETTINGS) }

        // Initial state
        applySelectedState(defaultSelected)
    }

    private fun findImageViewInGroup(group: ViewGroup): ImageView? {
        for (i in 0 until group.childCount) {
            val v = group.getChildAt(i)
            if (v is ImageView) return v
            if (v is ViewGroup) {
                val nested = findImageViewInGroup(v)
                if (nested != null) return nested
            }
        }
        return null
    }
}
