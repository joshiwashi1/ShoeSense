package com.shoesense.shoesense.Repository

import android.app.Activity
import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import com.shoesense.shoesense.R

object BottomNavbar {

    enum class Item { HOME, ANALYTICS, NOTIFICATIONS, SETTINGS }

    data class Callbacks(
        val onHome: () -> Unit = {},
        val onAnalytics: () -> Unit = {},
        val onNotifications: () -> Unit = {},
        val onSettings: () -> Unit = {}
    )

    /**
     * Call this from your Activity after setContentView(...)
     */
    fun attach(
        activity: Activity,
        defaultSelected: Item,
        callbacks: Callbacks,
        @ColorInt selectedTint: Int? = null,
        @ColorInt unselectedTint: Int? = null,
        unselectedAlpha: Float = 0.45f
    ) {
        val navHome = activity.findViewById<LinearLayout>(R.id.navHome)
        val navAnalytics = activity.findViewById<LinearLayout>(R.id.navAnalytics)
        val navNotifications = activity.findViewById<LinearLayout>(R.id.navNotifications)
        val navSettings = activity.findViewById<LinearLayout>(R.id.navSettings)

        val items = listOf(
            Pair(Item.HOME, navHome),
            Pair(Item.ANALYTICS, navAnalytics),
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
                val icon = iconOf(container)
                val isSelected = (item == selected)

                // Visuals: tint if provided, else alpha fallback
                if (selectedTint != null && unselectedTint != null) {
                    icon?.imageTintList = ColorStateList.valueOf(if (isSelected) selectedTint else unselectedTint)
                    icon?.alpha = 1f
                } else {
                    icon?.alpha = if (isSelected) 1f else unselectedAlpha
                }

                // Optional: contentDescription update for accessibility
                icon?.contentDescription = "${item.name.lowercase().replaceFirstChar { it.titlecase() }}${if (isSelected) " (selected)" else ""}"
            }
        }

        fun onClick(item: Item) {
            applySelectedState(item)
            when (item) {
                Item.HOME -> callbacks.onHome()
                Item.ANALYTICS -> callbacks.onAnalytics()
                Item.NOTIFICATIONS -> callbacks.onNotifications()
                Item.SETTINGS -> callbacks.onSettings()
            }
        }

        // Hook listeners
        navHome.setOnClickListener { onClick(Item.HOME) }
        navAnalytics.setOnClickListener { onClick(Item.ANALYTICS) }
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
