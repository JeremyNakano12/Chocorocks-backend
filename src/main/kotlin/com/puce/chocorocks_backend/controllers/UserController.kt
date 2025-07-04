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
@RequestMapping(Routes.BASE_URL + Routes.USERS)
class UserController(
    private val userService: UserService
) {

    @GetMapping
    fun getAllUsers(): ResponseEntity<List<UserResponse>> {
        val users = userService.findAll()
        return ResponseEntity.ok(users)
    }

    @GetMapping(Routes.ID)
    fun getUserById(@PathVariable id: Long): ResponseEntity<UserResponse> {
        return try {
            val user = userService.findById(id)
            ResponseEntity.ok(user)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun createUser(@RequestBody request: UserRequest): ResponseEntity<UserResponse> {
        val createdUser = userService.save(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser)
    }

    @PutMapping(Routes.ID)
    fun updateUser(
        @PathVariable id: Long,
        @RequestBody request: UserRequest
    ): ResponseEntity<UserResponse> {
        return try {
            val updatedUser = userService.update(id, request)
            ResponseEntity.ok(updatedUser)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping(Routes.ID)
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Void> {
        return try {
            userService.delete(id)
            ResponseEntity.noContent().build()
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }
}