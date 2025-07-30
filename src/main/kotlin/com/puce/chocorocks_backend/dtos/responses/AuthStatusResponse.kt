package com.puce.chocorocks_backend.dtos.responses

data class AuthStatusResponse(
    val isAuthenticated: Boolean,
    val user: AuthUserResponse?
)