package com.puce.chocorocks_backend.controllers

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*
import com.puce.chocorocks_backend.services.*
import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/sales")
class SaleController(
    private val saleService: SaleService
) {

    @GetMapping
    fun getAllSales(): ResponseEntity<List<SaleResponse>> {
        val sales = saleService.findAll()
        return ResponseEntity.ok(sales)
    }

    @GetMapping("/{id}")
    fun getSaleById(@PathVariable id: Long): ResponseEntity<SaleResponse> {
        return try {
            val sale = saleService.findById(id)
            ResponseEntity.ok(sale)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun createSale(@RequestBody request: SaleRequest): ResponseEntity<SaleResponse> {
        return try {
            val createdSale = saleService.save(request)
            ResponseEntity.status(HttpStatus.CREATED).body(createdSale)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.badRequest().build()
        }
    }

    @PutMapping("/{id}")
    fun updateSale(
        @PathVariable id: Long,
        @RequestBody request: SaleRequest
    ): ResponseEntity<SaleResponse> {
        return try {
            val updatedSale = saleService.update(id, request)
            ResponseEntity.ok(updatedSale)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    fun deleteSale(@PathVariable id: Long): ResponseEntity<Void> {
        return try {
            saleService.delete(id)
            ResponseEntity.noContent().build()
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }
}