package com.puce.chocorocks_backend.services

import com.puce.chocorocks_backend.dtos.responses.*
import java.time.LocalDate

interface ReportService {

    // ================== REPORTE DE VENTAS ==================
    fun getSalesReport(
        startDate: LocalDate,
        endDate: LocalDate,
        storeIds: List<Long>? = null
    ): SalesReportResponse

    // ================== REPORTE DE INVENTARIO ==================
    fun getInventoryReport(
        storeIds: List<Long>? = null,
        categoryIds: List<Long>? = null
    ): InventoryReportResponse

    // ================== REPORTE DE RENTABILIDAD ==================
    fun getProfitabilityReport(
        startDate: LocalDate,
        endDate: LocalDate,
        storeIds: List<Long>? = null,
        categoryIds: List<Long>? = null
    ): ProfitabilityReportResponse

    // ================== REPORTE DE PRODUCTOS MÁS VENDIDOS ==================
    fun getBestSellingProductsReport(
        startDate: LocalDate,
        endDate: LocalDate,
        limit: Int = 20,
        storeIds: List<Long>? = null,
        categoryIds: List<Long>? = null
    ): BestSellingProductsReportResponse

    // ================== REPORTE DE TRAZABILIDAD ==================
    fun getTraceabilityReport(
        batchCode: String
    ): TraceabilityReportResponse

    fun getTraceabilityReportByProduct(
        productId: Long,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): List<TraceabilityReportResponse>

    // ================== DASHBOARD EJECUTIVO ==================
    fun getExecutiveDashboard(
        startDate: LocalDate,
        endDate: LocalDate
    ): ExecutiveDashboardResponse

    // ================== MÉTODOS AUXILIARES ==================
    fun getAvailablePeriods(): List<String>

    fun getReportFilters(): Map<String, Any>
}