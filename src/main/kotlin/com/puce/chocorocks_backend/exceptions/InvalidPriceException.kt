package com.puce.chocorocks_backend.exceptions

class InvalidPriceException(
    productName: String,
    price: String,
    detalles: List<String> = emptyList()
) : BusinessException(
    message = "Precio inválido para '$productName': $price",
    detalles = detalles
)