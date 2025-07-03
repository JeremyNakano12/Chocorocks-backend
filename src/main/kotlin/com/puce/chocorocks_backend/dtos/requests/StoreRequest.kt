package com.puce.chocorocks_backend.dtos.requests

import com.puce.chocorocks_backend.models.entities.*
import java.time.LocalTime

data class StoreRequest(
    val name: String,
    val address: String,
    val managerId: Long? = null,
    val typeStore: StoreType,
    val phoneNumber: String? = null,
    val scheduleOpen: LocalTime? = null,
    val scheduleClosed: LocalTime? = null,
    val isActive: Boolean = true
)