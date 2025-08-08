package com.puce.chocorocks_backend.dtos.responses

import com.puce.chocorocks_backend.models.entities.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class ProductBatchResponse(
    val id: Long,
    val batchCode: String,
    val product: ProductResponse,
    val productionDate: LocalDate,
    val expirationDate: LocalDate,
    val initialQuantity: Int,
    val currentQuantity: Int,
    val batchCost: BigDecimal,
    val store: StoreResponse?,
    val isActive: Boolean,
    val createdAt: LocalDateTime
)