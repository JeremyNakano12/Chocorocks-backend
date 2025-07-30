package com.puce.chocorocks_backend.dtos.responses

data class AuthUserResponse(
    val id: String,
    val email: String,
    val name: String?,
    val role: String,
    val isAuthenticated: Boolean
)
