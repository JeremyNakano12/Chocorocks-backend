package com.puce.chocorocks_backend.controllers

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*
import com.puce.chocorocks_backend.services.*
import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user-activities")
class UserActivityController(
    private val userActivityService: UserActivityService
) {

    @GetMapping
    fun getAllUserActivities(): ResponseEntity<List<UserActivityResponse>> {
        val activities = userActivityService.findAll()
        return ResponseEntity.ok(activities)
    }

    @GetMapping("/{id}")
    fun getUserActivityById(@PathVariable id: Long): ResponseEntity<UserActivityResponse> {
        return try {
            val activity = userActivityService.findById(id)
            ResponseEntity.ok(activity)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun createUserActivity(@RequestBody request: UserActivityRequest): ResponseEntity<UserActivityResponse> {
        return try {
            val createdActivity = userActivityService.save(request)
            ResponseEntity.status(HttpStatus.CREATED).body(createdActivity)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.badRequest().build()
        }
    }

    @PutMapping("/{id}")
    fun updateUserActivity(
        @PathVariable id: Long,
        @RequestBody request: UserActivityRequest
    ): ResponseEntity<UserActivityResponse> {
        return try {
            val updatedActivity = userActivityService.update(id, request)
            ResponseEntity.ok(updatedActivity)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    fun deleteUserActivity(@PathVariable id: Long): ResponseEntity<Void> {
        return try {
            userActivityService.delete(id)
            ResponseEntity.noContent().build()
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }
}