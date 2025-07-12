package com.puce.chocorocks_backend.exceptions

import com.puce.chocorocks_backend.dtos.responses.ErrorResponse
import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(ex: ResourceNotFoundException): ResponseEntity<ErrorResponse> {
        logger.warn("Recurso no encontrado: ${ex.message}")

        val errorResponse = ErrorResponse(
            message = ex.message ?: "Recurso no encontrado",
            status = HttpStatus.NOT_FOUND.value(),
            detalles = ex.detalles
        )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    @ExceptionHandler(DuplicateResourceException::class)
    fun handleDuplicateResource(ex: DuplicateResourceException): ResponseEntity<ErrorResponse> {
        logger.warn("Recurso duplicado: ${ex.message}")

        val errorResponse = ErrorResponse(
            message = ex.message ?: "Recurso duplicado",
            status = HttpStatus.CONFLICT.value(),
            detalles = ex.detalles
        )

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }

    @ExceptionHandler(InsufficientStockException::class)
    fun handleInsufficientStock(ex: InsufficientStockException): ResponseEntity<ErrorResponse> {
        logger.warn("Stock insuficiente: ${ex.message}")

        val errorResponse = ErrorResponse(
            message = ex.message ?: "Stock insuficiente",
            status = HttpStatus.BAD_REQUEST.value(),
            detalles = ex.detalles + "Verifique la disponibilidad del producto"
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(ExpiredBatchException::class)
    fun handleExpiredBatch(ex: ExpiredBatchException): ResponseEntity<ErrorResponse> {
        logger.warn("Lote expirado: ${ex.message}")

        val errorResponse = ErrorResponse(
            message = ex.message ?: "Lote expirado",
            status = HttpStatus.BAD_REQUEST.value(),
            detalles = ex.detalles + "Seleccione un lote vigente"
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(BusinessValidationException::class)
    fun handleBusinessValidation(ex: BusinessValidationException): ResponseEntity<ErrorResponse> {
        logger.warn("Error de validación de negocio: ${ex.message}")

        val errorResponse = ErrorResponse(
            message = ex.message ?: "Error de validación",
            status = HttpStatus.BAD_REQUEST.value(),
            detalles = ex.detalles
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(InvalidOperationException::class)
    fun handleInvalidOperation(ex: InvalidOperationException): ResponseEntity<ErrorResponse> {
        logger.warn("Operación no permitida: ${ex.message}")

        val errorResponse = ErrorResponse(
            message = ex.message ?: "Operación no permitida",
            status = HttpStatus.FORBIDDEN.value(),
            detalles = ex.detalles
        )

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse)
    }

    @ExceptionHandler(InvalidPriceException::class)
    fun handleInvalidPrice(ex: InvalidPriceException): ResponseEntity<ErrorResponse> {
        logger.warn("Precio inválido: ${ex.message}")

        val errorResponse = ErrorResponse(
            message = ex.message ?: "Precio inválido",
            status = HttpStatus.BAD_REQUEST.value(),
            detalles = ex.detalles + "Los precios deben ser mayores a 0"
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(InvalidQuantityException::class)
    fun handleInvalidQuantity(ex: InvalidQuantityException): ResponseEntity<ErrorResponse> {
        logger.warn("Cantidad inválida: ${ex.message}")

        val errorResponse = ErrorResponse(
            message = ex.message ?: "Cantidad inválida",
            status = HttpStatus.BAD_REQUEST.value(),
            detalles = ex.detalles + "Las cantidades deben ser números positivos"
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(InvalidDateException::class)
    fun handleInvalidDate(ex: InvalidDateException): ResponseEntity<ErrorResponse> {
        logger.warn("Fecha inválida: ${ex.message}")

        val errorResponse = ErrorResponse(
            message = ex.message ?: "Fecha inválida",
            status = HttpStatus.BAD_REQUEST.value(),
            detalles = ex.detalles
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFound(ex: EntityNotFoundException): ResponseEntity<ErrorResponse> {
        logger.warn("Entidad no encontrada: ${ex.message}")

        val errorResponse = ErrorResponse(
            message = "Recurso no encontrado",
            status = HttpStatus.NOT_FOUND.value(),
            detalles = listOf("El recurso solicitado no existe")
        )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(ex: DataIntegrityViolationException): ResponseEntity<ErrorResponse> {
        logger.error("Violación de integridad de datos", ex)

        val message = when {
            ex.message?.contains("duplicate", ignoreCase = true) == true ->
                "Ya existe un registro con estos datos"
            ex.message?.contains("foreign key", ignoreCase = true) == true ->
                "No se puede eliminar porque tiene datos relacionados"
            ex.message?.contains("not null", ignoreCase = true) == true ->
                "Falta información requerida"
            else -> "Error de integridad de datos"
        }

        val errorResponse = ErrorResponse(
            message = message,
            status = HttpStatus.CONFLICT.value(),
            detalles = listOf("Verifique que los datos sean correctos y únicos")
        )

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        logger.warn("Errores de validación: ${ex.bindingResult.errorCount} errores")

        val detalles = ex.bindingResult.fieldErrors.map { error ->
            "${error.field}: ${error.defaultMessage}"
        }

        val errorResponse = ErrorResponse(
            message = "Datos de entrada inválidos",
            status = HttpStatus.BAD_REQUEST.value(),
            detalles = detalles
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        logger.warn("Violación de restricciones: ${ex.message}")

        val detalles = ex.constraintViolations.map { violation ->
            "${violation.propertyPath}: ${violation.message}"
        }

        val errorResponse = ErrorResponse(
            message = "Datos inválidos",
            status = HttpStatus.BAD_REQUEST.value(),
            detalles = detalles
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        logger.warn("JSON malformado: ${ex.message}")

        val errorResponse = ErrorResponse(
            message = "Formato de datos incorrecto",
            status = HttpStatus.BAD_REQUEST.value(),
            detalles = listOf("Verifique que el JSON esté bien formado")
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
        logger.warn("Tipo de dato incorrecto: ${ex.message}")

        val errorResponse = ErrorResponse(
            message = "Tipo de dato incorrecto para '${ex.name}'",
            status = HttpStatus.BAD_REQUEST.value(),
            detalles = listOf("Se esperaba ${ex.requiredType?.simpleName ?: "otro tipo"}")
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Error inesperado", ex)

        val errorResponse = ErrorResponse(
            message = "Ha ocurrido un error interno",
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            detalles = listOf("Contacte al administrador del sistema")
        )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}