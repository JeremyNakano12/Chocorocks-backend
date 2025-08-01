package com.puce.chocorocks_backend.repositories
import com.puce.chocorocks_backend.models.entities.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface SaleDetailRepository : JpaRepository<SaleDetail, Long> {

    @Query("SELECT sd FROM SaleDetail sd WHERE sd.sale.id = :saleId")
    fun findBySaleId(@Param("saleId") saleId: Long): List<SaleDetail>

    fun deleteBySaleId(saleId: Long)

    fun findBySaleCreatedAtBetween(startDate: LocalDateTime, endDate: LocalDateTime): List<SaleDetail>

    fun findBySaleCreatedAtBetweenAndSaleStoreIdIn(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        storeIds: List<Long>
    ): List<SaleDetail>

    fun findByBatchId(batchId: Long): List<SaleDetail>

    @Query("SELECT sd FROM SaleDetail sd WHERE sd.product.id = :productId AND sd.sale.createdAt BETWEEN :startDate AND :endDate")
    fun findByProductIdAndSaleDateBetween(
        @Param("productId") productId: Long,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<SaleDetail>

    @Query("SELECT SUM(sd.quantity) FROM SaleDetail sd WHERE sd.product.id = :productId AND sd.sale.createdAt BETWEEN :startDate AND :endDate")
    fun sumQuantityByProductAndPeriod(
        @Param("productId") productId: Long,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): Int?
}