package com.puce.chocorocks_backend.dtos.requests

import java.math.BigDecimal
import java.time.LocalDate

data class ProductBatchRequest(
    val batchCode: String,
    val productId: Long,
    val productionDate: LocalDate,
    val expirationDate: LocalDate,
    val initialQuantity: Int,
    val currentQuantity: Int,
    val batchCost: BigDecimal,
    val storeId: Long? = null,
    val isActive: Boolean = true
)
