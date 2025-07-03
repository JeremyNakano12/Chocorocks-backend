package com.puce.chocorocks_backend.dtos.responses

import java.time.LocalDateTime

data class UserActivityResponse(
    val id: Long,
    val user: UserResponse,
    val actionType: String,
    val tableAffected: String?,
    val recordId: Long?,
    val description: String,
    val ipAddress: String?,
    val userAgent: String?
)