package com.puce.chocorocks_backend.exceptions

abstract class BusinessException(
    message: String,
    val detalles: List<String> = emptyList()
) : RuntimeException(message)