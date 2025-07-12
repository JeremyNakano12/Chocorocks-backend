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
class CategoryServiceImpl(
    private val categoryRepository: CategoryRepository
) : CategoryService {

    override fun findAll(): List<CategoryResponse> =
        categoryRepository.findAll().map { CategoryMapper.toResponse(it) }

    override fun findById(id: Long): CategoryResponse {
        val category = categoryRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Categoría",
                    identifier = id,
                    detalles = listOf("Verifique que el ID sea correcto")
                )
            }
        return CategoryMapper.toResponse(category)
    }

    override fun save(request: CategoryRequest): CategoryResponse {
        // Validaciones de negocio básicas
        if (request.name.isBlank()) {
            throw BusinessValidationException(
                message = "El nombre de la categoría no puede estar vacío",
                detalles = listOf("Proporcione un nombre válido para la categoría")
            )
        }

        val category = CategoryMapper.toEntity(request)
        val savedCategory = categoryRepository.save(category)
        return CategoryMapper.toResponse(savedCategory)
    }

    override fun update(id: Long, request: CategoryRequest): CategoryResponse {
        val existingCategory = categoryRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Categoría",
                    identifier = id,
                    detalles = listOf("No se puede actualizar una categoría que no existe")
                )
            }

        // Validaciones de negocio
        if (request.name.isBlank()) {
            throw BusinessValidationException(
                message = "El nombre de la categoría no puede estar vacío",
                detalles = listOf("Proporcione un nombre válido para la categoría")
            )
        }

        val updatedCategory = Category(
            name = request.name,
            description = request.description
        ).apply { this.id = existingCategory.id }

        val savedCategory = categoryRepository.save(updatedCategory)
        return CategoryMapper.toResponse(savedCategory)
    }

    override fun delete(id: Long) {
        val category = categoryRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Categoría",
                    identifier = id,
                    detalles = listOf("No se puede eliminar una categoría que no existe")
                )
            }

        try {
            categoryRepository.deleteById(id)
        } catch (ex: Exception) {
            throw InvalidOperationException(
                operation = "eliminar la categoría '${category.name}'",
                reason = "tiene productos asociados",
                detalles = listOf("Elimine primero todos los productos de esta categoría")
            )
        }
    }
}