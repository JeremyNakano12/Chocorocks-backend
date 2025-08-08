package com.puce.chocorocks_backend.dtos.responses

import com.puce.chocorocks_backend.models.entities.*
import java.time.LocalDateTime

data class ClientResponse(
    val id: Long,
    val nameLastname: String,
    val typeIdentification: IdentificationType,
    val identificationNumber: String,
    val phoneNumber: String?,
    val email: String?,
    val address: String?,
    val requiresInvoice: Boolean,
    val isActive: Boolean,
    val createdAt: LocalDateTime
)