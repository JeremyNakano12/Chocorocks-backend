package com.puce.chocorocks_backend.repositories
import com.puce.chocorocks_backend.models.entities.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface SaleDetailRepository : JpaRepository<SaleDetail, Long> {

    @Query("SELECT sd FROM SaleDetail sd WHERE sd.sale.id = :saleId")
    fun findBySaleId(@Param("saleId") saleId: Long): List<SaleDetail>
}