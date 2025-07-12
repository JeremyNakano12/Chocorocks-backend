package com.puce.chocorocks_backend.exceptions

class BusinessValidationException(
    message: String,
    detalles: List<String> = emptyList()
) : BusinessException(message, detalles)