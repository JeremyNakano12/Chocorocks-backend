package com.puce.chocorocks_backend.services

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*

interface CategoryService {
    fun findAll(): List<CategoryResponse>
    fun findById(id: Long): CategoryResponse
    fun save(request: CategoryRequest): CategoryResponse
    fun update(id: Long, request: CategoryRequest): CategoryResponse
    fun delete(id: Long)
}