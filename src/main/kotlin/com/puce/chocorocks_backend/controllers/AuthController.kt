package com.puce.chocorocks_backend.controllers

import com.puce.chocorocks_backend.dtos.responses.AuthStatusResponse
import com.puce.chocorocks_backend.dtos.responses.AuthUserResponse
import com.puce.chocorocks_backend.dtos.responses.SupabaseUserResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import com.puce.chocorocks_backend.routes.Routes

@RestController
@RequestMapping(Routes.BASE_URL + "/auth")
class AuthController {

    @GetMapping("/me")
    fun getCurrentUser(): ResponseEntity<AuthUserResponse> {
        val authentication = SecurityContextHolder.getContext().authentication

        return if (authentication?.isAuthenticated == true && authentication.principal is SupabaseUserResponse) {
            val user = authentication.principal as SupabaseUserResponse
            val response = AuthUserResponse(
                id = user.id,
                email = user.email,
                name = user.name,
                role = user.role.name,
                isAuthenticated = true
            )
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    @GetMapping("/status")
    fun getAuthStatus(): ResponseEntity<AuthStatusResponse> {
        val authentication = SecurityContextHolder.getContext().authentication

        val response = if (authentication?.isAuthenticated == true && authentication.principal is SupabaseUserResponse) {
            val user = authentication.principal as SupabaseUserResponse
            AuthStatusResponse(
                isAuthenticated = true,
                user = AuthUserResponse(
                    id = user.id,
                    email = user.email,
                    name = user.name,
                    role = user.role.name,
                    isAuthenticated = true
                )
            )
        } else {
            AuthStatusResponse(
                isAuthenticated = false,
                user = null
            )
        }

        return ResponseEntity.ok(response)
    }

    @PostMapping("/validate")
    fun validateToken(): ResponseEntity<Map<String, Any>> {
        val authentication = SecurityContextHolder.getContext().authentication

        val response = if (authentication?.isAuthenticated == true && authentication.principal is SupabaseUserResponse) {
            val user = authentication.principal as SupabaseUserResponse
            mapOf(
                "valid" to true,
                "message" to "Token válido",
                "user" to mapOf(
                    "id" to user.id,
                    "email" to user.email,
                    "role" to user.role.name
                )
            )
        } else {
            mapOf(
                "valid" to false,
                "message" to "Token inválido o expirado"
            )
        }

        return ResponseEntity.ok(response)
    }

    @GetMapping("/permissions")
    fun getUserPermissions(): ResponseEntity<Map<String, Any>> {
        val authentication = SecurityContextHolder.getContext().authentication

        return if (authentication?.isAuthenticated == true && authentication.principal is SupabaseUserResponse) {
            val user = authentication.principal as SupabaseUserResponse
            val permissions = when (user.role) {
                com.puce.chocorocks_backend.models.entities.UserRole.ADMIN -> listOf(
                    "read", "create", "update", "delete",
                    "manage_users", "manage_stores", "manage_categories",
                    "view_reports", "delete_sales", "cancel_receipts"
                )
                com.puce.chocorocks_backend.models.entities.UserRole.EMPLOYEE -> listOf(
                    "read", "create", "update",
                    "create_sales", "create_receipts", "view_inventory"
                )
            }

            val response = mapOf(
                "user" to mapOf(
                    "id" to user.id,
                    "email" to user.email,
                    "role" to user.role.name
                ),
                "permissions" to permissions,
                "canDelete" to (user.role == com.puce.chocorocks_backend.models.entities.UserRole.ADMIN)
            )

            ResponseEntity.ok(response)
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }
}