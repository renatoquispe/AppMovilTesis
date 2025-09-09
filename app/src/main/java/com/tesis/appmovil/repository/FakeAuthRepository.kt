package com.tesis.appmovil.repository

import com.tesis.appmovil.models.User
import com.tesis.appmovil.models.UserRole
import kotlinx.coroutines.delay

object FakeAuthRepository {

    private var cachedUser: User? = null
    private var chosenRole: UserRole? = null

    // Base de datos falsa en memoria
    private val users = mutableListOf(
        User("1", "Susan", "susan@test.com", "123456"),
        User("2", "Bob", "bob@test.com", "123456")
    )

    suspend fun login(email: String, password: String): Result<User> {
        delay(800)
        val user = users.find { it.email == email && it.token == password }
        return if (user != null) {
            cachedUser = user
            Result.success(user)
        } else {
            Result.failure(IllegalArgumentException("Email o contraseña inválidos"))
        }
    }

    suspend fun register(name: String, email: String, password: String): Result<User> {
        delay(800)
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Completa todos los campos"))
        }
        // Verificar si ya existe
        if (users.any { it.email == email }) {
            return Result.failure(IllegalArgumentException("Ese email ya está registrado"))
        }

        val newUser = User(
            id = (users.size + 1).toString(),
            name = name,
            email = email,
            token = password // 👈 Aquí guardo la contraseña en token
        )
        users.add(newUser) // guardar en la lista
        cachedUser = newUser
        return Result.success(newUser)
    }

    fun setRole(role: UserRole) { chosenRole = role }
    fun getRole(): UserRole? = chosenRole
    fun getCurrentUser(): User? = cachedUser
}