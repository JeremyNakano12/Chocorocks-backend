package com.puce.chocorocks_backend.mappers

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*
import com.puce.chocorocks_backend.models.entities.*

object StoreMapper {
    fun toEntity(request: StoreRequest, manager: User? = null): Store {
        return Store(
            name = request.name,
            address = request.address,
            manager = manager,
            typeStore = request.typeStore,
            phoneNumber = request.phoneNumber,
            scheduleOpen = request.scheduleOpen,
            scheduleClosed = request.scheduleClosed,
            isActive = request.isActive
        )
    }

    fun toResponse(entity: Store): StoreResponse {
        return StoreResponse(
            id = entity.id,
            name = entity.name,
            address = entity.address,
            manager = entity.manager?.let { UserMapper.toResponse(it) },
            typeStore = entity.typeStore,
            phoneNumber = entity.phoneNumber,
            scheduleOpen = entity.scheduleOpen,
            scheduleClosed = entity.scheduleClosed,
            isActive = entity.isActive
        )
    }
}