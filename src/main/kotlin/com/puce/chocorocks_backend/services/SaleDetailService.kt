package com.puce.chocorocks_backend.services

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*

interface SaleDetailService {
    fun findAll(): List<SaleDetailResponse>
    fun findById(id: Long): SaleDetailResponse
    fun save(request: SaleDetailRequest): SaleDetailResponse
    fun update(id: Long, request: SaleDetailRequest): SaleDetailResponse
    fun delete(id: Long)
}