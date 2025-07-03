package com.puce.chocorocks_backend.dtos.requests

import java.math.BigDecimal

data class ProductRequest(
    val code: String,
    val nameProduct: String,
    val description: String? = null,
    val categoryId: Long,
    val flavor: String? = null,
    val size: String? = null,
    val productionCost: BigDecimal = BigDecimal.ZERO,
    val wholesalePrice: BigDecimal = BigDecimal.ZERO,
    val retailPrice: BigDecimal = BigDecimal.ZERO,
    val minStockLevel: Int = 0,
    val imageUrl: String? = null,
    val barcode: String? = null,
    val isActive: Boolean = true
)
