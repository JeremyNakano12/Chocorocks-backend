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
class ProductServiceImpl(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) : ProductService {

    override fun findAll(): List<ProductResponse> =
        productRepository.findAll().map { ProductMapper.toResponse(it) }

    override fun findById(id: Long): ProductResponse {
        val product = productRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Producto con ID $id no encontrado") }
        return ProductMapper.toResponse(product)
    }

    override fun save(request: ProductRequest): ProductResponse {
        val category = categoryRepository.findById(request.categoryId)
            .orElseThrow { EntityNotFoundException("Categoría con ID ${request.categoryId} no encontrada") }

        val product = ProductMapper.toEntity(request, category)
        val savedProduct = productRepository.save(product)
        return ProductMapper.toResponse(savedProduct)
    }

    override fun update(id: Long, request: ProductRequest): ProductResponse {
        val existingProduct = productRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Producto con ID $id no encontrado") }

        val category = categoryRepository.findById(request.categoryId)
            .orElseThrow { EntityNotFoundException("Categoría con ID ${request.categoryId} no encontrada") }

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
        if (!productRepository.existsById(id)) {
            throw EntityNotFoundException("Producto con ID $id no encontrado")
        }
        productRepository.deleteById(id)
    }
}