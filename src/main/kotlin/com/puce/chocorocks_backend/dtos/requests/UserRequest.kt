package com.puce.chocorocks_backend.dtos.requests

import com.puce.chocorocks_backend.models.entities.*

data class UserRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: UserRole,
    val typeIdentification: IdentificationType,
    val identificationNumber: String,
    val phoneNumber: String? = null,
    val isActive: Boolean = true
)