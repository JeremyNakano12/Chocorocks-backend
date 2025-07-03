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
class CategoryServiceImpl(
    private val categoryRepository: CategoryRepository
) : CategoryService {

    override fun findAll(): List<CategoryResponse> =
        categoryRepository.findAll().map { CategoryMapper.toResponse(it) }

    override fun findById(id: Long): CategoryResponse {
        val category = categoryRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Categoría con ID $id no encontrada") }
        return CategoryMapper.toResponse(category)
    }

    override fun save(request: CategoryRequest): CategoryResponse {
        val category = CategoryMapper.toEntity(request)
        val savedCategory = categoryRepository.save(category)
        return CategoryMapper.toResponse(savedCategory)
    }

    override fun update(id: Long, request: CategoryRequest): CategoryResponse {
        val existingCategory = categoryRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Categoría con ID $id no encontrada") }

        val updatedCategory = Category(
            name = request.name,
            description = request.description
        ).apply { this.id = existingCategory.id }

        val savedCategory = categoryRepository.save(updatedCategory)
        return CategoryMapper.toResponse(savedCategory)
    }

    override fun delete(id: Long) {
        if (!categoryRepository.existsById(id)) {
            throw EntityNotFoundException("Categoría con ID $id no encontrada")
        }
        categoryRepository.deleteById(id)
    }
}