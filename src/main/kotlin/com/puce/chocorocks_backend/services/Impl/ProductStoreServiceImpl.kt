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
class ProductStoreServiceImpl(
    private val productStoreRepository: ProductStoreRepository,
    private val productRepository: ProductRepository,
    private val storeRepository: StoreRepository
) : ProductStoreService {

    override fun findAll(): List<ProductStoreResponse> =
        productStoreRepository.findAll().map { ProductStoreMapper.toResponse(it) }

    override fun findById(id: Long): ProductStoreResponse {
        val productStore = productStoreRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Producto-Tienda con ID $id no encontrado") }
        return ProductStoreMapper.toResponse(productStore)
    }

    override fun save(request: ProductStoreRequest): ProductStoreResponse {
        val product = productRepository.findById(request.productId)
            .orElseThrow { EntityNotFoundException("Producto con ID ${request.productId} no encontrado") }

        val store = storeRepository.findById(request.storeId)
            .orElseThrow { EntityNotFoundException("Tienda con ID ${request.storeId} no encontrada") }

        val productStore = ProductStoreMapper.toEntity(request, product, store)
        val savedProductStore = productStoreRepository.save(productStore)
        return ProductStoreMapper.toResponse(savedProductStore)
    }

    override fun update(id: Long, request: ProductStoreRequest): ProductStoreResponse {
        val existingProductStore = productStoreRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Producto-Tienda con ID $id no encontrado") }

        val product = productRepository.findById(request.productId)
            .orElseThrow { EntityNotFoundException("Producto con ID ${request.productId} no encontrado") }

        val store = storeRepository.findById(request.storeId)
            .orElseThrow { EntityNotFoundException("Tienda con ID ${request.storeId} no encontrada") }

        val updatedProductStore = ProductStore(
            product = product,
            store = store,
            currentStock = request.currentStock,
            minStockLevel = request.minStockLevel
        ).apply { this.id = existingProductStore.id }

        val savedProductStore = productStoreRepository.save(updatedProductStore)
        return ProductStoreMapper.toResponse(savedProductStore)
    }

    override fun delete(id: Long) {
        if (!productStoreRepository.existsById(id)) {
            throw EntityNotFoundException("Producto-Tienda con ID $id no encontrado")
        }
        productStoreRepository.deleteById(id)
    }
}