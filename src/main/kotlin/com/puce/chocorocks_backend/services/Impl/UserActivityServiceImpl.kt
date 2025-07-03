package com.puce.chocorocks_backend.services.Impl

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*
import com.puce.chocorocks_backend.mappers.*
import com.puce.chocorocks_backend.repositories.*
import com.puce.chocorocks_backend.services.*
import com.puce.chocorocks_backend.models.entities.*
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserActivityServiceImpl(
    private val userActivityRepository: UserActivityRepository,
    private val userRepository: UserRepository
) : UserActivityService {

    override fun findAll(): List<UserActivityResponse> =
        userActivityRepository.findAll().map { UserActivityMapper.toResponse(it) }

    override fun findById(id: Long): UserActivityResponse {
        val userActivity = userActivityRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Actividad de usuario con ID $id no encontrada") }
        return UserActivityMapper.toResponse(userActivity)
    }

    override fun save(request: UserActivityRequest): UserActivityResponse {
        val user = userRepository.findById(request.userId)
            .orElseThrow { EntityNotFoundException("Usuario con ID ${request.userId} no encontrado") }

        val userActivity = UserActivityMapper.toEntity(request, user)
        val savedUserActivity = userActivityRepository.save(userActivity)
        return UserActivityMapper.toResponse(savedUserActivity)
    }

    override fun update(id: Long, request: UserActivityRequest): UserActivityResponse {
        val existingUserActivity = userActivityRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Actividad de usuario con ID $id no encontrada") }

        val user = userRepository.findById(request.userId)
            .orElseThrow { EntityNotFoundException("Usuario con ID ${request.userId} no encontrado") }

        val updatedUserActivity = UserActivity(
            user = user,
            actionType = request.actionType,
            tableAffected = request.tableAffected,
            recordId = request.recordId,
            description = request.description,
            ipAddress = request.ipAddress,
            userAgent = request.userAgent
        ).apply { this.id = existingUserActivity.id }

        val savedUserActivity = userActivityRepository.save(updatedUserActivity)
        return UserActivityMapper.toResponse(savedUserActivity)
    }

    override fun delete(id: Long) {
        if (!userActivityRepository.existsById(id)) {
            throw EntityNotFoundException("Actividad de usuario con ID $id no encontrada")
        }
        userActivityRepository.deleteById(id)
    }
}