package com.shoesense.shoesense.Repository

import com.google.firebase.auth.FirebaseAuth

/**
 * Shared configuration and runtime variables used across the app.
 */
object AppConfig {

    /**
     * Holds the current active site ID.
     * This can be assigned dynamically after registration or login.
     */
    var siteId: String? = null

    /**
     * Default role for new members joining a site.
     */
    const val DEFAULT_ROLE = "member"

    /**
     * Helper function: safely return a non-null siteId or throw an error.
     */
    fun requireSiteId(): String {
        return siteId ?: throw IllegalStateException("siteId not set. Make sure user joined a site first.")
    }

    /**
     * Get current user ID safely
     */
    fun requireUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User not logged in")
    }
}