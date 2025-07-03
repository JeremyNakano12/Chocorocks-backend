package com.puce.chocorocks_backend.mappers

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*
import com.puce.chocorocks_backend.models.entities.*

object ProductBatchMapper {
    fun toEntity(request: ProductBatchRequest, product: Product, store: Store? = null): ProductBatch {
        return ProductBatch(
            batchCode = request.batchCode,
            product = product,
            productionDate = request.productionDate,
            expirationDate = request.expirationDate,
            initialQuantity = request.initialQuantity,
            currentQuantity = request.currentQuantity,
            batchCost = request.batchCost,
            store = store,
            isActive = request.isActive
        )
    }

    fun toResponse(entity: ProductBatch): ProductBatchResponse {
        return ProductBatchResponse(
            id = entity.id,
            batchCode = entity.batchCode,
            product = ProductMapper.toResponse(entity.product),
            productionDate = entity.productionDate,
            expirationDate = entity.expirationDate,
            initialQuantity = entity.initialQuantity,
            currentQuantity = entity.currentQuantity,
            batchCost = entity.batchCost,
            store = entity.store?.let { StoreMapper.toResponse(it) },
            isActive = entity.isActive
        )
    }
}