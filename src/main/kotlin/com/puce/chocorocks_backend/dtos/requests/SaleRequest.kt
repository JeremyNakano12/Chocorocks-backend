package com.puce.chocorocks_backend.dtos.requests

import com.puce.chocorocks_backend.models.entities.*
import java.math.BigDecimal

data class SaleRequest(
    val saleNumber: String,
    val userId: Long,
    val clientId: Long? = null,
    val storeId: Long,
    val saleType: SaleType,
    val discountPercentage: BigDecimal = BigDecimal.ZERO,
    val discountAmount: BigDecimal = BigDecimal.ZERO,
    val taxPercentage: BigDecimal = BigDecimal("12.00"),
    val taxAmount: BigDecimal = BigDecimal.ZERO,
    val paymentMethod: String? = null,
    val notes: String? = null,
    val isInvoiced: Boolean = false
)