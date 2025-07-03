package com.puce.chocorocks_backend.services

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*

interface ProductService {
    fun findAll(): List<ProductResponse>
    fun findById(id: Long): ProductResponse
    fun save(request: ProductRequest): ProductResponse
    fun update(id: Long, request: ProductRequest): ProductResponse
    fun delete(id: Long)
}