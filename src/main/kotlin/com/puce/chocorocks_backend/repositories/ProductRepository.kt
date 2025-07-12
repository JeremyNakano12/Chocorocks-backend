package com.puce.chocorocks_backend.repositories
import com.puce.chocorocks_backend.models.entities.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : JpaRepository<Product, Long> {

    fun existsByCode(code: String): Boolean

    fun existsByBarcode(barcode: String): Boolean

    fun findByCode(code: String): Product?
}