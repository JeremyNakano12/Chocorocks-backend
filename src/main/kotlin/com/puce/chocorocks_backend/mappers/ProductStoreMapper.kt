package com.puce.chocorocks_backend.mappers

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*
import com.puce.chocorocks_backend.models.entities.*

object ProductStoreMapper {
    fun toEntity(request: ProductStoreRequest, product: Product, store: Store): ProductStore {
        return ProductStore(
            product = product,
            store = store,
            currentStock = request.currentStock,
            minStockLevel = request.minStockLevel
        )
    }

    fun toResponse(entity: ProductStore): ProductStoreResponse {
        return ProductStoreResponse(
            id = entity.id,
            product = ProductMapper.toResponse(entity.product),
            store = StoreMapper.toResponse(entity.store),
            currentStock = entity.currentStock,
            minStockLevel = entity.minStockLevel,
            lastUpdated = entity.lastUpdated,
            createdAt = entity.createdAt
        )
    }
}
