package com.puce.chocorocks_backend.dtos.responses

import com.puce.chocorocks_backend.models.entities.UserRole

data class SupabaseUserResponse(
    val id: String,
    val email: String,
    val role: UserRole,
    val name: String?
)