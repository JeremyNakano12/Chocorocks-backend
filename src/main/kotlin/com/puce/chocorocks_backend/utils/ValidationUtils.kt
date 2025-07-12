package com.puce.chocorocks_backend.utils

import com.puce.chocorocks_backend.exceptions.*
import java.math.BigDecimal
import java.time.LocalDate

object ValidationUtils {

    fun validatePositivePrice(price: BigDecimal, productName: String) {
        if (price <= BigDecimal.ZERO) {
            throw InvalidPriceException(
                productName = productName,
                price = price.toString(),
                detalles = listOf("El precio debe ser mayor a 0")
            )
        }
    }

    fun validatePositiveQuantity(quantity: Int, fieldName: String) {
        if (quantity <= 0) {
            throw InvalidQuantityException(
                field = fieldName,
                value = quantity,
                detalles = listOf("La cantidad debe ser un número positivo")
            )
        }
    }

    fun validateFutureDate(date: LocalDate, fieldName: String) {
        if (date.isBefore(LocalDate.now())) {
            throw InvalidDateException(
                field = fieldName,
                reason = "La fecha debe ser futura",
                detalles = listOf("Fecha mínima: ${LocalDate.now()}")
            )
        }
    }

    fun validateExpirationDate(expirationDate: LocalDate, productionDate: LocalDate) {
        if (expirationDate.isBefore(productionDate)) {
            throw InvalidDateException(
                field = "fecha de expiración",
                reason = "No puede ser anterior a la fecha de producción",
                detalles = listOf("Producción: $productionDate, Expiración: $expirationDate")
            )
        }
    }

    fun validateBatchNotExpired(expirationDate: LocalDate, batchCode: String) {
        if (expirationDate.isBefore(LocalDate.now())) {
            throw ExpiredBatchException(
                batchCode = batchCode,
                expirationDate = expirationDate.toString(),
                detalles = listOf("Seleccione un lote vigente")
            )
        }
    }

    fun validateSufficientStock(available: Int, requested: Int, productName: String) {
        if (available < requested) {
            throw InsufficientStockException(
                productName = productName,
                requested = requested,
                available = available,
                detalles = listOf("Stock actual: $available unidades")
            )
        }
    }

    fun validateUniqueCode(exists: Boolean, code: String, resourceName: String) {
        if (exists) {
            throw DuplicateResourceException(
                resourceName = resourceName,
                field = "código",
                value = code,
                detalles = listOf("El código debe ser único en el sistema")
            )
        }
    }

    fun validateUniqueEmail(exists: Boolean, email: String) {
        if (exists) {
            throw DuplicateResourceException(
                resourceName = "Usuario",
                field = "email",
                value = email,
                detalles = listOf("El email ya está registrado")
            )
        }
    }
}