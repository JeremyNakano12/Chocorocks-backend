package com.puce.chocorocks_backend.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.view.RedirectView
import java.time.LocalDateTime

@RestController
class HomeController {

    @GetMapping("/")
    fun home(): RedirectView {
        return RedirectView("/api-info")
    }

    @GetMapping("/api-info")
    fun apiInfo(): ResponseEntity<Map<String, Any>> {
        val info = mapOf(
            "application" to "Chocorocks Backend API",
            "version" to "1.0.0",
            "status" to "running",
            "timestamp" to LocalDateTime.now().toString(),
            "endpoints" to mapOf(
                "categories" to "/chocorocks/api/categories",
                "products" to "/chocorocks/api/products",
                "stores" to "/chocorocks/api/stores",
                "users" to "/chocorocks/api/users",
                "clients" to "/chocorocks/api/clients",
                "sales" to "/chocorocks/api/sales",
                "receipts" to "/chocorocks/api/receipts"
            ),
            "documentation" to "API REST para sistema de gestión Chocorocks",
            "support" to "soporte@chocorocks.com"
        )

        return ResponseEntity.ok(info)
    }

    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(
            mapOf(
                "status" to "Working",
                "timestamp" to LocalDateTime.now().toString()
            )
        )
    }


}