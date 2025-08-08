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

@Service
@Transactional
class ClientServiceImpl(
    private val clientRepository: ClientRepository,
    private val userActivityService: UserActivityService,
    private val userActivityHelper: UserActivityHelper
) : ClientService {

    override fun findAll(): List<ClientResponse> =
        clientRepository.findAll().map { ClientMapper.toResponse(it) }

    override fun findById(id: Long): ClientResponse {
        val client = clientRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Cliente",
                    identifier = id,
                    detalles = listOf("Verifique que el ID del cliente sea correcto")
                )
            }
        return ClientMapper.toResponse(client)
    }

    override fun save(request: ClientRequest): ClientResponse {
        validateClientData(request)

        val identificationExists = clientRepository.existsByIdentificationNumber(request.identificationNumber)
        if (identificationExists) {
            throw DuplicateResourceException(
                resourceName = "Cliente",
                field = "número de identificación",
                value = request.identificationNumber,
                detalles = listOf("El número de identificación debe ser único")
            )
        }

        request.email?.let { email ->
            val emailExists = clientRepository.existsByEmail(email)
            if (emailExists) {
                throw DuplicateResourceException(
                    resourceName = "Cliente",
                    field = "email",
                    value = email,
                    detalles = listOf("El email debe ser único")
                )
            }
        }

        val client = ClientMapper.toEntity(request)
        val savedClient = clientRepository.save(client)

        try {
            val emailInfo = savedClient.email?.let { " (${it})" } ?: ""
            val invoiceInfo = if (savedClient.requiresInvoice) " - Requiere factura" else ""

            val activityRequest = userActivityHelper.createActivityRequest(
                actionType = "CREATE",
                tableName = "clients",
                recordId = savedClient.id,
                description = "Creó cliente '${savedClient.nameLastname}' (${savedClient.typeIdentification}: ${savedClient.identificationNumber})$emailInfo$invoiceInfo"
            )
            userActivityService.save(activityRequest)

            println("✅ Actividad registrada: Usuario ${userActivityHelper.getCurrentUserEmail()} creó cliente ${savedClient.nameLastname}")
        } catch (e: Exception) {
            println("❌ Error logging user activity: ${e.message}")
        }

        return ClientMapper.toResponse(savedClient)
    }

    override fun update(id: Long, request: ClientRequest): ClientResponse {
        val existingClient = clientRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Cliente",
                    identifier = id,
                    detalles = listOf("No se puede actualizar un cliente que no existe")
                )
            }

        validateClientData(request)

        val identificationExists = clientRepository.findAll()
            .any { it.identificationNumber == request.identificationNumber && it.id != id }
        if (identificationExists) {
            throw DuplicateResourceException(
                resourceName = "Cliente",
                field = "número de identificación",
                value = request.identificationNumber,
                detalles = listOf("Ya existe otro cliente con este número de identificación")
            )
        }

        request.email?.let { email ->
            val emailExists = clientRepository.findAll()
                .any { it.email == email && it.id != id }
            if (emailExists) {
                throw DuplicateResourceException(
                    resourceName = "Cliente",
                    field = "email",
                    value = email,
                    detalles = listOf("Ya existe otro cliente con este email")
                )
            }
        }

        val oldName = existingClient.nameLastname
        val oldIdentification = existingClient.identificationNumber
        val oldEmail = existingClient.email
        val oldActive = existingClient.isActive
        val oldRequiresInvoice = existingClient.requiresInvoice

        val updatedClient = Client(
            nameLastname = request.nameLastname,
            typeIdentification = request.typeIdentification,
            identificationNumber = request.identificationNumber,
            phoneNumber = request.phoneNumber,
            email = request.email,
            address = request.address,
            requiresInvoice = request.requiresInvoice,
            isActive = request.isActive
        ).apply { this.id = existingClient.id }

        val savedClient = clientRepository.save(updatedClient)

        try {
            val changes = mutableListOf<String>()

            if (oldName != request.nameLastname) {
                changes.add("Nombre: '$oldName' → '${request.nameLastname}'")
            }

            if (oldIdentification != request.identificationNumber) {
                changes.add("Identificación: '$oldIdentification' → '${request.identificationNumber}'")
            }

            if (oldEmail != request.email) {
                val oldEmailText = oldEmail ?: "Sin email"
                val newEmailText = request.email ?: "Sin email"
                changes.add("Email: '$oldEmailText' → '$newEmailText'")
            }

            if (oldActive != request.isActive) {
                val statusText = if (request.isActive) "ACTIVADO" else "DESACTIVADO"
                changes.add("Estado: $statusText")
            }

            if (oldRequiresInvoice != request.requiresInvoice) {
                val invoiceText = if (request.requiresInvoice) "Requiere factura" else "No requiere factura"
                changes.add("Facturación: $invoiceText")
            }

            val changesText = if (changes.isNotEmpty()) " (${changes.joinToString(", ")})" else ""

            val activityRequest = userActivityHelper.createActivityRequest(
                actionType = "UPDATE",
                tableName = "clients",
                recordId = savedClient.id,
                description = "Actualizó cliente '${savedClient.nameLastname}' (${savedClient.typeIdentification}: ${savedClient.identificationNumber})$changesText"
            )
            userActivityService.save(activityRequest)
        } catch (e: Exception) {
            println("❌ Error logging user activity: ${e.message}")
        }

        return ClientMapper.toResponse(savedClient)
    }

    override fun delete(id: Long) {
        val client = clientRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Cliente",
                    identifier = id,
                    detalles = listOf("No se puede eliminar un cliente que no existe")
                )
            }

        if (!client.isActive) {
            throw InvalidOperationException(
                operation = "eliminar el cliente '${client.nameLastname}'",
                reason = "ya está inactivo",
                detalles = listOf("Active el cliente antes de eliminarlo o desactive definitivamente")
            )
        }

        try {
            val emailInfo = client.email?.let { " (${it})" } ?: ""
            val activityRequest = userActivityHelper.createActivityRequest(
                actionType = "DELETE",
                tableName = "clients",
                recordId = client.id,
                description = "Eliminó cliente '${client.nameLastname}' (${client.typeIdentification}: ${client.identificationNumber})$emailInfo"
            )
            userActivityService.save(activityRequest)
        } catch (e: Exception) {
            println("❌ Error logging user activity: ${e.message}")
        }

        clientRepository.deleteById(id)
    }

    private fun validateClientData(request: ClientRequest) {
        if (request.nameLastname.isBlank()) {
            throw BusinessValidationException(
                message = "El nombre y apellido no pueden estar vacíos",
                detalles = listOf("Proporcione el nombre completo del cliente")
            )
        }

        if (request.identificationNumber.isBlank()) {
            throw BusinessValidationException(
                message = "El número de identificación no puede estar vacío",
                detalles = listOf("Proporcione un número de identificación válido")
            )
        }

        request.email?.let { email ->
            if (email.isNotBlank() && !email.contains("@")) {
                throw BusinessValidationException(
                    message = "El formato del email es inválido",
                    detalles = listOf("Proporcione un email válido (ejemplo@dominio.com)")
                )
            }
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