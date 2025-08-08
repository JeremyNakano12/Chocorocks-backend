package com.puce.chocorocks_backend.dtos.responses

import com.puce.chocorocks_backend.models.entities.*
import java.math.BigDecimal
import java.time.LocalDateTime

data class SaleResponse(
    val id: Long,
    val saleNumber: String,
    val user: UserResponse,
    val client: ClientResponse?,
    val store: StoreResponse,
    val saleType: SaleType,
    val subtotal: BigDecimal,
    val discountPercentage: BigDecimal,
    val discountAmount: BigDecimal,
    val taxPercentage: BigDecimal,
    val taxAmount: BigDecimal,
    val totalAmount: BigDecimal,
    val paymentMethod: String?,
    val notes: String?,
    val isInvoiced: Boolean,
    val createdAt: LocalDateTime
)
