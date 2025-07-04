package com.puce.chocorocks_backend.dtos.responses

import com.puce.chocorocks_backend.models.entities.*

data class UserResponse(
    val id: Long,
    val name: String,
    val email: String,
    val role: UserRole,
    val typeIdentification: IdentificationType,
    val identificationNumber: String,
    val phoneNumber: String?,
    val isActive: Boolean
)