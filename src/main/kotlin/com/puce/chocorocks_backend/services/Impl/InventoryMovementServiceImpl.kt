package com.puce.chocorocks_backend.services.Impl

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*
import com.puce.chocorocks_backend.mappers.*
import com.puce.chocorocks_backend.repositories.*
import com.puce.chocorocks_backend.services.*
import com.puce.chocorocks_backend.models.entities.*
import com.puce.chocorocks_backend.utils.UserActivityHelper
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
    private val userRepository: UserRepository,
    private val userActivityService: UserActivityService,
    private val userActivityHelper: UserActivityHelper
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

            if (request.movementType == MovementType.OUT && request.reason != MovementReason.SALE) {
                val currentQuantity = productBatchRepository.getCurrentQuantityById(it) ?: 0
                ValidationUtils.validateSufficientStock(
                    available = currentQuantity,
                    requested = request.quantity,
                    productName = product.nameProduct
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

        val movement = InventoryMovementMapper.toEntity(request, product, batch, fromStore, toStore, user)
        val savedMovement = inventoryMovementRepository.save(movement)

        try {
            val movementTypeText = when (request.movementType) {
                MovementType.IN -> "entrada"
                MovementType.OUT -> "salida"
                MovementType.TRANSFER -> "transferencia"
            }

            val reasonText = when (request.reason) {
                MovementReason.PRODUCTION -> "producción"
                MovementReason.SALE -> "venta"
                MovementReason.TRANSFER -> "transferencia"
                MovementReason.ADJUSTMENT -> "ajuste"
                MovementReason.DAMAGE -> "daño"
                MovementReason.EXPIRED -> "vencimiento"
            }

            val batchInfo = batch?.let { " del lote '${it.batchCode}'" } ?: ""

            val storeInfo = when (request.movementType) {
                MovementType.IN -> toStore?.let { " hacia tienda '${it.name}'" } ?: ""
                MovementType.OUT -> fromStore?.let { " desde tienda '${it.name}'" } ?: ""
                MovementType.TRANSFER -> {
                    val from = fromStore?.name ?: "Unknown"
                    val to = toStore?.name ?: "Unknown"
                    " de '$from' a '$to'"
                }
            }

            val referenceInfo = request.referenceId?.let { " (Ref: ${request.referenceType}-${it})" } ?: ""

            val activityRequest = userActivityHelper.createActivityRequest(
                actionType = "CREATE",
                tableName = "inventory_movements",
                recordId = savedMovement.id,
                description = "Registró $movementTypeText de ${request.quantity} unidades de '${product.nameProduct}'$batchInfo$storeInfo por $reasonText$referenceInfo"
            )
            userActivityService.save(activityRequest)

            println("✅ Actividad registrada: Usuario ${userActivityHelper.getCurrentUserEmail()} registró movimiento de inventario para ${product.nameProduct}")
        } catch (e: Exception) {
            println("❌ Error logging user activity: ${e.message}")
        }

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

        val oldQuantity = existingMovement.quantity
        val oldReason = existingMovement.reason

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

        try {
            val changes = mutableListOf<String>()

            if (oldQuantity != request.quantity) {
                changes.add("Cantidad: $oldQuantity → ${request.quantity}")
            }

            if (oldReason != request.reason) {
                changes.add("Razón: ${oldReason.name} → ${request.reason.name}")
            }

            val changesText = if (changes.isNotEmpty()) " (${changes.joinToString(", ")})" else ""

            val activityRequest = userActivityHelper.createActivityRequest(
                actionType = "UPDATE",
                tableName = "inventory_movements",
                recordId = savedMovement.id,
                description = "Actualizó movimiento de inventario para '${product.nameProduct}'$changesText"
            )
            userActivityService.save(activityRequest)
        } catch (e: Exception) {
            println("❌ Error logging user activity: ${e.message}")
        }

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

        try {
            val movementTypeText = when (movement.movementType) {
                MovementType.IN -> "entrada"
                MovementType.OUT -> "salida"
                MovementType.TRANSFER -> "transferencia"
            }

            val batchInfo = movement.batch?.let { " del lote '${it.batchCode}'" } ?: ""

            val activityRequest = userActivityHelper.createActivityRequest(
                actionType = "DELETE",
                tableName = "inventory_movements",
                recordId = movement.id,
                description = "Eliminó movimiento de $movementTypeText de ${movement.quantity} unidades de '${movement.product.nameProduct}'$batchInfo (Razón: ${movement.reason.name})"
            )
            userActivityService.save(activityRequest)
        } catch (e: Exception) {
            println("❌ Error logging user activity: ${e.message}")
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

        when (request.movementType) {
            MovementType.TRANSFER -> {
                if (request.fromStoreId == null || request.toStoreId == null) {
                    throw BusinessValidationException(
                        message = "Para transferencias se requieren tienda origen y destino",
                        detalles = listOf("Especifique ambas tiendas para la transferencia")
                    )
                }
                if (request.fromStoreId == request.toStoreId) {
                    throw BusinessValidationException(
                        message = "La tienda origen y destino no pueden ser la misma",
                        detalles = listOf("Seleccione tiendas diferentes para la transferencia")
                    )
                }
            }
            MovementType.IN -> {
                if (request.toStoreId == null) {
                    throw BusinessValidationException(
                        message = "Para entradas se requiere especificar la tienda destino",
                        detalles = listOf("Seleccione la tienda donde ingresa el producto")
                    )
                }
            }
            MovementType.OUT -> {
                if (request.fromStoreId == null) {
                    throw BusinessValidationException(
                        message = "Para salidas se requiere especificar la tienda origen",
                        detalles = listOf("Seleccione la tienda desde donde sale el producto")
                    )
                }
            }
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

        return Pair(fromStore, toStore)
    }
}