package com.puce.chocorocks_backend.services

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*

interface SaleService {
    fun findAll(): List<SaleResponse>
    fun findById(id: Long): SaleResponse
    fun save(request: SaleRequest): SaleResponse
    fun update(id: Long, request: SaleRequest): SaleResponse
    fun delete(id: Long)
}