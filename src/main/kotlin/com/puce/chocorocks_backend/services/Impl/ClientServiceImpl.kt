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
class ClientServiceImpl(
    private val clientRepository: ClientRepository
) : ClientService {

    override fun findAll(): List<ClientResponse> =
        clientRepository.findAll().map { ClientMapper.toResponse(it) }

    override fun findById(id: Long): ClientResponse {
        val client = clientRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Cliente con ID $id no encontrado") }
        return ClientMapper.toResponse(client)
    }

    override fun save(request: ClientRequest): ClientResponse {
        val client = ClientMapper.toEntity(request)
        val savedClient = clientRepository.save(client)
        return ClientMapper.toResponse(savedClient)
    }

    override fun update(id: Long, request: ClientRequest): ClientResponse {
        val existingClient = clientRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Cliente con ID $id no encontrado") }

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
        if (!clientRepository.existsById(id)) {
            throw EntityNotFoundException("Cliente con ID $id no encontrado")
        }
        clientRepository.deleteById(id)
    }
}