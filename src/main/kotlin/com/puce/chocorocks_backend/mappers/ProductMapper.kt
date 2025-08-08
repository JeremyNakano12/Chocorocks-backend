package com.puce.chocorocks_backend.mappers

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*
import com.puce.chocorocks_backend.models.entities.*

object ProductMapper {
    fun toEntity(request: ProductRequest, category: Category): Product {
        return Product(
            code = request.code,
            nameProduct = request.nameProduct,
            description = request.description,
            category = category,
            flavor = request.flavor,
            size = request.size,
            productionCost = request.productionCost,
            wholesalePrice = request.wholesalePrice,
            retailPrice = request.retailPrice,
            minStockLevel = request.minStockLevel,
            imageUrl = request.imageUrl,
            barcode = request.barcode,
            isActive = request.isActive
        )
    }

    fun toResponse(entity: Product): ProductResponse {
        return ProductResponse(
            id = entity.id,
            code = entity.code,
            nameProduct = entity.nameProduct,
            description = entity.description,
            category = CategoryMapper.toResponse(entity.category),
            flavor = entity.flavor,
            size = entity.size,
            productionCost = entity.productionCost,
            wholesalePrice = entity.wholesalePrice,
            retailPrice = entity.retailPrice,
            minStockLevel = entity.minStockLevel,
            imageUrl = entity.imageUrl,
            barcode = entity.barcode,
            isActive = entity.isActive,
            createdAt = entity.createdAt,

        )
    }
}