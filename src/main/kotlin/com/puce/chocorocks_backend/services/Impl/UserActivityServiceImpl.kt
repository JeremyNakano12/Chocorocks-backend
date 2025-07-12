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
import com.puce.chocorocks_backend.exceptions.*

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
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Actividad de usuario",
                    identifier = id,
                    detalles = listOf("Verifique que el ID de la actividad sea correcto")
                )
            }
        return UserActivityMapper.toResponse(userActivity)
    }

    override fun save(request: UserActivityRequest): UserActivityResponse {
        // Validaciones de datos requeridos
        validateActivityData(request)

        // Validar que el usuario existe
        val user = userRepository.findById(request.userId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Usuario",
                    identifier = request.userId,
                    detalles = listOf("Seleccione un usuario válido")
                )
            }

        val userActivity = UserActivityMapper.toEntity(request, user)
        val savedUserActivity = userActivityRepository.save(userActivity)
        return UserActivityMapper.toResponse(savedUserActivity)
    }

    override fun update(id: Long, request: UserActivityRequest): UserActivityResponse {
        val existingUserActivity = userActivityRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Actividad de usuario",
                    identifier = id,
                    detalles = listOf("No se puede actualizar una actividad que no existe")
                )
            }

        // Validaciones similares al save
        validateActivityData(request)

        val user = userRepository.findById(request.userId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Usuario",
                    identifier = request.userId,
                    detalles = listOf("Seleccione un usuario válido")
                )
            }

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
        val userActivity = userActivityRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Actividad de usuario",
                    identifier = id,
                    detalles = listOf("No se puede eliminar una actividad que no existe")
                )
            }

        // Normalmente las actividades de usuario no se eliminan por auditoría
        throw InvalidOperationException(
            operation = "eliminar la actividad de usuario",
            reason = "las actividades se mantienen por auditoría",
            detalles = listOf("Las actividades de usuario no se pueden eliminar para mantener el registro de auditoría")
        )
    }

    private fun validateActivityData(request: UserActivityRequest) {
        if (request.actionType.isBlank()) {
            throw BusinessValidationException(
                message = "El tipo de acción no puede estar vacío",
                detalles = listOf("Proporcione un tipo de acción válido (CREATE, UPDATE, DELETE, etc.)")
            )
        }

        if (request.description.isBlank()) {
            throw BusinessValidationException(
                message = "La descripción de la actividad no puede estar vacía",
                detalles = listOf("Proporcione una descripción clara de la actividad realizada")
            )
        }

        // Validar que si se proporciona recordId, también se proporcione tableAffected
        if (request.recordId != null && request.tableAffected.isNullOrBlank()) {
            throw BusinessValidationException(
                message = "Debe especificar la tabla afectada cuando se proporciona ID de registro",
                detalles = listOf("Complete el campo 'tableAffected' para identificar la entidad modificada")
            )
        }

        // Validar formatos básicos
        request.ipAddress?.let { ip ->
            if (ip.isNotBlank() && !isValidIpFormat(ip)) {
                throw BusinessValidationException(
                    message = "Formato de dirección IP inválido",
                    detalles = listOf("IP proporcionada: $ip")
                )
            }
        }
    }

    private fun isValidIpFormat(ip: String): Boolean {
        // Validación básica de formato IP (IPv4)
        val ipPattern = Regex("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")
        return ipPattern.matches(ip) || ip == "localhost" || ip.startsWith("::") // IPv6 básico
    }
}