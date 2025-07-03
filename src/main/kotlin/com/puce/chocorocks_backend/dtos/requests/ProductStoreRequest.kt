package com.puce.chocorocks_backend.dtos.requests

data class ProductStoreRequest(
    val productId: Long,
    val storeId: Long,
    val currentStock: Int = 0,
    val minStockLevel: Int = 0
)