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
@RequestMapping(Routes.BASE_URL + Routes.INVENTORY_MOVEMENTS)
class InventoryMovementController(
    private val inventoryMovementService: InventoryMovementService
) {

    @GetMapping
    fun getAllInventoryMovements(): ResponseEntity<List<InventoryMovementResponse>> {
        val movements = inventoryMovementService.findAll()
        return ResponseEntity.ok(movements)
    }

    @GetMapping(Routes.ID)
    fun getInventoryMovementById(@PathVariable id: Long): ResponseEntity<InventoryMovementResponse> {
        return try {
            val movement = inventoryMovementService.findById(id)
            ResponseEntity.ok(movement)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun createInventoryMovement(@RequestBody request: InventoryMovementRequest): ResponseEntity<InventoryMovementResponse> {
        return try {
            val createdMovement = inventoryMovementService.save(request)
            ResponseEntity.status(HttpStatus.CREATED).body(createdMovement)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.badRequest().build()
        }
    }

    @PutMapping(Routes.ID)
    fun updateInventoryMovement(
        @PathVariable id: Long,
        @RequestBody request: InventoryMovementRequest
    ): ResponseEntity<InventoryMovementResponse> {
        return try {
            val updatedMovement = inventoryMovementService.update(id, request)
            ResponseEntity.ok(updatedMovement)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping(Routes.ID)
    fun deleteInventoryMovement(@PathVariable id: Long): ResponseEntity<Void> {
        return try {
            inventoryMovementService.delete(id)
            ResponseEntity.noContent().build()
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }
}