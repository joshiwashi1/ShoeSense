package com.shoesense.shoesense.Model

data class User(val name: String, val email: String, val password: String)

object AuthRepository {
    private val users = mutableListOf<User>()

    fun registerUser(name: String, email: String, password: String): Boolean {
        if (users.any { it.email == email }) return false
        users.add(User(name, email, password))
        return true
    }

    fun validateLogin(email: String, password: String): Boolean {
        return users.any { it.email == email && it.password == password }
    }
}

