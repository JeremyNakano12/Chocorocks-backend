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
class ProductBatchServiceImpl(
    private val productBatchRepository: ProductBatchRepository,
    private val productRepository: ProductRepository,
    private val storeRepository: StoreRepository
) : ProductBatchService {

    override fun findAll(): List<ProductBatchResponse> =
        productBatchRepository.findAll().map { ProductBatchMapper.toResponse(it) }

    override fun findById(id: Long): ProductBatchResponse {
        val batch = productBatchRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Lote de producto con ID $id no encontrado") }
        return ProductBatchMapper.toResponse(batch)
    }

    override fun save(request: ProductBatchRequest): ProductBatchResponse {
        val product = productRepository.findById(request.productId)
            .orElseThrow { EntityNotFoundException("Producto con ID ${request.productId} no encontrado") }

        val store = request.storeId?.let {
            storeRepository.findById(it)
                .orElseThrow { EntityNotFoundException("Tienda con ID $it no encontrada") }
        }

        val batch = ProductBatchMapper.toEntity(request, product, store)
        val savedBatch = productBatchRepository.save(batch)
        return ProductBatchMapper.toResponse(savedBatch)
    }

    override fun update(id: Long, request: ProductBatchRequest): ProductBatchResponse {
        val existingBatch = productBatchRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Lote de producto con ID $id no encontrado") }

        val product = productRepository.findById(request.productId)
            .orElseThrow { EntityNotFoundException("Producto con ID ${request.productId} no encontrado") }

        val store = request.storeId?.let {
            storeRepository.findById(it)
                .orElseThrow { EntityNotFoundException("Tienda con ID $it no encontrada") }
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
        if (!productBatchRepository.existsById(id)) {
            throw EntityNotFoundException("Lote de producto con ID $id no encontrado")
        }
        productBatchRepository.deleteById(id)
    }
}