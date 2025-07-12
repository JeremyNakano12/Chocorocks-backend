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
}
