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
@RequestMapping(Routes.BASE_URL + Routes.PRODUCT_BATCHES)
class ProductBatchController(
    private val productBatchService: ProductBatchService
) {

    @GetMapping
    fun getAllProductBatches(): ResponseEntity<List<ProductBatchResponse>> {
        val batches = productBatchService.findAll()
        return ResponseEntity.ok(batches)
    }

    @GetMapping(Routes.ID)
    fun getProductBatchById(@PathVariable id: Long): ResponseEntity<ProductBatchResponse> {
        return try {
            val batch = productBatchService.findById(id)
            ResponseEntity.ok(batch)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun createProductBatch(@RequestBody request: ProductBatchRequest): ResponseEntity<ProductBatchResponse> {
        return try {
            val createdBatch = productBatchService.save(request)
            ResponseEntity.status(HttpStatus.CREATED).body(createdBatch)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.badRequest().build()
        }
    }

    @PutMapping(Routes.ID)
    fun updateProductBatch(
        @PathVariable id: Long,
        @RequestBody request: ProductBatchRequest
    ): ResponseEntity<ProductBatchResponse> {
        return try {
            val updatedBatch = productBatchService.update(id, request)
            ResponseEntity.ok(updatedBatch)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping(Routes.ID)
    fun deleteProductBatch(@PathVariable id: Long): ResponseEntity<Void> {
        return try {
            productBatchService.delete(id)
            ResponseEntity.noContent().build()
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }
}