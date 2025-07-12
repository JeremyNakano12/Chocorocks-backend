package com.puce.chocorocks_backend.exceptions

class ResourceNotFoundException(
    resourceName: String,
    identifier: Any,
    detalles: List<String> = emptyList()
) : BusinessException(
    message = "$resourceName con ID $identifier no encontrado",
    detalles = detalles
)