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
class InventoryMovementServiceImpl(
    private val inventoryMovementRepository: InventoryMovementRepository,
    private val productRepository: ProductRepository,
    private val productBatchRepository: ProductBatchRepository,
    private val storeRepository: StoreRepository,
    private val userRepository: UserRepository
) : InventoryMovementService {

    override fun findAll(): List<InventoryMovementResponse> =
        inventoryMovementRepository.findAll().map { InventoryMovementMapper.toResponse(it) }

    override fun findById(id: Long): InventoryMovementResponse {
        val movement = inventoryMovementRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Movimiento de inventario",
                    identifier = id,
                    detalles = listOf("Verifique que el ID del movimiento sea correcto")
                )
            }
        return InventoryMovementMapper.toResponse(movement)
    }

    override fun save(request: InventoryMovementRequest): InventoryMovementResponse {
        ValidationUtils.validatePositiveQuantity(request.quantity, "cantidad")
        validateMovementData(request)

        val product = productRepository.findById(request.productId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Producto",
                    identifier = request.productId,
                    detalles = listOf("Seleccione un producto válido")
                )
            }

        val batch = request.batchId?.let {
            val productBatch = productBatchRepository.findById(it)
                .orElseThrow {
                    ResourceNotFoundException(
                        resourceName = "Lote",
                        identifier = it,
                        detalles = listOf("Seleccione un lote válido")
                    )
                }

            if (productBatch.product.id != request.productId) {
                throw BusinessValidationException(
                    message = "El lote seleccionado no pertenece al producto",
                    detalles = listOf("Lote: ${productBatch.batchCode}, Producto: ${product.nameProduct}")
                )
            }

            productBatch
        }

        val (fromStore, toStore) = validateStoresForMovement(request)

        val user = userRepository.findById(request.userId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Usuario",
                    identifier = request.userId,
                    detalles = listOf("Seleccione un usuario válido")
                )
            }

        validateMovementLogic(request, product, batch)

        val movement = InventoryMovementMapper.toEntity(request, product, batch, fromStore, toStore, user)
        val savedMovement = inventoryMovementRepository.save(movement)
        return InventoryMovementMapper.toResponse(savedMovement)
    }

    override fun update(id: Long, request: InventoryMovementRequest): InventoryMovementResponse {
        val existingMovement = inventoryMovementRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Movimiento de inventario",
                    identifier = id,
                    detalles = listOf("No se puede actualizar un movimiento que no existe")
                )
            }

        ValidationUtils.validatePositiveQuantity(request.quantity, "cantidad")
        validateMovementData(request)

        val product = productRepository.findById(request.productId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Producto",
                    identifier = request.productId,
                    detalles = listOf("Seleccione un producto válido")
                )
            }

        val batch = request.batchId?.let {
            productBatchRepository.findById(it)
                .orElseThrow {
                    ResourceNotFoundException(
                        resourceName = "Lote",
                        identifier = it,
                        detalles = listOf("Seleccione un lote válido")
                    )
                }
        }

        val (fromStore, toStore) = validateStoresForMovement(request)

        val user = userRepository.findById(request.userId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Usuario",
                    identifier = request.userId,
                    detalles = listOf("Seleccione un usuario válido")
                )
            }

        validateMovementLogic(request, product, batch)

        val updatedMovement = InventoryMovement(
            movementType = request.movementType,
            product = product,
            batch = batch,
            fromStore = fromStore,
            toStore = toStore,
            quantity = request.quantity,
            reason = request.reason,
            referenceId = request.referenceId,
            referenceType = request.referenceType,
            user = user,
            notes = request.notes
        ).apply { this.id = existingMovement.id }

        val savedMovement = inventoryMovementRepository.save(updatedMovement)
        return InventoryMovementMapper.toResponse(savedMovement)
    }

    override fun delete(id: Long) {
        val movement = inventoryMovementRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Movimiento de inventario",
                    identifier = id,
                    detalles = listOf("No se puede eliminar un movimiento que no existe")
                )
            }

        if (movement.reason != MovementReason.ADJUSTMENT) {
            throw InvalidOperationException(
                operation = "eliminar el movimiento de inventario",
                reason = "solo se pueden eliminar movimientos de ajuste",
                detalles = listOf("Tipo de movimiento: ${movement.reason}")
            )
        }

        inventoryMovementRepository.deleteById(id)
    }

    private fun validateMovementData(request: InventoryMovementRequest) {
        when (request.reason) {
            MovementReason.ADJUSTMENT, MovementReason.DAMAGE -> {
                if (request.notes.isNullOrBlank()) {
                    throw BusinessValidationException(
                        message = "Las notas son requeridas para movimientos de ${request.reason}",
                        detalles = listOf("Proporcione una explicación del movimiento")
                    )
                }
            }
            else -> {}
        }
    }

    private fun validateStoresForMovement(request: InventoryMovementRequest): Pair<Store?, Store?> {
        val fromStore = request.fromStoreId?.let {
            storeRepository.findById(it)
                .orElseThrow {
                    ResourceNotFoundException(
                        resourceName = "Tienda origen",
                        identifier = it,
                        detalles = listOf("Seleccione una tienda de origen válida")
                    )
                }
        }

        val toStore = request.toStoreId?.let {
            storeRepository.findById(it)
                .orElseThrow {
                    ResourceNotFoundException(
                        resourceName = "Tienda destino",
                        identifier = it,
                        detalles = listOf("Seleccione una tienda de destino válida")
                    )
                }
        }

        when (request.movementType) {
            MovementType.TRANSFER -> {
                if (fromStore == null || toStore == null) {
                    throw BusinessValidationException(
                        message = "Los movimientos de transferencia requieren tienda origen y destino",
                        detalles = listOf("Seleccione ambas tiendas para la transferencia")
                    )
                }
                if (fromStore.id == toStore.id) {
                    throw BusinessValidationException(
                        message = "La tienda origen y destino no pueden ser la misma",
                        detalles = listOf("Seleccione tiendas diferentes para la transferencia")
                    )
                }
            }
            MovementType.IN -> {
                if (toStore == null) {
                    throw BusinessValidationException(
                        message = "Los movimientos de entrada requieren tienda destino",
                        detalles = listOf("Seleccione la tienda donde ingresa el producto")
                    )
                }
            }
            MovementType.OUT -> {
                if (fromStore == null) {
                    throw BusinessValidationException(
                        message = "Los movimientos de salida requieren tienda origen",
                        detalles = listOf("Seleccione la tienda de donde sale el producto")
                    )
                }
            }
        }

        return Pair(fromStore, toStore)
    }

    private fun validateMovementLogic(
        request: InventoryMovementRequest,
        product: Product,
        batch: ProductBatch?
    ) {
        if (request.movementType == MovementType.OUT || request.movementType == MovementType.TRANSFER) {
            batch?.let {
                ValidationUtils.validateSufficientStock(
                    available = it.currentQuantity,
                    requested = request.quantity,
                    productName = product.nameProduct
                )
            }
        }

        if (request.movementType == MovementType.OUT || request.movementType == MovementType.TRANSFER) {
            batch?.let {
                ValidationUtils.validateBatchNotExpired(it.expirationDate, it.batchCode)
            }
        }
    }
}