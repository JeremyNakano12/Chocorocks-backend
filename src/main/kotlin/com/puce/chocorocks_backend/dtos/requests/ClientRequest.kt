package com.puce.chocorocks_backend.dtos.requests

import com.puce.chocorocks_backend.models.entities.*

data class ClientRequest(
    val nameLastname: String,
    val typeIdentification: IdentificationType,
    val identificationNumber: String,
    val phoneNumber: String? = null,
    val email: String? = null,
    val address: String? = null,
    val requiresInvoice: Boolean = false,
    val isActive: Boolean = true
)