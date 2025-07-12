package com.puce.chocorocks_backend.repositories
import com.puce.chocorocks_backend.models.entities.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SaleRepository : JpaRepository<Sale, Long> {

    fun existsBySaleNumber(saleNumber: String): Boolean

    fun findByClientId(clientId: Long): List<Sale>

    fun findByStoreId(storeId: Long): List<Sale>
}