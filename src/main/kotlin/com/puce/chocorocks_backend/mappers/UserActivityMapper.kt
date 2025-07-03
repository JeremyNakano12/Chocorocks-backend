package com.puce.chocorocks_backend.mappers

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*
import com.puce.chocorocks_backend.models.entities.*

object UserActivityMapper {
    fun toEntity(request: UserActivityRequest, user: User): UserActivity {
        return UserActivity(
            user = user,
            actionType = request.actionType,
            tableAffected = request.tableAffected,
            recordId = request.recordId,
            description = request.description,
            ipAddress = request.ipAddress,
            userAgent = request.userAgent
        )
    }

    fun toResponse(entity: UserActivity): UserActivityResponse {
        return UserActivityResponse(
            id = entity.id,
            user = UserMapper.toResponse(entity.user),
            actionType = entity.actionType,
            tableAffected = entity.tableAffected,
            recordId = entity.recordId,
            description = entity.description,
            ipAddress = entity.ipAddress,
            userAgent = entity.userAgent
        )
    }
}