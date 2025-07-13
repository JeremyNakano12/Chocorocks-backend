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
import com.puce.chocorocks_backend.utils.*

@Service
@Transactional
class UserServiceImpl(
    private val userRepository: UserRepository
) : UserService {

    override fun findAll(): List<UserResponse> =
        userRepository.findAll().map { UserMapper.toResponse(it) }

    override fun findById(id: Long): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Usuario",
                    identifier = id,
                    detalles = listOf("Verifique que el ID del usuario sea correcto")
                )
            }
        return UserMapper.toResponse(user)
    }

    override fun save(request: UserRequest): UserResponse {
        validateUserData(request)

        val emailExists = userRepository.existsByEmail(request.email)
        ValidationUtils.validateUniqueEmail(emailExists, request.email)

        val identificationExists = userRepository.existsByIdentificationNumber(request.identificationNumber)
        if (identificationExists) {
            throw DuplicateResourceException(
                resourceName = "Usuario",
                field = "número de identificación",
                value = request.identificationNumber,
                detalles = listOf("Ya existe un usuario con este número de identificación")
            )
        }

        val user = UserMapper.toEntity(request)
        val savedUser = userRepository.save(user)
        return UserMapper.toResponse(savedUser)
    }

    override fun update(id: Long, request: UserRequest): UserResponse {
        val existingUser = userRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Usuario",
                    identifier = id,
                    detalles = listOf("No se puede actualizar un usuario que no existe")
                )
            }

        validateUserData(request)

        val emailExists = userRepository.findAll()
            .any { it.email == request.email && it.id != id }
        if (emailExists) {
            throw DuplicateResourceException(
                resourceName = "Usuario",
                field = "email",
                value = request.email,
                detalles = listOf("Ya existe otro usuario con este email")
            )
        }

        val updatedUser = User(
            name = request.name,
            email = request.email,
            role = request.role,
            typeIdentification = request.typeIdentification,
            identificationNumber = request.identificationNumber,
            phoneNumber = request.phoneNumber,
            isActive = request.isActive
        ).apply { this.id = existingUser.id }

        val savedUser = userRepository.save(updatedUser)
        return UserMapper.toResponse(savedUser)
    }

    override fun delete(id: Long) {
        val user = userRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Usuario",
                    identifier = id,
                    detalles = listOf("No se puede eliminar un usuario que no existe")
                )
            }

        try {
            userRepository.deleteById(id)
        } catch (ex: Exception) {
            throw InvalidOperationException(
                operation = "eliminar el usuario '${user.name}'",
                reason = "tiene ventas o actividades asociadas",
                detalles = listOf("No se puede eliminar un usuario con historial en el sistema")
            )
        }
    }

    private fun validateUserData(request: UserRequest) {
        if (request.name.isBlank()) {
            throw BusinessValidationException(
                message = "El nombre del usuario no puede estar vacío",
                detalles = listOf("Proporcione un nombre válido")
            )
        }

        if (request.email.isBlank() || !request.email.contains("@")) {
            throw BusinessValidationException(
                message = "El email del usuario no es válido",
                detalles = listOf("Proporcione un email con formato válido")
            )
        }


        if (request.identificationNumber.isBlank()) {
            throw BusinessValidationException(
                message = "El número de identificación no puede estar vacío",
                detalles = listOf("Proporcione un número de identificación válido")
            )
        }
    }
}

