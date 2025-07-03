package com.puce.chocorocks_backend.dtos.requests

data class CategoryRequest(
    val name: String,
    val description: String? = null
)