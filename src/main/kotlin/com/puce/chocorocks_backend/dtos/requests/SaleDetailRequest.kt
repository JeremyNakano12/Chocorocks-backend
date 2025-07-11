package com.puce.chocorocks_backend.dtos.requests

import java.math.BigDecimal

data class SaleDetailRequest(
    val saleId: Long,
    val productId: Long,
    val batchId: Long? = null,
    val quantity: Int
)