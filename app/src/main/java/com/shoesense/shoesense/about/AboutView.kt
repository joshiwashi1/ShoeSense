package com.shoesense.shoesense.about

interface AboutView {
    fun showAppName(name: String)
    fun showSubtitle(subtitle: String)
    fun showDescription(desc: String)
    fun showMembersTitle(title: String)
    fun setTitleCentered(title: String)
}
