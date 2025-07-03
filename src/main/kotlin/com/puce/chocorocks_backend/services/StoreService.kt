package com.puce.chocorocks_backend.services

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*

interface StoreService {
    fun findAll(): List<StoreResponse>
    fun findById(id: Long): StoreResponse
    fun save(request: StoreRequest): StoreResponse
    fun update(id: Long, request: StoreRequest): StoreResponse
    fun delete(id: Long)
}