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
            .orElseThrow { EntityNotFoundException("Movimiento de inventario con ID $id no encontrado") }
        return InventoryMovementMapper.toResponse(movement)
    }

    override fun save(request: InventoryMovementRequest): InventoryMovementResponse {
        val product = productRepository.findById(request.productId)
            .orElseThrow { EntityNotFoundException("Producto con ID ${request.productId} no encontrado") }

        val batch = request.batchId?.let {
            productBatchRepository.findById(it)
                .orElseThrow { EntityNotFoundException("Lote con ID $it no encontrado") }
        }

        val fromStore = request.fromStoreId?.let {
            storeRepository.findById(it)
                .orElseThrow { EntityNotFoundException("Tienda origen con ID $it no encontrada") }
        }

        val toStore = request.toStoreId?.let {
            storeRepository.findById(it)
                .orElseThrow { EntityNotFoundException("Tienda destino con ID $it no encontrada") }
        }

        val user = userRepository.findById(request.userId)
            .orElseThrow { EntityNotFoundException("Usuario con ID ${request.userId} no encontrado") }

        val movement = InventoryMovementMapper.toEntity(request, product, batch, fromStore, toStore, user)
        val savedMovement = inventoryMovementRepository.save(movement)
        return InventoryMovementMapper.toResponse(savedMovement)
    }

    override fun update(id: Long, request: InventoryMovementRequest): InventoryMovementResponse {
        val existingMovement = inventoryMovementRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Movimiento de inventario con ID $id no encontrado") }

        val product = productRepository.findById(request.productId)
            .orElseThrow { EntityNotFoundException("Producto con ID ${request.productId} no encontrado") }

        val batch = request.batchId?.let {
            productBatchRepository.findById(it)
                .orElseThrow { EntityNotFoundException("Lote con ID $it no encontrado") }
        }

        val fromStore = request.fromStoreId?.let {
            storeRepository.findById(it)
                .orElseThrow { EntityNotFoundException("Tienda origen con ID $it no encontrada") }
        }

        val toStore = request.toStoreId?.let {
            storeRepository.findById(it)
                .orElseThrow { EntityNotFoundException("Tienda destino con ID $it no encontrada") }
        }

        val user = userRepository.findById(request.userId)
            .orElseThrow { EntityNotFoundException("Usuario con ID ${request.userId} no encontrado") }

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
        if (!inventoryMovementRepository.existsById(id)) {
            throw EntityNotFoundException("Movimiento de inventario con ID $id no encontrado")
        }
        inventoryMovementRepository.deleteById(id)
    }
}