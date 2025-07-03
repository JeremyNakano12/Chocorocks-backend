package com.puce.chocorocks_backend.services

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*

interface UserActivityService {
    fun findAll(): List<UserActivityResponse>
    fun findById(id: Long): UserActivityResponse
    fun save(request: UserActivityRequest): UserActivityResponse
    fun update(id: Long, request: UserActivityRequest): UserActivityResponse
    fun delete(id: Long)
}