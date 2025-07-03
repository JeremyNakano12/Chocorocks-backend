package com.puce.chocorocks_backend.services

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*

interface UserService {
    fun findAll(): List<UserResponse>
    fun findById(id: Long): UserResponse
    fun save(request: UserRequest): UserResponse
    fun update(id: Long, request: UserRequest): UserResponse
    fun delete(id: Long)
}