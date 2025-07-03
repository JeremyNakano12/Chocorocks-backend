package com.puce.chocorocks_backend.services.Impl

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*
import com.puce.chocorocks_backend.mappers.*
import com.puce.chocorocks_backend.repositories.*
import com.puce.chocorocks_backend.services.*
import com.puce.chocorocks_backend.models.entities.*
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SaleServiceImpl(
    private val saleRepository: SaleRepository,
    private val userRepository: UserRepository,
    private val clientRepository: ClientRepository,
    private val storeRepository: StoreRepository
) : SaleService {

    override fun findAll(): List<SaleResponse> =
        saleRepository.findAll().map { SaleMapper.toResponse(it) }

    override fun findById(id: Long): SaleResponse {
        val sale = saleRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Venta con ID $id no encontrada") }
        return SaleMapper.toResponse(sale)
    }

    override fun save(request: SaleRequest): SaleResponse {
        val user = userRepository.findById(request.userId)
            .orElseThrow { EntityNotFoundException("Usuario con ID ${request.userId} no encontrado") }

        val client = request.clientId?.let {
            clientRepository.findById(it)
                .orElseThrow { EntityNotFoundException("Cliente con ID $it no encontrado") }
        }

        val store = storeRepository.findById(request.storeId)
            .orElseThrow { EntityNotFoundException("Tienda con ID ${request.storeId} no encontrada") }

        val sale = SaleMapper.toEntity(request, user, client, store)
        val savedSale = saleRepository.save(sale)
        return SaleMapper.toResponse(savedSale)
    }

    override fun update(id: Long, request: SaleRequest): SaleResponse {
        val existingSale = saleRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Venta con ID $id no encontrada") }

        val user = userRepository.findById(request.userId)
            .orElseThrow { EntityNotFoundException("Usuario con ID ${request.userId} no encontrado") }

        val client = request.clientId?.let {
            clientRepository.findById(it)
                .orElseThrow { EntityNotFoundException("Cliente con ID $it no encontrado") }
        }

        val store = storeRepository.findById(request.storeId)
            .orElseThrow { EntityNotFoundException("Tienda con ID ${request.storeId} no encontrada") }

        val updatedSale = Sale(
            saleNumber = request.saleNumber,
            user = user,
            client = client,
            store = store,
            saleType = request.saleType,
            subtotal = request.subtotal,
            discountPercentage = request.discountPercentage,
            discountAmount = request.discountAmount,
            taxPercentage = request.taxPercentage,
            taxAmount = request.taxAmount,
            totalAmount = request.totalAmount,
            paymentMethod = request.paymentMethod,
            notes = request.notes,
            isInvoiced = request.isInvoiced
        ).apply { this.id = existingSale.id }

        val savedSale = saleRepository.save(updatedSale)
        return SaleMapper.toResponse(savedSale)
    }

    override fun delete(id: Long) {
        if (!saleRepository.existsById(id)) {
            throw EntityNotFoundException("Venta con ID $id no encontrada")
        }
        saleRepository.deleteById(id)
    }
}