package com.puce.chocorocks_backend.dtos.responses

data class CategoryResponse(
    val id: Long,
    val name: String,
    val description: String?
)