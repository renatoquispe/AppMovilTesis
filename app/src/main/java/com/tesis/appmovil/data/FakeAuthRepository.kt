package com.tesis.appmovil.data

//class FakeAuthRepository {
//
//    private var cachedUser: User? = null
//    private var chosenRole: UserRole? = null
//
//    // Simula usuarios en tu "base de datos"
//    private val validUsers = listOf(
//        User("1", "Susan", "susan@test.com", "123456"),
//        User("2", "Bob", "bob@test.com", "123456")
//    )
//
//    suspend fun login(email: String, password: String): Result<User> {
//        delay(800) // simula la llamada a la red
//        val user = validUsers.find { it.email == email && it.token == password }
//        return if (user != null) {
//            cachedUser = user
//            Result.success(user)
//        } else {
//            Result.failure(IllegalArgumentException("Email o contraseña inválidos"))
//        }
//    }
//    suspend fun register(name: String, email: String, password: String): Result<User> {
//        delay(800)
//        return if (name.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
//            val user = User(
//                id = "3",
//                name = name,
//                email = email,
//                token = "fake_token_register_456"
//            )
//            cachedUser = user
//            Result.success(user)
//        } else {
//            Result.failure(IllegalArgumentException("Completa todos los campos"))
//        }
//    }
//
//    fun setRole(role: UserRole) {
//        chosenRole = role
//    }
//
//    fun getRole(): UserRole? = chosenRole
//
//    fun getCurrentUser(): User? = cachedUser
//}

