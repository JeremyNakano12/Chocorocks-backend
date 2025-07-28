package com.puce.chocorocks_backend.repositories

import com.puce.chocorocks_backend.models.entities.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ReceiptRepository : JpaRepository<Receipt, Long> {

    fun existsByReceiptNumber(receiptNumber: String): Boolean

    fun findBySaleId(saleId: Long): Receipt?

    fun findByClientId(clientId: Long): List<Receipt>

    fun findByStoreId(storeId: Long): List<Receipt>

    fun findByUserId(userId: Long): List<Receipt>

    fun findByReceiptStatus(status: ReceiptStatus): List<Receipt>

    @Query("SELECT r FROM Receipt r WHERE r.issueDate BETWEEN :startDate AND :endDate")
    fun findByIssueDateBetween(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<Receipt>

    @Query("SELECT r FROM Receipt r WHERE r.store.id = :storeId AND r.issueDate BETWEEN :startDate AND :endDate")
    fun findByStoreAndDateRange(
        @Param("storeId") storeId: Long,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<Receipt>

    @Query("SELECT COUNT(r) FROM Receipt r WHERE r.receiptStatus = :status")
    fun countByReceiptStatus(@Param("status") status: ReceiptStatus): Long

    @Query("SELECT SUM(r.totalAmount) FROM Receipt r WHERE r.receiptStatus = 'ACTIVE' AND r.issueDate BETWEEN :startDate AND :endDate")
    fun getTotalSalesByDateRange(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): Double?
}