package com.puce.chocorocks_backend.exceptions

class ExpiredBatchException(
    batchCode: String,
    expirationDate: String,
    detalles: List<String> = emptyList()
) : BusinessException(
    message = "El lote '$batchCode' está vencido desde $expirationDate",
    detalles = detalles
)