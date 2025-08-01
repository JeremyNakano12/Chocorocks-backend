package com.puce.chocorocks_backend.services.Impl

import com.puce.chocorocks_backend.dtos.responses.*
import com.puce.chocorocks_backend.repositories.*
import com.puce.chocorocks_backend.services.*
import com.puce.chocorocks_backend.models.entities.*
import com.puce.chocorocks_backend.exceptions.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Service
@Transactional(readOnly = true)
class ReportServiceImpl(
    private val saleRepository: SaleRepository,
    private val saleDetailRepository: SaleDetailRepository,
    private val productRepository: ProductRepository,
    private val productStoreRepository: ProductStoreRepository,
    private val productBatchRepository: ProductBatchRepository,
    private val inventoryMovementRepository: InventoryMovementRepository,
    private val storeRepository: StoreRepository,
    private val categoryRepository: CategoryRepository
) : ReportService {

    // ================== REPORTE DE VENTAS ==================
    override fun getSalesReport(
        startDate: LocalDate,
        endDate: LocalDate,
        storeIds: List<Long>?
    ): SalesReportResponse {

        val sales = if (storeIds.isNullOrEmpty()) {
            saleRepository.findByCreatedAtBetween(startDate.atStartOfDay(), endDate.atTime(23, 59, 59))
        } else {
            saleRepository.findByCreatedAtBetweenAndStoreIdIn(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59),
                storeIds
            )
        }

        val totalSales = sales.size
        val totalRevenue = sales.sumOf { it.totalAmount }
        val averageTicket = if (totalSales > 0) totalRevenue.divide(BigDecimal(totalSales), 2, RoundingMode.HALF_UP) else BigDecimal.ZERO

        // Sales by type
        val retailSales = sales.filter { it.saleType == SaleType.RETAIL }
        val wholesaleSales = sales.filter { it.saleType == SaleType.WHOLESALE }

        val retailRevenue = retailSales.sumOf { it.totalAmount }
        val wholesaleRevenue = wholesaleSales.sumOf { it.totalAmount }

        val salesByType = SalesByTypeResponse(
            retail = SalesMetrics(
                count = retailSales.size,
                revenue = retailRevenue,
                percentage = if (totalRevenue > BigDecimal.ZERO) retailRevenue.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal(100)).toDouble() else 0.0
            ),
            wholesale = SalesMetrics(
                count = wholesaleSales.size,
                revenue = wholesaleRevenue,
                percentage = if (totalRevenue > BigDecimal.ZERO) wholesaleRevenue.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal(100)).toDouble() else 0.0
            )
        )

        // Sales by store
        val salesByStore = sales.groupBy { it.store.id }
            .map { (storeId, storeSales) ->
                val storeRevenue = storeSales.sumOf { it.totalAmount }
                SalesByStoreResponse(
                    storeId = storeId,
                    storeName = storeSales.first().store.name,
                    salesCount = storeSales.size,
                    revenue = storeRevenue,
                    percentage = if (totalRevenue > BigDecimal.ZERO) storeRevenue.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal(100)).toDouble() else 0.0
                )
            }

        // Top selling products
        val saleDetails = saleDetailRepository.findBySaleCreatedAtBetween(startDate.atStartOfDay(), endDate.atTime(23, 59, 59))
        val productSales = saleDetails.groupBy { it.product.id }
            .map { (productId, details) ->
                val product = details.first().product
                val quantitySold = details.sumOf { it.quantity }
                val revenue = details.sumOf { it.subtotal }

                TopSellingProductResponse(
                    productId = productId,
                    productName = product.nameProduct,
                    productCode = product.code,
                    quantitySold = quantitySold,
                    revenue = revenue,
                    rank = 0 // Se asignará después del ordenamiento
                )
            }
            .sortedByDescending { it.quantitySold }
            .take(10)
            .mapIndexed { index, product -> product.copy(rank = index + 1) }

        // Daily sales
        val dailySales = sales.groupBy { it.createdAt.toLocalDate() }
            .map { (date, dailySales) ->
                DailySalesResponse(
                    date = date,
                    salesCount = dailySales.size,
                    revenue = dailySales.sumOf { it.totalAmount }
                )
            }
            .sortedBy { it.date }

        val period = "${startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} - ${endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"

        return SalesReportResponse(
            period = period,
            totalSales = totalSales,
            totalRevenue = totalRevenue,
            averageTicket = averageTicket,
            salesByType = salesByType,
            salesByStore = salesByStore,
            topSellingProducts = productSales,
            dailySales = dailySales
        )
    }

    // ================== REPORTE DE INVENTARIO ==================
    override fun getInventoryReport(
        storeIds: List<Long>?,
        categoryIds: List<Long>?
    ): InventoryReportResponse {

        val productStores = if (storeIds.isNullOrEmpty()) {
            productStoreRepository.findAll()
        } else {
            productStoreRepository.findByStoreIdIn(storeIds)
        }

        val filteredProductStores = if (categoryIds.isNullOrEmpty()) {
            productStores
        } else {
            productStores.filter { it.product.category.id in categoryIds }
        }

        val totalProducts = filteredProductStores.map { it.product.id }.distinct().size
        val totalValue = filteredProductStores.sumOf {
            it.product.productionCost.multiply(BigDecimal(it.currentStock))
        }

        // Stock alerts
        val lowStockProducts = filteredProductStores.filter { it.currentStock <= it.minStockLevel && it.currentStock > 0 }
        val outOfStockProducts = filteredProductStores.filter { it.currentStock == 0 }
        val criticalStockProducts = filteredProductStores.filter { it.currentStock > 0 && it.currentStock <= (it.minStockLevel * 0.5) }

        // Expiring batches
        val expiringBatches = productBatchRepository.findByExpirationDateBetween(
            LocalDate.now(),
            LocalDate.now().plusDays(30)
        )

        val stockAlerts = InventoryAlertsResponse(
            lowStock = lowStockProducts.size,
            outOfStock = outOfStockProducts.size,
            critical = criticalStockProducts.size,
            expiringSoon = expiringBatches.size
        )

        // Inventory by store
        val inventoryByStore = filteredProductStores.groupBy { it.store.id }
            .map { (storeId, storeProducts) ->
                val store = storeProducts.first().store
                InventoryByStoreResponse(
                    storeId = storeId,
                    storeName = store.name,
                    productCount = storeProducts.map { it.product.id }.distinct().size,
                    totalStock = storeProducts.sumOf { it.currentStock },
                    totalValue = storeProducts.sumOf {
                        it.product.productionCost.multiply(BigDecimal(it.currentStock))
                    }
                )
            }

        // Inventory by category
        val inventoryByCategory = filteredProductStores.groupBy { it.product.category.id }
            .map { (categoryId, categoryProducts) ->
                val category = categoryProducts.first().product.category
                InventoryByCategoryResponse(
                    categoryId = categoryId,
                    categoryName = category.name,
                    productCount = categoryProducts.map { it.product.id }.distinct().size,
                    totalStock = categoryProducts.sumOf { it.currentStock },
                    totalValue = categoryProducts.sumOf {
                        it.product.productionCost.multiply(BigDecimal(it.currentStock))
                    }
                )
            }

        // Low stock products details
        val lowStockProductsDetails = lowStockProducts.map { productStore ->
            val alertLevel = when {
                productStore.currentStock == 0 -> "OUT_OF_STOCK"
                productStore.currentStock <= (productStore.minStockLevel * 0.5) -> "CRITICAL"
                else -> "LOW"
            }

            LowStockProductResponse(
                productId = productStore.product.id,
                productName = productStore.product.nameProduct,
                productCode = productStore.product.code,
                storeId = productStore.store.id,
                storeName = productStore.store.name,
                currentStock = productStore.currentStock,
                minStockLevel = productStore.minStockLevel,
                alertLevel = alertLevel
            )
        }

        // Expiring batches details
        val expiringBatchesDetails = expiringBatches.map { batch ->
            val daysUntilExpiration = ChronoUnit.DAYS.between(LocalDate.now(), batch.expirationDate).toInt()

            ExpiringBatchResponse(
                batchId = batch.id,
                batchCode = batch.batchCode,
                productId = batch.product.id,
                productName = batch.product.nameProduct,
                storeId = batch.store?.id,
                storeName = batch.store?.name,
                expirationDate = batch.expirationDate,
                daysUntilExpiration = daysUntilExpiration,
                currentQuantity = batch.currentQuantity
            )
        }

        return InventoryReportResponse(
            totalProducts = totalProducts,
            totalValue = totalValue,
            stockAlerts = stockAlerts,
            inventoryByStore = inventoryByStore,
            inventoryByCategory = inventoryByCategory,
            lowStockProducts = lowStockProductsDetails,
            expiringBatches = expiringBatchesDetails
        )
    }

    // ================== REPORTE DE RENTABILIDAD ==================
    override fun getProfitabilityReport(
        startDate: LocalDate,
        endDate: LocalDate,
        storeIds: List<Long>?,
        categoryIds: List<Long>?
    ): ProfitabilityReportResponse {

        val saleDetails = if (storeIds.isNullOrEmpty()) {
            saleDetailRepository.findBySaleCreatedAtBetween(startDate.atStartOfDay(), endDate.atTime(23, 59, 59))
        } else {
            saleDetailRepository.findBySaleCreatedAtBetweenAndSaleStoreIdIn(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59),
                storeIds
            )
        }

        val filteredSaleDetails = if (categoryIds.isNullOrEmpty()) {
            saleDetails
        } else {
            saleDetails.filter { it.product.category.id in categoryIds }
        }

        val totalRevenue = filteredSaleDetails.sumOf { it.subtotal }
        val totalCosts = filteredSaleDetails.sumOf {
            it.product.productionCost.multiply(BigDecimal(it.quantity))
        }
        val grossProfit = totalRevenue.subtract(totalCosts)
        val profitMargin = if (totalRevenue > BigDecimal.ZERO) {
            grossProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal(100)).toDouble()
        } else 0.0

        // Profit by product
        val profitByProduct = filteredSaleDetails.groupBy { it.product.id }
            .map { (productId, details) ->
                val product = details.first().product
                val quantitySold = details.sumOf { it.quantity }
                val revenue = details.sumOf { it.subtotal }
                val costs = details.sumOf {
                    it.product.productionCost.multiply(BigDecimal(it.quantity))
                }
                val profit = revenue.subtract(costs)
                val margin = if (revenue > BigDecimal.ZERO) {
                    profit.divide(revenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal(100)).toDouble()
                } else 0.0

                ProductProfitabilityResponse(
                    productId = productId,
                    productName = product.nameProduct,
                    productCode = product.code,
                    quantitySold = quantitySold,
                    revenue = revenue,
                    costs = costs,
                    profit = profit,
                    profitMargin = margin
                )
            }
            .sortedByDescending { it.profit }

        // Profit by category
        val profitByCategory = filteredSaleDetails.groupBy { it.product.category.id }
            .map { (categoryId, details) ->
                val category = details.first().product.category
                val revenue = details.sumOf { it.subtotal }
                val costs = details.sumOf {
                    it.product.productionCost.multiply(BigDecimal(it.quantity))
                }
                val profit = revenue.subtract(costs)
                val margin = if (revenue > BigDecimal.ZERO) {
                    profit.divide(revenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal(100)).toDouble()
                } else 0.0

                CategoryProfitabilityResponse(
                    categoryId = categoryId,
                    categoryName = category.name,
                    revenue = revenue,
                    costs = costs,
                    profit = profit,
                    profitMargin = margin
                )
            }
            .sortedByDescending { it.profit }

        // Profit by store
        val profitByStore = filteredSaleDetails.groupBy { it.sale.store.id }
            .map { (storeId, details) ->
                val store = details.first().sale.store
                val revenue = details.sumOf { it.subtotal }
                val costs = details.sumOf {
                    it.product.productionCost.multiply(BigDecimal(it.quantity))
                }
                val profit = revenue.subtract(costs)
                val margin = if (revenue > BigDecimal.ZERO) {
                    profit.divide(revenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal(100)).toDouble()
                } else 0.0

                StoreProfitabilityResponse(
                    storeId = storeId,
                    storeName = store.name,
                    revenue = revenue,
                    costs = costs,
                    profit = profit,
                    profitMargin = margin
                )
            }
            .sortedByDescending { it.profit }

        val period = "${startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} - ${endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"

        return ProfitabilityReportResponse(
            period = period,
            totalRevenue = totalRevenue,
            totalCosts = totalCosts,
            grossProfit = grossProfit,
            profitMargin = profitMargin,
            profitByProduct = profitByProduct,
            profitByCategory = profitByCategory,
            profitByStore = profitByStore
        )
    }

    // ================== REPORTE DE PRODUCTOS MÁS VENDIDOS ==================
    override fun getBestSellingProductsReport(
        startDate: LocalDate,
        endDate: LocalDate,
        limit: Int,
        storeIds: List<Long>?,
        categoryIds: List<Long>?
    ): BestSellingProductsReportResponse {

        val saleDetails = if (storeIds.isNullOrEmpty()) {
            saleDetailRepository.findBySaleCreatedAtBetween(startDate.atStartOfDay(), endDate.atTime(23, 59, 59))
        } else {
            saleDetailRepository.findBySaleCreatedAtBetweenAndSaleStoreIdIn(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59),
                storeIds
            )
        }

        val filteredSaleDetails = if (categoryIds.isNullOrEmpty()) {
            saleDetails
        } else {
            saleDetails.filter { it.product.category.id in categoryIds }
        }

        val totalProductsSold = filteredSaleDetails.sumOf { it.quantity }

        val products = filteredSaleDetails.groupBy { it.product.id }
            .map { (productId, details) ->
                val product = details.first().product
                val quantitySold = details.sumOf { it.quantity }
                val revenue = details.sumOf { it.subtotal }
                val salesCount = details.map { it.sale.id }.distinct().size
                val averagePrice = if (quantitySold > 0) {
                    revenue.divide(BigDecimal(quantitySold), 2, RoundingMode.HALF_UP)
                } else BigDecimal.ZERO
                val marketShare = if (totalProductsSold > 0) {
                    (quantitySold.toDouble() / totalProductsSold.toDouble()) * 100
                } else 0.0

                BestSellingProductResponse(
                    rank = 0, // Se asignará después
                    productId = productId,
                    productName = product.nameProduct,
                    productCode = product.code,
                    categoryName = product.category.name,
                    quantitySold = quantitySold,
                    revenue = revenue,
                    averagePrice = averagePrice,
                    salesCount = salesCount,
                    marketShare = marketShare
                )
            }
            .sortedByDescending { it.quantitySold }
            .take(limit)
            .mapIndexed { index, product -> product.copy(rank = index + 1) }

        val period = "${startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} - ${endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"

        return BestSellingProductsReportResponse(
            period = period,
            totalProductsSold = totalProductsSold,
            products = products
        )
    }

    // ================== REPORTE DE TRAZABILIDAD ==================
    override fun getTraceabilityReport(batchCode: String): TraceabilityReportResponse {
        val batch = productBatchRepository.findByBatchCode(batchCode)
            ?: throw ResourceNotFoundException(
                resourceName = "Lote",
                identifier = batchCode,
                detalles = listOf("Verifique que el código del lote sea correcto")
            )

        // Movements del lote
        val movements = inventoryMovementRepository.findByBatchIdOrderByCreatedAtDesc(batch.id)
            .map { movement ->
                BatchMovementResponse(
                    movementId = movement.id,
                    movementType = movement.movementType.name,
                    fromStore = movement.fromStore?.name,
                    toStore = movement.toStore?.name,
                    quantity = movement.quantity,
                    reason = movement.reason.name,
                    movementDate = movement.movementDate.toLocalDate(),
                    userEmail = movement.user.email
                )
            }

        // Sales del lote
        val sales = saleDetailRepository.findByBatchId(batch.id)
            .map { saleDetail ->
                BatchSaleResponse(
                    saleId = saleDetail.sale.id,
                    saleNumber = saleDetail.sale.saleNumber,
                    quantity = saleDetail.quantity,
                    unitPrice = saleDetail.unitPrice,
                    subtotal = saleDetail.subtotal,
                    saleDate = saleDetail.sale.createdAt.toLocalDate(),
                    storeName = saleDetail.sale.store.name,
                    clientName = saleDetail.sale.client?.nameLastname
                )
            }

        return TraceabilityReportResponse(
            batchCode = batch.batchCode,
            productId = batch.product.id,
            productName = batch.product.nameProduct,
            productionDate = batch.productionDate,
            expirationDate = batch.expirationDate,
            initialQuantity = batch.initialQuantity,
            currentQuantity = batch.currentQuantity,
            movements = movements,
            sales = sales
        )
    }

    override fun getTraceabilityReportByProduct(
        productId: Long,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): List<TraceabilityReportResponse> {
        val product = productRepository.findById(productId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Producto",
                    identifier = productId,
                    detalles = listOf("Verifique que el ID del producto sea correcto")
                )
            }

        val batches = if (startDate != null && endDate != null) {
            productBatchRepository.findByProductIdAndProductionDateBetween(productId, startDate, endDate)
        } else {
            productBatchRepository.findByProductId(productId)
        }

        return batches.map { batch ->
            getTraceabilityReport(batch.batchCode)
        }
    }

    // ================== DASHBOARD EJECUTIVO ==================
    override fun getExecutiveDashboard(
        startDate: LocalDate,
        endDate: LocalDate
    ): ExecutiveDashboardResponse {

        val sales = saleRepository.findByCreatedAtBetween(startDate.atStartOfDay(), endDate.atTime(23, 59, 59))
        val totalRevenue = sales.sumOf { it.totalAmount }
        val totalSales = sales.size
        val totalProducts = productRepository.count().toInt()
        val activeStores = storeRepository.count().toInt()

        val summary = DashboardSummaryResponse(
            totalRevenue = totalRevenue,
            totalSales = totalSales,
            totalProducts = totalProducts,
            activeStores = activeStores,
            period = "${startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} - ${endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
        )

        // KPIs básicos
        val averageTicket = if (totalSales > 0) totalRevenue.divide(BigDecimal(totalSales), 2, RoundingMode.HALF_UP) else BigDecimal.ZERO
        val saleDetails = saleDetailRepository.findBySaleCreatedAtBetween(startDate.atStartOfDay(), endDate.atTime(23, 59, 59))
        val totalCosts = saleDetails.sumOf { it.product.productionCost.multiply(BigDecimal(it.quantity)) }
        val profitMargin = if (totalRevenue > BigDecimal.ZERO) {
            totalRevenue.subtract(totalCosts).divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal(100)).toDouble()
        } else 0.0

        val kpis = DashboardKPIsResponse(
            averageTicket = averageTicket,
            conversionRate = 0.0, // Requiere datos adicionales de tráfico
            inventoryTurnover = 0.0, // Requiere cálculo más complejo
            profitMargin = profitMargin,
            customerRetention = 0.0 // Requiere análisis histórico de clientes
        )

        // Trends (simplificado para 7 días)
        val salesTrend = (0..6).map { days ->
            val date = endDate.minusDays(days.toLong())
            val dailySales = sales.filter { it.createdAt.toLocalDate() == date }
            TrendDataPoint(
                date = date,
                value = BigDecimal(dailySales.size)
            )
        }.reversed()

        val revenueTrend = (0..6).map { days ->
            val date = endDate.minusDays(days.toLong())
            val dailySales = sales.filter { it.createdAt.toLocalDate() == date }
            TrendDataPoint(
                date = date,
                value = dailySales.sumOf { it.totalAmount }
            )
        }.reversed()

        val trends = DashboardTrendsResponse(
            salesTrend = salesTrend,
            revenueTrend = revenueTrend,
            inventoryTrend = emptyList() // Requiere datos de inventario histórico
        )

        // Alerts
        val lowStockCount = productStoreRepository.findLowStockProducts().size
        val expiringBatchesCount = productBatchRepository.findByExpirationDateBetween(
            LocalDate.now(),
            LocalDate.now().plusDays(7)
        ).size

        val alerts = DashboardAlertsResponse(
            lowStockCount = lowStockCount,
            expiringBatchesCount = expiringBatchesCount,
            pendingReceiptsCount = 0, // Requiere repository de receipts
            systemAlerts = emptyList()
        )

        return ExecutiveDashboardResponse(
            summary = summary,
            kpis = kpis,
            trends = trends,
            alerts = alerts
        )
    }

    // ================== MÉTODOS AUXILIARES ==================
    override fun getAvailablePeriods(): List<String> {
        return listOf(
            "Últimos 7 días",
            "Últimos 30 días",
            "Últimos 3 meses",
            "Último año",
            "Personalizado"
        )
    }

    override fun getReportFilters(): Map<String, Any> {
        val stores = storeRepository.findAll().map {
            mapOf("id" to it.id, "name" to it.name)
        }
        val categories = categoryRepository.findAll().map {
            mapOf("id" to it.id, "name" to it.name)
        }

        return mapOf(
            "stores" to stores,
            "categories" to categories,
            "periods" to getAvailablePeriods()
        )
    }
}