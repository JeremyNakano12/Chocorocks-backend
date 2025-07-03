package com.puce.chocorocks_backend.services

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*

interface InventoryMovementService {
    fun findAll(): List<InventoryMovementResponse>
    fun findById(id: Long): InventoryMovementResponse
    fun save(request: InventoryMovementRequest): InventoryMovementResponse
    fun update(id: Long, request: InventoryMovementRequest): InventoryMovementResponse
    fun delete(id: Long)
}