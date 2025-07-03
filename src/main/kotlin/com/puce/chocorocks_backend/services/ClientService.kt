package com.puce.chocorocks_backend.services

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*

interface ClientService {
    fun findAll(): List<ClientResponse>
    fun findById(id: Long): ClientResponse
    fun save(request: ClientRequest): ClientResponse
    fun update(id: Long, request: ClientRequest): ClientResponse
    fun delete(id: Long)
}