package com.puce.chocorocks_backend.mappers

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*
import com.puce.chocorocks_backend.models.entities.*

object ClientMapper {
    fun toEntity(request: ClientRequest): Client {
        return Client(
            nameLastname = request.nameLastname,
            typeIdentification = request.typeIdentification,
            identificationNumber = request.identificationNumber,
            phoneNumber = request.phoneNumber,
            email = request.email,
            address = request.address,
            requiresInvoice = request.requiresInvoice,
            isActive = request.isActive
        )
    }

    fun toResponse(entity: Client): ClientResponse {
        return ClientResponse(
            id = entity.id,
            nameLastname = entity.nameLastname,
            typeIdentification = entity.typeIdentification,
            identificationNumber = entity.identificationNumber,
            phoneNumber = entity.phoneNumber,
            email = entity.email,
            address = entity.address,
            requiresInvoice = entity.requiresInvoice,
            isActive = entity.isActive
        )
    }
}