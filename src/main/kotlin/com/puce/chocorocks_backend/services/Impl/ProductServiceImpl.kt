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
class ProductServiceImpl(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val userActivityService: UserActivityService,
    private val userActivityHelper: UserActivityHelper
) : ProductService {

    override fun findAll(): List<ProductResponse> =
        productRepository.findAll().map { ProductMapper.toResponse(it) }

    override fun findById(id: Long): ProductResponse {
        val product = productRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Producto",
                    identifier = id,
                    detalles = listOf("Verifique que el ID del producto sea correcto")
                )
            }
        return ProductMapper.toResponse(product)
    }

    override fun save(request: ProductRequest): ProductResponse {
        ValidationUtils.validatePositivePrice(request.retailPrice, request.nameProduct)
        ValidationUtils.validatePositivePrice(request.wholesalePrice, request.nameProduct)
        ValidationUtils.validatePositivePrice(request.productionCost, request.nameProduct)

        ValidationUtils.validatePositiveQuantity(request.minStockLevel, "stock mínimo")

        validateProductData(request)

        val codeExists = productRepository.existsByCode(request.code)
        ValidationUtils.validateUniqueCode(codeExists, request.code, "Producto")

        request.barcode?.let { barcode ->
            val barcodeExists = productRepository.existsByBarcode(barcode)
            if (barcodeExists) {
                throw DuplicateResourceException(
                    resourceName = "Producto",
                    field = "código de barras",
                    value = barcode,
                    detalles = listOf("El código de barras debe ser único")
                )
            }
        }

        val category = categoryRepository.findById(request.categoryId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Categoría",
                    identifier = request.categoryId,
                    detalles = listOf("Seleccione una categoría válida")
                )
            }

        val product = ProductMapper.toEntity(request, category)
        val savedProduct = productRepository.save(product)

        try {
            val activityRequest = userActivityHelper.createActivityRequest(
                actionType = "CREATE",
                tableName = "products",
                recordId = savedProduct.id,
                description = "Creó producto '${savedProduct.nameProduct}' (${savedProduct.code}) en categoría '${category.name}' - Precio retail: $${savedProduct.retailPrice}, Stock mínimo: ${savedProduct.minStockLevel}"
            )
            userActivityService.save(activityRequest)

            println("✅ Actividad registrada: Usuario ${userActivityHelper.getCurrentUserEmail()} creó producto ${savedProduct.nameProduct}")
        } catch (e: Exception) {
            println("❌ Error logging user activity: ${e.message}")
        }

        return ProductMapper.toResponse(savedProduct)
    }

    override fun update(id: Long, request: ProductRequest): ProductResponse {
        val existingProduct = productRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Producto",
                    identifier = id,
                    detalles = listOf("No se puede actualizar un producto que no existe")
                )
            }

        ValidationUtils.validatePositivePrice(request.retailPrice, request.nameProduct)
        ValidationUtils.validatePositivePrice(request.wholesalePrice, request.nameProduct)
        ValidationUtils.validatePositivePrice(request.productionCost, request.nameProduct)

        ValidationUtils.validatePositiveQuantity(request.minStockLevel, "stock mínimo")

        validateProductData(request)

        val codeExists = productRepository.findAll()
            .any { it.code == request.code && it.id != id }
        if (codeExists) {
            throw DuplicateResourceException(
                resourceName = "Producto",
                field = "código",
                value = request.code,
                detalles = listOf("Ya existe otro producto con este código")
            )
        }

        request.barcode?.let { barcode ->
            val barcodeExists = productRepository.findAll()
                .any { it.barcode == barcode && it.id != id }
            if (barcodeExists) {
                throw DuplicateResourceException(
                    resourceName = "Producto",
                    field = "código de barras",
                    value = barcode,
                    detalles = listOf("Ya existe otro producto con este código de barras")
                )
            }
        }

        val category = categoryRepository.findById(request.categoryId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Categoría",
                    identifier = request.categoryId,
                    detalles = listOf("Seleccione una categoría válida")
                )
            }

        val oldName = existingProduct.nameProduct
        val oldCode = existingProduct.code
        val oldRetailPrice = existingProduct.retailPrice
        val oldCategory = existingProduct.category.name
        val oldActive = existingProduct.isActive

        val updatedProduct = Product(
            code = request.code,
            nameProduct = request.nameProduct,
            description = request.description,
            category = category,
            flavor = request.flavor,
            size = request.size,
            productionCost = request.productionCost,
            wholesalePrice = request.wholesalePrice,
            retailPrice = request.retailPrice,
            minStockLevel = request.minStockLevel,
            imageUrl = request.imageUrl,
            barcode = request.barcode,
            isActive = request.isActive
        ).apply { this.id = existingProduct.id }

        val savedProduct = productRepository.save(updatedProduct)

        try {
            val changes = mutableListOf<String>()

            if (oldName != request.nameProduct) {
                changes.add("Nombre: '$oldName' → '${request.nameProduct}'")
            }

            if (oldCode != request.code) {
                changes.add("Código: '$oldCode' → '${request.code}'")
            }

            if (oldRetailPrice != request.retailPrice) {
                changes.add("Precio retail: $${oldRetailPrice} → $${request.retailPrice}")
            }

            if (oldCategory != category.name) {
                changes.add("Categoría: '$oldCategory' → '${category.name}'")
            }

            if (oldActive != request.isActive) {
                val statusText = if (request.isActive) "ACTIVADO" else "DESACTIVADO"
                changes.add("Estado: $statusText")
            }

            val changesText = if (changes.isNotEmpty()) " (${changes.joinToString(", ")})" else ""

            val activityRequest = userActivityHelper.createActivityRequest(
                actionType = "UPDATE",
                tableName = "products",
                recordId = savedProduct.id,
                description = "Actualizó producto '${savedProduct.nameProduct}' (${savedProduct.code})$changesText"
            )
            userActivityService.save(activityRequest)
        } catch (e: Exception) {
            println("❌ Error logging user activity: ${e.message}")
        }

        return ProductMapper.toResponse(savedProduct)
    }

    override fun delete(id: Long) {
        val product = productRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Producto",
                    identifier = id,
                    detalles = listOf("No se puede eliminar un producto que no existe")
                )
            }

        if (!product.isActive) {
            throw InvalidOperationException(
                operation = "eliminar el producto '${product.nameProduct}'",
                reason = "ya está inactivo",
                detalles = listOf("Active el producto antes de eliminarlo o desactive definitivamente")
            )
        }

        try {
            val activityRequest = userActivityHelper.createActivityRequest(
                actionType = "DELETE",
                tableName = "products",
                recordId = product.id,
                description = "Eliminó producto '${product.nameProduct}' (${product.code}) de categoría '${product.category.name}' - Precio retail: $${product.retailPrice}"
            )
            userActivityService.save(activityRequest)
        } catch (e: Exception) {
            println("❌ Error logging user activity: ${e.message}")
        }

        productRepository.deleteById(id)
    }

    private fun validateProductData(request: ProductRequest) {
        if (request.nameProduct.isBlank()) {
            throw BusinessValidationException(
                message = "El nombre del producto no puede estar vacío",
                detalles = listOf("Proporcione un nombre descriptivo para el producto")
            )
        }

        if (request.code.isBlank()) {
            throw BusinessValidationException(
                message = "El código del producto no puede estar vacío",
                detalles = listOf("Proporcione un código único para el producto")
            )
        }

        if (request.categoryId <= 0) {
            throw BusinessValidationException(
                message = "Debe seleccionar una categoría válida",
                detalles = listOf("Seleccione una categoría de la lista disponible")
            )
        }
    }
}