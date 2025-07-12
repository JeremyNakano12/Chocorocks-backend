package com.puce.chocorocks_backend.exceptions

class InvalidQuantityException(
    field: String,
    value: Int,
    detalles: List<String> = emptyList()
) : BusinessException(
    message = "Cantidad inválida en '$field': $value. Debe ser mayor a 0",
    detalles = detalles
)
