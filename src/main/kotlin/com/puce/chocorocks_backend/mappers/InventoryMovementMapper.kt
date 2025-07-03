package com.puce.chocorocks_backend.mappers

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*
import com.puce.chocorocks_backend.models.entities.*

object InventoryMovementMapper {
    fun toEntity(
        request: InventoryMovementRequest,
        product: Product,
        batch: ProductBatch? = null,
        fromStore: Store? = null,
        toStore: Store? = null,
        user: User
    ): InventoryMovement {
        return InventoryMovement(
            movementType = request.movementType,
            product = product,
            batch = batch,
            fromStore = fromStore,
            toStore = toStore,
            quantity = request.quantity,
            reason = request.reason,
            referenceId = request.referenceId,
            referenceType = request.referenceType,
            user = user,
            notes = request.notes
        )
    }

    fun toResponse(entity: InventoryMovement): InventoryMovementResponse {
        return InventoryMovementResponse(
            id = entity.id,
            movementType = entity.movementType,
            product = ProductMapper.toResponse(entity.product),
            batch = entity.batch?.let { ProductBatchMapper.toResponse(it) },
            fromStore = entity.fromStore?.let { StoreMapper.toResponse(it) },
            toStore = entity.toStore?.let { StoreMapper.toResponse(it) },
            quantity = entity.quantity,
            reason = entity.reason,
            referenceId = entity.referenceId,
            referenceType = entity.referenceType,
            user = UserMapper.toResponse(entity.user),
            notes = entity.notes,
            movementDate = entity.movementDate,
        )
    }
}