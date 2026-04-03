package com.pikachu.financify.models

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: Long,
    val email: String,
    val name: String,
    val authProvider: String,
    val profilePictureUrl: String? = null,
    val createdAt: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class GoogleAuthRequest(
    val idToken: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserResponse,
    val message: String = "Success"
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String = ""
)

@Serializable
data class ErrorResponse(
    val success: Boolean = false,
    val message: String
)
