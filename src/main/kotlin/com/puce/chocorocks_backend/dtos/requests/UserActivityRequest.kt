package com.puce.chocorocks_backend.dtos.requests

data class UserActivityRequest(
    val userId: Long,
    val actionType: String,
    val tableAffected: String? = null,
    val recordId: Long? = null,
    val description: String,
    val ipAddress: String? = null,
    val userAgent: String? = null
)