package com.puce.chocorocks_backend.services

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*

interface ProductBatchService {
    fun findAll(): List<ProductBatchResponse>
    fun findById(id: Long): ProductBatchResponse
    fun save(request: ProductBatchRequest): ProductBatchResponse
    fun update(id: Long, request: ProductBatchRequest): ProductBatchResponse
    fun delete(id: Long)
}