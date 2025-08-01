package com.puce.chocorocks_backend.repositories
import com.puce.chocorocks_backend.models.entities.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime

@Repository
interface InventoryMovementRepository : JpaRepository<InventoryMovement, Long> {

    fun findByProductId(productId: Long): List<InventoryMovement>

    fun findByBatchId(batchId: Long): List<InventoryMovement>

    fun findByBatchIdOrderByCreatedAtDesc(batchId: Long): List<InventoryMovement>

    fun findByFromStoreId(storeId: Long): List<InventoryMovement>

    fun findByToStoreId(storeId: Long): List<InventoryMovement>

    fun findByMovementDateBetween(startDate: LocalDate, endDate: LocalDate): List<InventoryMovement>

    fun findByCreatedAtBetweenOrderByCreatedAtDesc(startDate: LocalDateTime, endDate: LocalDateTime): List<InventoryMovement>

    @Query("SELECT im FROM InventoryMovement im WHERE im.movementType = :movementType AND im.movementDate BETWEEN :startDate AND :endDate")
    fun findByMovementTypeAndDateBetween(
        @Param("movementType") movementType: MovementType,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<InventoryMovement>

    @Query("SELECT im FROM InventoryMovement im WHERE im.reason = :reason AND im.movementDate BETWEEN :startDate AND :endDate")
    fun findByReasonAndDateBetween(
        @Param("reason") reason: MovementReason,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<InventoryMovement>
}