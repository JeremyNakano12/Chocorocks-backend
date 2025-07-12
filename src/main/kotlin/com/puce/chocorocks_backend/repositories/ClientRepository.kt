package com.puce.chocorocks_backend.repositories
import com.puce.chocorocks_backend.models.entities.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ClientRepository : JpaRepository<Client, Long> {

    fun existsByIdentificationNumber(identificationNumber: String): Boolean

    fun existsByEmail(email: String): Boolean
}