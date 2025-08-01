package com.puce.chocorocks_backend.controllers

import com.puce.chocorocks_backend.dtos.responses.*
import com.puce.chocorocks_backend.services.*
import com.puce.chocorocks_backend.exceptions.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import com.puce.chocorocks_backend.routes.Routes
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@RestController
@RequestMapping(Routes.BASE_URL + Routes.REPORTS)
class ReportController(
    private val reportService: ReportService
) {

    // ================== REPORTE DE VENTAS ==================
    @GetMapping("/sales")
    fun getSalesReport(
        @RequestParam startDate: String,
        @RequestParam endDate: String,
        @RequestParam(required = false) storeIds: List<Long>?
    ): ResponseEntity<SalesReportResponse> {
        return try {
            val start = parseDate(startDate, "startDate")
            val end = parseDate(endDate, "endDate")
            validateDateRange(start, end)

            val report = reportService.getSalesReport(start, end, storeIds)
            ResponseEntity.ok(report)
        } catch (e: BusinessValidationException) {
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    // ================== REPORTE DE INVENTARIO ==================
    @GetMapping("/inventory")
    fun getInventoryReport(
        @RequestParam(required = false) storeIds: List<Long>?,
        @RequestParam(required = false) categoryIds: List<Long>?
    ): ResponseEntity<InventoryReportResponse> {
        return try {
            val report = reportService.getInventoryReport(storeIds, categoryIds)
            ResponseEntity.ok(report)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    // ================== REPORTE DE RENTABILIDAD ==================
    @GetMapping("/profitability")
    fun getProfitabilityReport(
        @RequestParam startDate: String,
        @RequestParam endDate: String,
        @RequestParam(required = false) storeIds: List<Long>?,
        @RequestParam(required = false) categoryIds: List<Long>?
    ): ResponseEntity<ProfitabilityReportResponse> {
        return try {
            val start = parseDate(startDate, "startDate")
            val end = parseDate(endDate, "endDate")
            validateDateRange(start, end)

            val report = reportService.getProfitabilityReport(start, end, storeIds, categoryIds)
            ResponseEntity.ok(report)
        } catch (e: BusinessValidationException) {
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    // ================== REPORTE DE PRODUCTOS MÁS VENDIDOS ==================
    @GetMapping("/best-selling-products")
    fun getBestSellingProductsReport(
        @RequestParam startDate: String,
        @RequestParam endDate: String,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(required = false) storeIds: List<Long>?,
        @RequestParam(required = false) categoryIds: List<Long>?
    ): ResponseEntity<BestSellingProductsReportResponse> {
        return try {
            val start = parseDate(startDate, "startDate")
            val end = parseDate(endDate, "endDate")
            validateDateRange(start, end)
            validateLimit(limit)

            val report = reportService.getBestSellingProductsReport(start, end, limit, storeIds, categoryIds)
            ResponseEntity.ok(report)
        } catch (e: BusinessValidationException) {
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    // ================== REPORTE DE TRAZABILIDAD ==================
    @GetMapping("/traceability/batch/{batchCode}")
    fun getTraceabilityReport(
        @PathVariable batchCode: String
    ): ResponseEntity<TraceabilityReportResponse> {
        return try {
            if (batchCode.isBlank()) {
                throw BusinessValidationException(
                    message = "El código del lote no puede estar vacío",
                    detalles = listOf("Proporcione un código de lote válido")
                )
            }

            val report = reportService.getTraceabilityReport(batchCode)
            ResponseEntity.ok(report)
        } catch (e: ResourceNotFoundException) {
            ResponseEntity.notFound().build()
        } catch (e: BusinessValidationException) {
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GetMapping("/traceability/product/{productId}")
    fun getTraceabilityReportByProduct(
        @PathVariable productId: Long,
        @RequestParam(required = false) startDate: String?,
        @RequestParam(required = false) endDate: String?
    ): ResponseEntity<List<TraceabilityReportResponse>> {
        return try {
            val start = startDate?.let { parseDate(it, "startDate") }
            val end = endDate?.let { parseDate(it, "endDate") }

            if (start != null && end != null) {
                validateDateRange(start, end)
            }

            val reports = reportService.getTraceabilityReportByProduct(productId, start, end)
            ResponseEntity.ok(reports)
        } catch (e: ResourceNotFoundException) {
            ResponseEntity.notFound().build()
        } catch (e: BusinessValidationException) {
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    // ================== DASHBOARD EJECUTIVO ==================
    @GetMapping("/executive-dashboard")
    fun getExecutiveDashboard(
        @RequestParam startDate: String,
        @RequestParam endDate: String
    ): ResponseEntity<ExecutiveDashboardResponse> {
        return try {
            val start = parseDate(startDate, "startDate")
            val end = parseDate(endDate, "endDate")
            validateDateRange(start, end)

            val dashboard = reportService.getExecutiveDashboard(start, end)
            ResponseEntity.ok(dashboard)
        } catch (e: BusinessValidationException) {
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    // ================== ENDPOINTS AUXILIARES ==================
    @GetMapping("/periods")
    fun getAvailablePeriods(): ResponseEntity<List<String>> {
        return try {
            val periods = reportService.getAvailablePeriods()
            ResponseEntity.ok(periods)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GetMapping("/filters")
    fun getReportFilters(): ResponseEntity<Map<String, Any>> {
        return try {
            val filters = reportService.getReportFilters()
            ResponseEntity.ok(filters)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    // ================== ENDPOINTS DE RESÚMENES RÁPIDOS ==================
    @GetMapping("/summary/sales")
    fun getSalesSummary(
        @RequestParam(defaultValue = "30") days: Int
    ): ResponseEntity<Map<String, Any>> {
        return try {
            val endDate = LocalDate.now()
            val startDate = endDate.minusDays(days.toLong())

            val salesReport = reportService.getSalesReport(startDate, endDate)

            val summary = mapOf(
                "period" to salesReport.period,
                "totalSales" to salesReport.totalSales,
                "totalRevenue" to salesReport.totalRevenue,
                "averageTicket" to salesReport.averageTicket,
                "growthTrend" to calculateGrowthTrend(salesReport.dailySales)
            )

            ResponseEntity.ok(summary)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GetMapping("/summary/inventory")
    fun getInventorySummary(): ResponseEntity<Map<String, Any>> {
        return try {
            val inventoryReport = reportService.getInventoryReport()

            val summary = mapOf(
                "totalProducts" to inventoryReport.totalProducts,
                "totalValue" to inventoryReport.totalValue,
                "lowStockCount" to inventoryReport.stockAlerts.lowStock,
                "outOfStockCount" to inventoryReport.stockAlerts.outOfStock,
                "expiringBatchesCount" to inventoryReport.stockAlerts.expiringSoon
            )

            ResponseEntity.ok(summary)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    // ================== MÉTODOS AUXILIARES PRIVADOS ==================
    private fun parseDate(dateString: String, fieldName: String): LocalDate {
        try {
            return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        } catch (e: DateTimeParseException) {
            throw BusinessValidationException(
                message = "Formato de fecha inválido para $fieldName",
                detalles = listOf("Use el formato yyyy-MM-dd (ej: 2024-01-31)")
            )
        }
    }

    private fun validateDateRange(startDate: LocalDate, endDate: LocalDate) {
        if (startDate.isAfter(endDate)) {
            throw BusinessValidationException(
                message = "La fecha de inicio no puede ser posterior a la fecha de fin",
                detalles = listOf("Verifique el rango de fechas seleccionado")
            )
        }

        val maxRangeDays = 365L
        if (startDate.isBefore(endDate.minusDays(maxRangeDays))) {
            throw BusinessValidationException(
                message = "El rango de fechas no puede ser mayor a $maxRangeDays días",
                detalles = listOf("Seleccione un período más corto para mejorar el rendimiento")
            )
        }

        if (endDate.isAfter(LocalDate.now())) {
            throw BusinessValidationException(
                message = "La fecha de fin no puede ser futura",
                detalles = listOf("Seleccione una fecha de fin válida")
            )
        }
    }

    private fun validateLimit(limit: Int) {
        if (limit <= 0 || limit > 100) {
            throw BusinessValidationException(
                message = "El límite debe estar entre 1 y 100",
                detalles = listOf("Proporcione un límite válido para la cantidad de productos")
            )
        }
    }

    private fun calculateGrowthTrend(dailySales: List<DailySalesResponse>): String {
        if (dailySales.size < 2) return "INSUFFICIENT_DATA"

        val recentSales = dailySales.takeLast(3).sumOf { it.revenue }
        val previousSales = dailySales.dropLast(3).takeLast(3).sumOf { it.revenue }

        return when {
            recentSales > previousSales -> "GROWING"
            recentSales < previousSales -> "DECLINING"
            else -> "STABLE"
        }
    }
}