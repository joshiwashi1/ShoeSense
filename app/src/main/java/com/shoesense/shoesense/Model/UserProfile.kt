package com.shoesense.shoesense.model

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val createdAt: Long = 0L
)
