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
class UserServiceImpl(
    private val userRepository: UserRepository
) : UserService {

    override fun findAll(): List<UserResponse> =
        userRepository.findAll().map { UserMapper.toResponse(it) }

    override fun findById(id: Long): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Usuario con ID $id no encontrado") }
        return UserMapper.toResponse(user)
    }

    override fun save(request: UserRequest): UserResponse {
        val user = UserMapper.toEntity(request)
        val savedUser = userRepository.save(user)
        return UserMapper.toResponse(savedUser)
    }

    override fun update(id: Long, request: UserRequest): UserResponse {
        val existingUser = userRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Usuario con ID $id no encontrado") }

        val updatedUser = User(
            name = request.name,
            email = request.email,
            passwordHash = request.passwordHash,
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
        if (!userRepository.existsById(id)) {
            throw EntityNotFoundException("Usuario con ID $id no encontrado")
        }
        userRepository.deleteById(id)
    }
}
