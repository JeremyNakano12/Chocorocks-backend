package com.puce.chocorocks_backend.services.Impl

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*
import com.puce.chocorocks_backend.mappers.*
import com.puce.chocorocks_backend.repositories.*
import com.puce.chocorocks_backend.services.*
import com.puce.chocorocks_backend.models.entities.*
import com.puce.chocorocks_backend.utils.UserActivityHelper
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.puce.chocorocks_backend.exceptions.*
import com.puce.chocorocks_backend.utils.*
import org.slf4j.LoggerFactory

@Service
@Transactional
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val userActivityService: UserActivityService,
    private val userActivityHelper: UserActivityHelper,
    private val supabaseAuthService: SupabaseAuthService // Dependencia de Supabase
) : UserService {

    private val logger = LoggerFactory.getLogger(UserServiceImpl::class.java)

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
        logger.info("🚀 Iniciando creación de usuario: ${request.email}")

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
        logger.info("✅ Usuario guardado en DB local: ${savedUser.email} (ID: ${savedUser.id})")

        try {
            val metadata = mapOf(
                "name" to savedUser.name,
                "role" to savedUser.role.name,
                "user_id" to savedUser.id.toString(),
                "phone" to (savedUser.phoneNumber ?: ""),
                "identification" to savedUser.identificationNumber,
                "identification_type" to savedUser.typeIdentification.name,
                "created_by_admin" to true
            )

            val supabaseUser = supabaseAuthService.createUser(
                email = savedUser.email,
                password = request.password,
                metadata = metadata
            )

            if (supabaseUser == null) {
                logger.error("❌ Error creando usuario en Supabase - Realizando rollback")
                userRepository.delete(savedUser)
                throw RuntimeException("Error creando usuario en Supabase. Usuario no creado.")
            }

            logger.info("✅ Usuario creado en Supabase: ${supabaseUser.id} para ${savedUser.email}")

        } catch (e: Exception) {
            logger.error("❌ Error en sincronización con Supabase: ${e.message}")
            try {
                userRepository.delete(savedUser)
                logger.info("🔄 Rollback completado - Usuario eliminado de DB local")
            } catch (rollbackError: Exception) {
                logger.error("❌ Error en rollback: ${rollbackError.message}")
            }
            throw RuntimeException("Error sincronizando con Supabase: ${e.message}")
        }

        try {
            val roleInfo = when (savedUser.role) {
                UserRole.ADMIN -> "Administrador"
                UserRole.EMPLOYEE -> "Empleado"
            }

            val activityRequest = userActivityHelper.createActivityRequest(
                actionType = "CREATE",
                tableName = "users",
                recordId = savedUser.id,
                description = "Creó usuario '${savedUser.name}' (${savedUser.email}) como $roleInfo - ${savedUser.typeIdentification}: ${savedUser.identificationNumber}. Usuario listo para login."
            )
            userActivityService.save(activityRequest)

            logger.info("✅ Actividad registrada: Usuario ${userActivityHelper.getCurrentUserEmail()} creó usuario ${savedUser.name}")
        } catch (e: Exception) {
            logger.warn("⚠️ Error logging user activity: ${e.message}")
        }

        logger.info("🎉 Usuario creado exitosamente: ${savedUser.email}. Puede hacer login inmediatamente.")
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

        val identificationExists = userRepository.findAll()
            .any { it.identificationNumber == request.identificationNumber && it.id != id }
        if (identificationExists) {
            throw DuplicateResourceException(
                resourceName = "Usuario",
                field = "número de identificación",
                value = request.identificationNumber,
                detalles = listOf("Ya existe otro usuario con este número de identificación")
            )
        }

        val oldName = existingUser.name
        val oldEmail = existingUser.email
        val oldRole = existingUser.role
        val oldActive = existingUser.isActive

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

        try {
            val changes = mutableListOf<String>()

            if (oldName != request.name) {
                changes.add("Nombre: '$oldName' → '${request.name}'")
            }

            if (oldEmail != request.email) {
                changes.add("Email: '$oldEmail' → '${request.email}'")
            }

            if (oldRole != request.role) {
                val oldRoleText = when (oldRole) {
                    UserRole.ADMIN -> "Administrador"
                    UserRole.EMPLOYEE -> "Empleado"
                }
                val newRoleText = when (request.role) {
                    UserRole.ADMIN -> "Administrador"
                    UserRole.EMPLOYEE -> "Empleado"
                }
                changes.add("Rol: $oldRoleText → $newRoleText")
            }

            if (oldActive != request.isActive) {
                val statusText = if (request.isActive) "ACTIVADO" else "DESACTIVADO"
                changes.add("Estado: $statusText")
            }

            val changesText = if (changes.isNotEmpty()) " (${changes.joinToString(", ")})" else ""

            val activityRequest = userActivityHelper.createActivityRequest(
                actionType = "UPDATE",
                tableName = "users",
                recordId = savedUser.id,
                description = "Actualizó usuario '${savedUser.name}' (${savedUser.email})$changesText"
            )
            userActivityService.save(activityRequest)
        } catch (e: Exception) {
            logger.warn("❌ Error logging user activity: ${e.message}")
        }

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
            val activityRequest = userActivityHelper.createActivityRequest(
                actionType = "DELETE",
                tableName = "users",
                recordId = user.id,
                description = "Eliminó usuario '${user.name}' (${user.email}) - ${user.typeIdentification}: ${user.identificationNumber}"
            )
            userActivityService.save(activityRequest)
        } catch (e: Exception) {
            logger.warn("❌ Error logging user activity: ${e.message}")
        }

        userRepository.delete(user)
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

        if (request.password.isBlank()) {
            throw BusinessValidationException(
                message = "La contraseña no puede estar vacía",
                detalles = listOf("Proporcione una contraseña válida")
            )
        }

        if (request.password.length < 6) {
            throw BusinessValidationException(
                message = "La contraseña debe tener al menos 6 caracteres",
                detalles = listOf("Contraseña actual: ${request.password.length} caracteres")
            )
        }

        if (request.identificationNumber.isBlank()) {
            throw BusinessValidationException(
                message = "El número de identificación no puede estar vacío",
                detalles = listOf("Proporcione un número de identificación válido")
            )
        }

        when (request.typeIdentification) {
            IdentificationType.CEDULA -> {
                if (request.identificationNumber.length != 10) {
                    throw BusinessValidationException(
                        message = "La cédula debe tener 10 dígitos",
                        detalles = listOf("Número actual: ${request.identificationNumber}")
                    )
                }
            }
            IdentificationType.RUC -> {
                if (request.identificationNumber.length != 13) {
                    throw BusinessValidationException(
                        message = "El RUC debe tener 13 dígitos",
                        detalles = listOf("Número actual: ${request.identificationNumber}")
                    )
                }
            }
            IdentificationType.PASAPORTE -> {
                if (request.identificationNumber.length < 6 || request.identificationNumber.length > 12) {
                    throw BusinessValidationException(
                        message = "El pasaporte debe tener entre 6 y 12 caracteres",
                        detalles = listOf("Número actual: ${request.identificationNumber}")
                    )
                }
            }
        }
    }
}