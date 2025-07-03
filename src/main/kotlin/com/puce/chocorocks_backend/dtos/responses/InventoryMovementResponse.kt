package com.puce.chocorocks_backend.dtos.responses

import com.puce.chocorocks_backend.models.entities.*
import java.time.LocalDateTime


data class InventoryMovementResponse(
    val id: Long,
    val movementType: MovementType,
    val product: ProductResponse,
    val batch: ProductBatchResponse?,
    val fromStore: StoreResponse?,
    val toStore: StoreResponse?,
    val quantity: Int,
    val reason: MovementReason,
    val referenceId: Long?,
    val referenceType: String?,
    val user: UserResponse,
    val notes: String?,
    val movementDate: LocalDateTime
)