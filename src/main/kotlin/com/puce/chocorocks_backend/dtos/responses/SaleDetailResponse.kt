package com.puce.chocorocks_backend.dtos.responses

import java.math.BigDecimal
import java.time.LocalDateTime

data class SaleDetailResponse(
    val id: Long,
    val sale: SaleResponse,
    val product: ProductResponse,
    val batch: ProductBatchResponse?,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val subtotal: BigDecimal
)
