package com.puce.chocorocks_backend.repositories
import com.puce.chocorocks_backend.models.entities.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StoreRepository : JpaRepository<Store, Long> {

    fun existsByName(name: String): Boolean

    fun findByIsActiveTrue(): List<Store>
}