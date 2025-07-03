package com.puce.chocorocks_backend.mappers

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*
import com.puce.chocorocks_backend.models.entities.*

object CategoryMapper {
    fun toEntity(request: CategoryRequest): Category {
        return Category(
            name = request.name,
            description = request.description
        )
    }

    fun toResponse(entity: Category): CategoryResponse {
        return CategoryResponse(
            id = entity.id,
            name = entity.name,
            description = entity.description
        )
    }
}