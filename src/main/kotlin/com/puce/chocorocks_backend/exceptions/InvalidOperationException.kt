package com.puce.chocorocks_backend.exceptions

class InvalidOperationException(
    operation: String,
    reason: String,
    detalles: List<String> = emptyList()
) : BusinessException(
    message = "No se puede $operation: $reason",
    detalles = detalles
)