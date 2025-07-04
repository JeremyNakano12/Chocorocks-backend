package com.puce.chocorocks_backend.controllers

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*
import com.puce.chocorocks_backend.services.*
import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import com.puce.chocorocks_backend.routes.Routes

@RestController
@RequestMapping(Routes.BASE_URL + Routes.CATEGORIES)
class CategoryController(
    private val categoryService: CategoryService
) {

    @GetMapping
    fun getAllCategories(): ResponseEntity<List<CategoryResponse>> {
        val categories = categoryService.findAll()
        return ResponseEntity.ok(categories)
    }

    @GetMapping(Routes.ID)
    fun getCategoryById(@PathVariable id: Long): ResponseEntity<CategoryResponse> {
        return try {
            val category = categoryService.findById(id)
            ResponseEntity.ok(category)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun createCategory(@RequestBody request: CategoryRequest): ResponseEntity<CategoryResponse> {
        val createdCategory = categoryService.save(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory)
    }

    @PutMapping(Routes.ID)
    fun updateCategory(
        @PathVariable id: Long,
        @RequestBody request: CategoryRequest
    ): ResponseEntity<CategoryResponse> {
        return try {
            val updatedCategory = categoryService.update(id, request)
            ResponseEntity.ok(updatedCategory)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping(Routes.ID)
    fun deleteCategory(@PathVariable id: Long): ResponseEntity<Void> {
        return try {
            categoryService.delete(id)
            ResponseEntity.noContent().build()
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }
}