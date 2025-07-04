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
@RequestMapping(Routes.BASE_URL + Routes.CLIENTS)
class ClientController(
    private val clientService: ClientService
) {

    @GetMapping
    fun getAllClients(): ResponseEntity<List<ClientResponse>> {
        val clients = clientService.findAll()
        return ResponseEntity.ok(clients)
    }

    @GetMapping(Routes.ID)
    fun getClientById(@PathVariable id: Long): ResponseEntity<ClientResponse> {
        return try {
            val client = clientService.findById(id)
            ResponseEntity.ok(client)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun createClient(@RequestBody request: ClientRequest): ResponseEntity<ClientResponse> {
        val createdClient = clientService.save(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdClient)
    }

    @PutMapping(Routes.ID)
    fun updateClient(
        @PathVariable id: Long,
        @RequestBody request: ClientRequest
    ): ResponseEntity<ClientResponse> {
        return try {
            val updatedClient = clientService.update(id, request)
            ResponseEntity.ok(updatedClient)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping(Routes.ID)
    fun deleteClient(@PathVariable id: Long): ResponseEntity<Void> {
        return try {
            clientService.delete(id)
            ResponseEntity.noContent().build()
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }
}