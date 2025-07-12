package com.puce.chocorocks_backend.exceptions

class InvalidDateException(
    field: String,
    reason: String,
    detalles: List<String> = emptyList()
) : BusinessException(
    message = "Fecha inválida en '$field': $reason",
    detalles = detalles
)