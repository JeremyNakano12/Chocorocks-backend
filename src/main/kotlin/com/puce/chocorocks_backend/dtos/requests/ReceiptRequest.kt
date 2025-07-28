package com.puce.chocorocks_backend.dtos.requests

import com.puce.chocorocks_backend.models.entities.ReceiptStatus
import java.math.BigDecimal

data class ReceiptRequest(
    val receiptNumber: String,
    val userId: Long,
    val clientId: Long? = null,
    val saleId: Long,
    val storeId: Long,
    val receiptStatus: ReceiptStatus = ReceiptStatus.ACTIVE,
    val paymentMethod: String? = null,
    val additionalNotes: String? = null,
    val customerName: String? = null,
    val customerIdentification: String? = null
)