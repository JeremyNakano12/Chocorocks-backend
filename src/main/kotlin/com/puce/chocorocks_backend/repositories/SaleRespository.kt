package com.puce.chocorocks_backend.repositories
import com.puce.chocorocks_backend.models.entities.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface SaleRepository : JpaRepository<Sale, Long> {

    fun existsBySaleNumber(saleNumber: String): Boolean

    fun findByClientId(clientId: Long): List<Sale>

    fun findByStoreId(storeId: Long): List<Sale>

    fun findByCreatedAtBetween(startDate: LocalDateTime, endDate: LocalDateTime): List<Sale>

    fun findByCreatedAtBetweenAndStoreIdIn(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        storeIds: List<Long>
    ): List<Sale>

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.createdAt BETWEEN :startDate AND :endDate")
    fun countSalesByPeriod(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): Long

    @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE s.createdAt BETWEEN :startDate AND :endDate")
    fun sumRevenueByPeriod(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): java.math.BigDecimal?
}