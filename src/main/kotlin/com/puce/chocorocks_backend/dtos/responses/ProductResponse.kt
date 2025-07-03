package com.puce.chocorocks_backend.dtos.responses

import java.math.BigDecimal
import java.time.LocalDateTime

data class ProductResponse(
    val id: Long,
    val code: String,
    val nameProduct: String,
    val description: String?,
    val category: CategoryResponse,
    val flavor: String?,
    val size: String?,
    val productionCost: BigDecimal,
    val wholesalePrice: BigDecimal,
    val retailPrice: BigDecimal,
    val minStockLevel: Int,
    val imageUrl: String?,
    val barcode: String?,
    val isActive: Boolean
)