package com.tesis.appmovil.data.remote


data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val status: Int? = null,
    val data: T? = null
)
