package com.puce.chocorocks_backend.dtos.responses

import com.puce.chocorocks_backend.models.entities.ReceiptStatus
import java.math.BigDecimal
import java.time.LocalDateTime

data class ReceiptResponse(
    val id: Long,
    val receiptNumber: String,
    val user: UserResponse,
    val client: ClientResponse?,
    val sale: SaleResponse,
    val store: StoreResponse,
    val issueDate: LocalDateTime,
    val receiptStatus: ReceiptStatus,
    val subtotal: BigDecimal,
    val discountAmount: BigDecimal,
    val taxPercentage: BigDecimal,
    val taxAmount: BigDecimal,
    val totalAmount: BigDecimal,
    val paymentMethod: String?,
    val additionalNotes: String?,
    val customerName: String?,
    val customerIdentification: String?,
    val isPrinted: Boolean,
    val printCount: Int
)