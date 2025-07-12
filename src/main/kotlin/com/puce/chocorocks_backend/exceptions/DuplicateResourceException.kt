package com.puce.chocorocks_backend.exceptions

class DuplicateResourceException(
    resourceName: String,
    field: String,
    value: Any,
    detalles: List<String> = emptyList()
) : BusinessException(
    message = "$resourceName con $field '$value' ya existe",
    detalles = detalles
)