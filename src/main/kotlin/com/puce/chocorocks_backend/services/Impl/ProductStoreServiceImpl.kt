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
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Producto-Tienda",
                    identifier = id,
                    detalles = listOf("Verifique que el ID de la relación sea correcto")
                )
            }
        return ProductStoreMapper.toResponse(productStore)
    }

    override fun save(request: ProductStoreRequest): ProductStoreResponse {
        if (request.currentStock < 0) {
            throw InvalidQuantityException(
                field = "stock actual",
                value = request.currentStock,
                detalles = listOf("El stock actual no puede ser negativo")
            )
        }

        if (request.minStockLevel < 0) {
            throw InvalidQuantityException(
                field = "stock mínimo",
                value = request.minStockLevel,
                detalles = listOf("El stock mínimo no puede ser negativo")
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

        val store = storeRepository.findById(request.storeId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Tienda",
                    identifier = request.storeId,
                    detalles = listOf("Seleccione una tienda válida")
                )
            }

        val relationExists = productStoreRepository.existsByProductIdAndStoreId(
            request.productId,
            request.storeId
        )
        if (relationExists) {
            throw DuplicateResourceException(
                resourceName = "Producto-Tienda",
                field = "relación",
                value = "${product.nameProduct} en ${store.name}",
                detalles = listOf("El producto ya está asignado a esta tienda")
            )
        }

        val productStore = ProductStoreMapper.toEntity(request, product, store)
        val savedProductStore = productStoreRepository.save(productStore)
        return ProductStoreMapper.toResponse(savedProductStore)
    }

    override fun update(id: Long, request: ProductStoreRequest): ProductStoreResponse {
        val existingProductStore = productStoreRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Producto-Tienda",
                    identifier = id,
                    detalles = listOf("No se puede actualizar una relación que no existe")
                )
            }

        if (request.currentStock < 0) {
            throw InvalidQuantityException(
                field = "stock actual",
                value = request.currentStock,
                detalles = listOf("El stock actual no puede ser negativo")
            )
        }

        if (request.minStockLevel < 0) {
            throw InvalidQuantityException(
                field = "stock mínimo",
                value = request.minStockLevel,
                detalles = listOf("El stock mínimo no puede ser negativo")
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

        val store = storeRepository.findById(request.storeId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Tienda",
                    identifier = request.storeId,
                    detalles = listOf("Seleccione una tienda válida")
                )
            }

        val relationExists = productStoreRepository.findAll()
            .any {
                it.product.id == request.productId &&
                        it.store.id == request.storeId &&
                        it.id != id
            }
        if (relationExists) {
            throw DuplicateResourceException(
                resourceName = "Producto-Tienda",
                field = "relación",
                value = "${product.nameProduct} en ${store.name}",
                detalles = listOf("Ya existe otra relación entre este producto y tienda")
            )
        }

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
        val productStore = productStoreRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Producto-Tienda",
                    identifier = id,
                    detalles = listOf("No se puede eliminar una relación que no existe")
                )
            }

        if (productStore.currentStock > 0) {
            throw InvalidOperationException(
                operation = "eliminar la relación '${productStore.product.nameProduct}' de '${productStore.store.name}'",
                reason = "aún tiene stock disponible",
                detalles = listOf("Stock actual: ${productStore.currentStock} unidades")
            )
        }

        productStoreRepository.deleteById(id)
    }
}