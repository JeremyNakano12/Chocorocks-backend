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
class ProductServiceImpl(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
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

        val category = categoryRepository.findById(request.categoryId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Categoría",
                    identifier = request.categoryId,
                    detalles = listOf("Seleccione una categoría válida")
                )
            }

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

        try {
            productRepository.deleteById(id)
        } catch (ex: Exception) {
            throw InvalidOperationException(
                operation = "eliminar el producto '${product.nameProduct}'",
                reason = "tiene ventas o movimientos de inventario asociados",
                detalles = listOf("No se puede eliminar un producto con historial de ventas")
            )
        }
    }

    private fun validateProductData(request: ProductRequest) {
        if (request.code.isBlank()) {
            throw BusinessValidationException(
                message = "El código del producto no puede estar vacío",
                detalles = listOf("Proporcione un código único para el producto")
            )
        }

        if (request.nameProduct.isBlank()) {
            throw BusinessValidationException(
                message = "El nombre del producto no puede estar vacío",
                detalles = listOf("Proporcione un nombre descriptivo para el producto")
            )
        }
    }
}