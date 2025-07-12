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
class ClientServiceImpl(
    private val clientRepository: ClientRepository
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
                detalles = listOf("Ya existe otro cliente con esta identificación")
            )
        }

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

        try {
            clientRepository.deleteById(id)
        } catch (ex: Exception) {
            throw InvalidOperationException(
                operation = "eliminar el cliente '${client.nameLastname}'",
                reason = "tiene ventas asociadas",
                detalles = listOf("No se puede eliminar un cliente con historial de ventas")
            )
        }
    }

    private fun validateClientData(request: ClientRequest) {
        if (request.nameLastname.isBlank()) {
            throw BusinessValidationException(
                message = "El nombre del cliente no puede estar vacío",
                detalles = listOf("Proporcione un nombre válido")
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
                    message = "Formato de email inválido",
                    detalles = listOf("El email debe tener un formato válido (ejemplo@dominio.com)")
                )
            }
        }
    }
}