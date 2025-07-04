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
@RequestMapping(Routes.BASE_URL + Routes.SALE_DETAILS)
class SaleDetailController(
    private val saleDetailService: SaleDetailService
) {

    @GetMapping
    fun getAllSaleDetails(): ResponseEntity<List<SaleDetailResponse>> {
        val saleDetails = saleDetailService.findAll()
        return ResponseEntity.ok(saleDetails)
    }

    @GetMapping(Routes.ID)
    fun getSaleDetailById(@PathVariable id: Long): ResponseEntity<SaleDetailResponse> {
        return try {
            val saleDetail = saleDetailService.findById(id)
            ResponseEntity.ok(saleDetail)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun createSaleDetail(@RequestBody request: SaleDetailRequest): ResponseEntity<SaleDetailResponse> {
        return try {
            val createdSaleDetail = saleDetailService.save(request)
            ResponseEntity.status(HttpStatus.CREATED).body(createdSaleDetail)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.badRequest().build()
        }
    }

    @PutMapping(Routes.ID)
    fun updateSaleDetail(
        @PathVariable id: Long,
        @RequestBody request: SaleDetailRequest
    ): ResponseEntity<SaleDetailResponse> {
        return try {
            val updatedSaleDetail = saleDetailService.update(id, request)
            ResponseEntity.ok(updatedSaleDetail)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping(Routes.ID)
    fun deleteSaleDetail(@PathVariable id: Long): ResponseEntity<Void> {
        return try {
            saleDetailService.delete(id)
            ResponseEntity.noContent().build()
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }
}