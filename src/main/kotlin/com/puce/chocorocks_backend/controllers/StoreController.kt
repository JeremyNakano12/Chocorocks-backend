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
@RequestMapping(Routes.BASE_URL + Routes.STORES)
class StoreController(
    private val storeService: StoreService
) {

    @GetMapping
    fun getAllStores(): ResponseEntity<List<StoreResponse>> {
        val stores = storeService.findAll()
        return ResponseEntity.ok(stores)
    }

    @GetMapping(Routes.ID)
    fun getStoreById(@PathVariable id: Long): ResponseEntity<StoreResponse> {
        return try {
            val store = storeService.findById(id)
            ResponseEntity.ok(store)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun createStore(@RequestBody request: StoreRequest): ResponseEntity<StoreResponse> {
        return try {
            val createdStore = storeService.save(request)
            ResponseEntity.status(HttpStatus.CREATED).body(createdStore)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.badRequest().build()
        }
    }

    @PutMapping(Routes.ID)
    fun updateStore(
        @PathVariable id: Long,
        @RequestBody request: StoreRequest
    ): ResponseEntity<StoreResponse> {
        return try {
            val updatedStore = storeService.update(id, request)
            ResponseEntity.ok(updatedStore)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping(Routes.ID)
    fun deleteStore(@PathVariable id: Long): ResponseEntity<Void> {
        return try {
            storeService.delete(id)
            ResponseEntity.noContent().build()
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }
}
