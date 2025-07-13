package com.puce.chocorocks_backend.mappers

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*
import com.puce.chocorocks_backend.models.entities.*

object UserMapper {
    fun toEntity(request: UserRequest): User {
        return User(
            name = request.name,
            email = request.email,
            role = request.role,
            typeIdentification = request.typeIdentification,
            identificationNumber = request.identificationNumber,
            phoneNumber = request.phoneNumber,
            isActive = request.isActive
        )
    }

    fun toResponse(entity: User): UserResponse {
        return UserResponse(
            id = entity.id,
            name = entity.name,
            email = entity.email,
            role = entity.role,
            typeIdentification = entity.typeIdentification,
            identificationNumber = entity.identificationNumber,
            phoneNumber = entity.phoneNumber,
            isActive = entity.isActive
        )
    }
}