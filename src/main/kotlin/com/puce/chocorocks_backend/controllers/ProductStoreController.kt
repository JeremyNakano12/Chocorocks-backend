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
@RequestMapping(Routes.BASE_URL + Routes.PRODUCT_STORES)
class ProductStoreController(
    private val productStoreService: ProductStoreService
) {

    @GetMapping
    fun getAllProductStores(): ResponseEntity<List<ProductStoreResponse>> {
        val productStores = productStoreService.findAll()
        return ResponseEntity.ok(productStores)
    }

    @GetMapping(Routes.ID)
    fun getProductStoreById(@PathVariable id: Long): ResponseEntity<ProductStoreResponse> {
        return try {
            val productStore = productStoreService.findById(id)
            ResponseEntity.ok(productStore)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun createProductStore(@RequestBody request: ProductStoreRequest): ResponseEntity<ProductStoreResponse> {
        return try {
            val createdProductStore = productStoreService.save(request)
            ResponseEntity.status(HttpStatus.CREATED).body(createdProductStore)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.badRequest().build()
        }
    }

    @PutMapping(Routes.ID)
    fun updateProductStore(
        @PathVariable id: Long,
        @RequestBody request: ProductStoreRequest
    ): ResponseEntity<ProductStoreResponse> {
        return try {
            val updatedProductStore = productStoreService.update(id, request)
            ResponseEntity.ok(updatedProductStore)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping(Routes.ID)
    fun deleteProductStore(@PathVariable id: Long): ResponseEntity<Void> {
        return try {
            productStoreService.delete(id)
            ResponseEntity.noContent().build()
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }
}