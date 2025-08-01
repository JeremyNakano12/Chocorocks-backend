package com.puce.chocorocks_backend.repositories
import com.puce.chocorocks_backend.models.entities.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ProductStoreRepository : JpaRepository<ProductStore, Long> {

    fun existsByProductIdAndStoreId(productId: Long, storeId: Long): Boolean

    fun findByProductIdAndStoreId(productId: Long, storeId: Long): ProductStore?

    @Query("SELECT ps FROM ProductStore ps WHERE ps.store.id = :storeId AND ps.currentStock <= ps.minStockLevel")
    fun findLowStockProductsByStoreId(@Param("storeId") storeId: Long): List<ProductStore>

    fun findByStoreIdIn(storeIds: List<Long>): List<ProductStore>

    @Query("SELECT ps FROM ProductStore ps WHERE ps.currentStock <= ps.minStockLevel AND ps.currentStock > 0")
    fun findLowStockProducts(): List<ProductStore>

    @Query("SELECT ps FROM ProductStore ps WHERE ps.currentStock = 0")
    fun findOutOfStockProducts(): List<ProductStore>

    @Query("SELECT ps FROM ProductStore ps WHERE ps.currentStock > 0 AND ps.currentStock <= (ps.minStockLevel * 0.5)")
    fun findCriticalStockProducts(): List<ProductStore>

    @Query("SELECT SUM(ps.currentStock * ps.product.productionCost) FROM ProductStore ps")
    fun calculateTotalInventoryValue(): java.math.BigDecimal?

    @Query("SELECT SUM(ps.currentStock * ps.product.productionCost) FROM ProductStore ps WHERE ps.store.id IN :storeIds")
    fun calculateInventoryValueByStores(@Param("storeIds") storeIds: List<Long>): java.math.BigDecimal?

    @Query("SELECT COUNT(DISTINCT ps.product.id) FROM ProductStore ps WHERE ps.store.id = :storeId")
    fun countProductsByStore(@Param("storeId") storeId: Long): Long

    @Query("SELECT SUM(ps.currentStock) FROM ProductStore ps WHERE ps.store.id = :storeId")
    fun sumStockByStore(@Param("storeId") storeId: Long): Int?
}
