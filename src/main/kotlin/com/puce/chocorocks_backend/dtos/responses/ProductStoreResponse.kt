package com.puce.chocorocks_backend.dtos.responses

import com.puce.chocorocks_backend.models.entities.*
import java.time.LocalDateTime


data class ProductStoreResponse(
    val id: Long,
    val product: ProductResponse,
    val store: StoreResponse,
    val currentStock: Int,
    val minStockLevel: Int,
    val lastUpdated: LocalDateTime,
    val createdAt: LocalDateTime
)
