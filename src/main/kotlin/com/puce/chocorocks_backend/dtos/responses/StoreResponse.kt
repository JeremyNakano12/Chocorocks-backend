package com.puce.chocorocks_backend.dtos.responses

import com.puce.chocorocks_backend.models.entities.*
import java.time.LocalDateTime
import java.time.LocalTime

data class StoreResponse(
    val id: Long,
    val name: String,
    val address: String,
    val manager: UserResponse?,
    val typeStore: StoreType,
    val phoneNumber: String?,
    val scheduleOpen: LocalTime?,
    val scheduleClosed: LocalTime?,
    val isActive: Boolean,
    val createdAt: LocalDateTime
)