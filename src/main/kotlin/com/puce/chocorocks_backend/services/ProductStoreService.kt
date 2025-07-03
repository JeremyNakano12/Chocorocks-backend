package com.puce.chocorocks_backend.services

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*

interface ProductStoreService {
    fun findAll(): List<ProductStoreResponse>
    fun findById(id: Long): ProductStoreResponse
    fun save(request: ProductStoreRequest): ProductStoreResponse
    fun update(id: Long, request: ProductStoreRequest): ProductStoreResponse
    fun delete(id: Long)
}