package com.puce.chocorocks_backend.exceptions

class InsufficientStockException(
    productName: String,
    requested: Int,
    available: Int,
    detalles: List<String> = emptyList()
) : BusinessException(
    message = "Stock insuficiente para '$productName'. Solicitado: $requested, Disponible: $available",
    detalles = detalles
)