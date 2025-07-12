package com.puce.chocorocks_backend.repositories
import com.puce.chocorocks_backend.models.entities.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ProductBatchRepository : JpaRepository<ProductBatch, Long> {

    fun existsByBatchCode(batchCode: String): Boolean

    @Query("SELECT pb FROM ProductBatch pb WHERE pb.product.id = :productId AND pb.expirationDate >= CURRENT_DATE AND pb.isActive = true")
    fun findActiveNonExpiredBatchesByProductId(@Param("productId") productId: Long): List<ProductBatch>

    @Query("SELECT pb FROM ProductBatch pb WHERE pb.product.id = :productId AND pb.currentQuantity > 0 AND pb.expirationDate >= CURRENT_DATE AND pb.isActive = true")
    fun findAvailableBatchesByProductId(@Param("productId") productId: Long): List<ProductBatch>
}