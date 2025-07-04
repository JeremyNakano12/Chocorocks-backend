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
@RequestMapping(Routes.BASE_URL + Routes.SALES)
class SaleController(
    private val saleService: SaleService
) {

    @GetMapping
    fun getAllSales(): ResponseEntity<List<SaleResponse>> {
        val sales = saleService.findAll()
        return ResponseEntity.ok(sales)
    }

    @GetMapping(Routes.ID)
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

    @PutMapping(Routes.ID)
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

    @DeleteMapping(Routes.ID)
    fun deleteSale(@PathVariable id: Long): ResponseEntity<Void> {
        return try {
            saleService.delete(id)
            ResponseEntity.noContent().build()
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }
}