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
@RequestMapping(Routes.BASE_URL + Routes.PRODUCTS)
class ProductController(
    private val productService: ProductService
) {

    @GetMapping
    fun getAllProducts(): ResponseEntity<List<ProductResponse>> {
        val products = productService.findAll()
        return ResponseEntity.ok(products)
    }

    @GetMapping(Routes.ID)
    fun getProductById(@PathVariable id: Long): ResponseEntity<ProductResponse> {
        return try {
            val product = productService.findById(id)
            ResponseEntity.ok(product)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun createProduct(@RequestBody request: ProductRequest): ResponseEntity<ProductResponse> {
        return try {
            val createdProduct = productService.save(request)
            ResponseEntity.status(HttpStatus.CREATED).body(createdProduct)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.badRequest().build()
        }
    }

    @PutMapping(Routes.ID)
    fun updateProduct(
        @PathVariable id: Long,
        @RequestBody request: ProductRequest
    ): ResponseEntity<ProductResponse> {
        return try {
            val updatedProduct = productService.update(id, request)
            ResponseEntity.ok(updatedProduct)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping(Routes.ID)
    fun deleteProduct(@PathVariable id: Long): ResponseEntity<Void> {
        return try {
            productService.delete(id)
            ResponseEntity.noContent().build()
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }
}