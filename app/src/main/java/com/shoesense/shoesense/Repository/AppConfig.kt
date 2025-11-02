package com.shoesense.shoesense.Repository

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
     * Root node in Firebase Realtime Database.
     */
    const val FIREBASE_ROOT = "sites"

    /**
     * Helper function: safely return a non-null siteId or throw an error.
     */
    fun requireSiteId(): String {
        return siteId ?: throw IllegalStateException("siteId not set. Make sure user joined a site first.")
    }
}
