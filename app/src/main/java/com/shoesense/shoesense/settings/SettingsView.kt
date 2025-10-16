package com.shoesense.shoesense.settings

interface SettingsView {
    fun setNotificationEnabled(enabled: Boolean)

    fun navigateToMyAccount()
    fun navigateToChangePassword()
    fun navigateToManageShelf()
    fun navigateToHelp()
    fun signOut()

    fun showMessage(msg: String)
}
