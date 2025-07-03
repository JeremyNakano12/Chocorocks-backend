package com.puce.chocorocks_backend.dtos.requests

import com.puce.chocorocks_backend.models.entities.*

data class InventoryMovementRequest(
    val movementType: MovementType,
    val productId: Long,
    val batchId: Long? = null,
    val fromStoreId: Long? = null,
    val toStoreId: Long? = null,
    val quantity: Int,
    val reason: MovementReason,
    val referenceId: Long? = null,
    val referenceType: String? = null,
    val userId: Long,
    val notes: String? = null
)