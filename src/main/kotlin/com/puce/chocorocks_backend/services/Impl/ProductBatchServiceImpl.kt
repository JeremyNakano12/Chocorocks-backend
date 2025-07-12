package com.puce.chocorocks_backend.services.Impl

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*
import com.puce.chocorocks_backend.mappers.*
import com.puce.chocorocks_backend.repositories.*
import com.puce.chocorocks_backend.services.*
import com.puce.chocorocks_backend.models.entities.*
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.puce.chocorocks_backend.exceptions.*
import com.puce.chocorocks_backend.utils.*

@Service
@Transactional
class ProductBatchServiceImpl(
    private val productBatchRepository: ProductBatchRepository,
    private val productRepository: ProductRepository,
    private val storeRepository: StoreRepository
) : ProductBatchService {

    override fun findAll(): List<ProductBatchResponse> =
        productBatchRepository.findAll().map { ProductBatchMapper.toResponse(it) }

    override fun findById(id: Long): ProductBatchResponse {
        val batch = productBatchRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Lote de producto",
                    identifier = id,
                    detalles = listOf("Verifique que el ID del lote sea correcto")
                )
            }
        return ProductBatchMapper.toResponse(batch)
    }

    override fun save(request: ProductBatchRequest): ProductBatchResponse {
        ValidationUtils.validatePositiveQuantity(request.initialQuantity, "cantidad inicial")
        ValidationUtils.validatePositiveQuantity(request.currentQuantity, "cantidad actual")

        ValidationUtils.validatePositivePrice(request.batchCost, "lote")

        ValidationUtils.validateExpirationDate(request.expirationDate, request.productionDate)

        validateBatchData(request)

        val batchCodeExists = productBatchRepository.existsByBatchCode(request.batchCode)
        ValidationUtils.validateUniqueCode(batchCodeExists, request.batchCode, "Lote")

        val product = productRepository.findById(request.productId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Producto",
                    identifier = request.productId,
                    detalles = listOf("Seleccione un producto válido")
                )
            }

        val store = request.storeId?.let {
            storeRepository.findById(it)
                .orElseThrow {
                    ResourceNotFoundException(
                        resourceName = "Tienda",
                        identifier = it,
                        detalles = listOf("Seleccione una tienda válida")
                    )
                }
        }

        val batch = ProductBatchMapper.toEntity(request, product, store)
        val savedBatch = productBatchRepository.save(batch)
        return ProductBatchMapper.toResponse(savedBatch)
    }

    override fun update(id: Long, request: ProductBatchRequest): ProductBatchResponse {
        val existingBatch = productBatchRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Lote de producto",
                    identifier = id,
                    detalles = listOf("No se puede actualizar un lote que no existe")
                )
            }

        ValidationUtils.validatePositiveQuantity(request.initialQuantity, "cantidad inicial")
        ValidationUtils.validatePositiveQuantity(request.currentQuantity, "cantidad actual")
        ValidationUtils.validatePositivePrice(request.batchCost, "lote")
        ValidationUtils.validateExpirationDate(request.expirationDate, request.productionDate)

        validateBatchData(request)

        val batchCodeExists = productBatchRepository.findAll()
            .any { it.batchCode == request.batchCode && it.id != id }
        if (batchCodeExists) {
            throw DuplicateResourceException(
                resourceName = "Lote",
                field = "código",
                value = request.batchCode,
                detalles = listOf("Ya existe otro lote con este código")
            )
        }

        val product = productRepository.findById(request.productId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Producto",
                    identifier = request.productId,
                    detalles = listOf("Seleccione un producto válido")
                )
            }

        val store = request.storeId?.let {
            storeRepository.findById(it)
                .orElseThrow {
                    ResourceNotFoundException(
                        resourceName = "Tienda",
                        identifier = it,
                        detalles = listOf("Seleccione una tienda válida")
                    )
                }
        }

        val updatedBatch = ProductBatch(
            batchCode = request.batchCode,
            product = product,
            productionDate = request.productionDate,
            expirationDate = request.expirationDate,
            initialQuantity = request.initialQuantity,
            currentQuantity = request.currentQuantity,
            batchCost = request.batchCost,
            store = store,
            isActive = request.isActive
        ).apply { this.id = existingBatch.id }

        val savedBatch = productBatchRepository.save(updatedBatch)
        return ProductBatchMapper.toResponse(savedBatch)
    }

    override fun delete(id: Long) {
        val batch = productBatchRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Lote de producto",
                    identifier = id,
                    detalles = listOf("No se puede eliminar un lote que no existe")
                )
            }

        if (batch.currentQuantity > 0) {
            throw InvalidOperationException(
                operation = "eliminar el lote '${batch.batchCode}'",
                reason = "aún tiene stock disponible",
                detalles = listOf("Solo se pueden eliminar lotes sin stock")
            )
        }

        try {
            productBatchRepository.deleteById(id)
        } catch (ex: Exception) {
            throw InvalidOperationException(
                operation = "eliminar el lote '${batch.batchCode}'",
                reason = "tiene ventas asociadas",
                detalles = listOf("No se puede eliminar un lote con historial de ventas")
            )
        }
    }

    private fun validateBatchData(request: ProductBatchRequest) {
        if (request.batchCode.isBlank()) {
            throw BusinessValidationException(
                message = "El código del lote no puede estar vacío",
                detalles = listOf("Proporcione un código único para el lote")
            )
        }

        if (request.currentQuantity > request.initialQuantity) {
            throw BusinessValidationException(
                message = "La cantidad actual no puede ser mayor a la cantidad inicial",
                detalles = listOf("Inicial: ${request.initialQuantity}, Actual: ${request.currentQuantity}")
            )
        }
    }
}
