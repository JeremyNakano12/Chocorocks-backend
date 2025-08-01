package com.puce.chocorocks_backend.dtos.responses

import java.math.BigDecimal
import java.time.LocalDate

// ================== REPORTE DE VENTAS ==================
data class SalesReportResponse(
    val period: String,
    val totalSales: Int,
    val totalRevenue: BigDecimal,
    val averageTicket: BigDecimal,
    val salesByType: SalesByTypeResponse,
    val salesByStore: List<SalesByStoreResponse>,
    val topSellingProducts: List<TopSellingProductResponse>,
    val dailySales: List<DailySalesResponse>
)

data class SalesByTypeResponse(
    val retail: SalesMetrics,
    val wholesale: SalesMetrics
)

data class SalesMetrics(
    val count: Int,
    val revenue: BigDecimal,
    val percentage: Double
)

data class SalesByStoreResponse(
    val storeId: Long,
    val storeName: String,
    val salesCount: Int,
    val revenue: BigDecimal,
    val percentage: Double
)

data class TopSellingProductResponse(
    val productId: Long,
    val productName: String,
    val productCode: String,
    val quantitySold: Int,
    val revenue: BigDecimal,
    val rank: Int
)

data class DailySalesResponse(
    val date: LocalDate,
    val salesCount: Int,
    val revenue: BigDecimal
)

// ================== REPORTE DE INVENTARIO ==================
data class InventoryReportResponse(
    val totalProducts: Int,
    val totalValue: BigDecimal,
    val stockAlerts: InventoryAlertsResponse,
    val inventoryByStore: List<InventoryByStoreResponse>,
    val inventoryByCategory: List<InventoryByCategoryResponse>,
    val lowStockProducts: List<LowStockProductResponse>,
    val expiringBatches: List<ExpiringBatchResponse>
)

data class InventoryAlertsResponse(
    val lowStock: Int,
    val outOfStock: Int,
    val critical: Int,
    val expiringSoon: Int
)

data class InventoryByStoreResponse(
    val storeId: Long,
    val storeName: String,
    val productCount: Int,
    val totalStock: Int,
    val totalValue: BigDecimal
)

data class InventoryByCategoryResponse(
    val categoryId: Long,
    val categoryName: String,
    val productCount: Int,
    val totalStock: Int,
    val totalValue: BigDecimal
)

data class LowStockProductResponse(
    val productId: Long,
    val productName: String,
    val productCode: String,
    val storeId: Long,
    val storeName: String,
    val currentStock: Int,
    val minStockLevel: Int,
    val alertLevel: String // LOW, CRITICAL, OUT_OF_STOCK
)

data class ExpiringBatchResponse(
    val batchId: Long,
    val batchCode: String,
    val productId: Long,
    val productName: String,
    val storeId: Long?,
    val storeName: String?,
    val expirationDate: LocalDate,
    val daysUntilExpiration: Int,
    val currentQuantity: Int
)

// ================== REPORTE DE RENTABILIDAD ==================
data class ProfitabilityReportResponse(
    val period: String,
    val totalRevenue: BigDecimal,
    val totalCosts: BigDecimal,
    val grossProfit: BigDecimal,
    val profitMargin: Double,
    val profitByProduct: List<ProductProfitabilityResponse>,
    val profitByCategory: List<CategoryProfitabilityResponse>,
    val profitByStore: List<StoreProfitabilityResponse>
)

data class ProductProfitabilityResponse(
    val productId: Long,
    val productName: String,
    val productCode: String,
    val quantitySold: Int,
    val revenue: BigDecimal,
    val costs: BigDecimal,
    val profit: BigDecimal,
    val profitMargin: Double
)

data class CategoryProfitabilityResponse(
    val categoryId: Long,
    val categoryName: String,
    val revenue: BigDecimal,
    val costs: BigDecimal,
    val profit: BigDecimal,
    val profitMargin: Double
)

data class StoreProfitabilityResponse(
    val storeId: Long,
    val storeName: String,
    val revenue: BigDecimal,
    val costs: BigDecimal,
    val profit: BigDecimal,
    val profitMargin: Double
)

// ================== REPORTE DE PRODUCTOS MÁS VENDIDOS ==================
data class BestSellingProductsReportResponse(
    val period: String,
    val totalProductsSold: Int,
    val products: List<BestSellingProductResponse>
)

data class BestSellingProductResponse(
    val rank: Int,
    val productId: Long,
    val productName: String,
    val productCode: String,
    val categoryName: String,
    val quantitySold: Int,
    val revenue: BigDecimal,
    val averagePrice: BigDecimal,
    val salesCount: Int,
    val marketShare: Double // Porcentaje del total de ventas
)

// ================== REPORTE DE TRAZABILIDAD ==================
data class TraceabilityReportResponse(
    val batchCode: String,
    val productId: Long,
    val productName: String,
    val productionDate: LocalDate,
    val expirationDate: LocalDate,
    val initialQuantity: Int,
    val currentQuantity: Int,
    val movements: List<BatchMovementResponse>,
    val sales: List<BatchSaleResponse>
)

data class BatchMovementResponse(
    val movementId: Long,
    val movementType: String,
    val fromStore: String?,
    val toStore: String?,
    val quantity: Int,
    val reason: String,
    val movementDate: LocalDate,
    val userEmail: String
)

data class BatchSaleResponse(
    val saleId: Long,
    val saleNumber: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val subtotal: BigDecimal,
    val saleDate: LocalDate,
    val storeName: String,
    val clientName: String?
)

// ================== DASHBOARD EJECUTIVO ==================
data class ExecutiveDashboardResponse(
    val summary: DashboardSummaryResponse,
    val kpis: DashboardKPIsResponse,
    val trends: DashboardTrendsResponse,
    val alerts: DashboardAlertsResponse
)

data class DashboardSummaryResponse(
    val totalRevenue: BigDecimal,
    val totalSales: Int,
    val totalProducts: Int,
    val activeStores: Int,
    val period: String
)

data class DashboardKPIsResponse(
    val averageTicket: BigDecimal,
    val conversionRate: Double,
    val inventoryTurnover: Double,
    val profitMargin: Double,
    val customerRetention: Double
)

data class DashboardTrendsResponse(
    val salesTrend: List<TrendDataPoint>,
    val revenueTrend: List<TrendDataPoint>,
    val inventoryTrend: List<TrendDataPoint>
)

data class TrendDataPoint(
    val date: LocalDate,
    val value: BigDecimal
)

data class DashboardAlertsResponse(
    val lowStockCount: Int,
    val expiringBatchesCount: Int,
    val pendingReceiptsCount: Int,
    val systemAlerts: List<String>
)

// ================== REQUEST COMÚN PARA FILTROS ==================
data class ReportFiltersRequest(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val storeIds: List<Long>? = null,
    val categoryIds: List<Long>? = null,
    val productIds: List<Long>? = null
)