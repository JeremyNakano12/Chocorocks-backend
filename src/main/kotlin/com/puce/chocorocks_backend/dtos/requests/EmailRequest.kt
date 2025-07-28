package com.puce.chocorocks_backend.dtos.requests

data class EmailRequest(
    val recipientEmail: String? = null,
    val useClientEmail: Boolean = true,
    val additionalMessage: String? = null
)