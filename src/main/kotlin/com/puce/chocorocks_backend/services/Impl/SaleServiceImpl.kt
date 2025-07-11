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
import java.math.BigDecimal

@Service
@Transactional
class SaleServiceImpl(
    private val saleRepository: SaleRepository,
    private val userRepository: UserRepository,
    private val clientRepository: ClientRepository,
    private val storeRepository: StoreRepository,
    private val saleDetailRepository: SaleDetailRepository
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
            subtotal = existingSale.subtotal, // Mantener los totales calculados
            discountPercentage = request.discountPercentage,
            discountAmount = request.discountAmount,
            taxPercentage = request.taxPercentage,
            taxAmount = request.taxAmount,
            totalAmount = existingSale.totalAmount, // Mantener los totales calculados
            paymentMethod = request.paymentMethod,
            notes = request.notes,
            isInvoiced = request.isInvoiced
        ).apply { this.id = existingSale.id }

        val savedSale = saleRepository.save(updatedSale)

        recalculateSaleTotals(savedSale.id)

        val finalSale = saleRepository.findById(savedSale.id).get()
        return SaleMapper.toResponse(finalSale)
    }

    override fun delete(id: Long) {
        if (!saleRepository.existsById(id)) {
            throw EntityNotFoundException("Venta con ID $id no encontrada")
        }
        saleRepository.deleteById(id)
    }

    private fun recalculateSaleTotals(saleId: Long) {
        val sale = saleRepository.findById(saleId).orElse(null) ?: return

        val saleDetails = saleDetailRepository.findBySaleId(saleId)

        val subtotal = saleDetails.sumOf { it.subtotal }

        val totalWithDiscount = subtotal - sale.discountAmount

        val taxAmount = totalWithDiscount * sale.taxPercentage / BigDecimal(100)

        val totalAmount = totalWithDiscount + taxAmount

        val updatedSale = Sale(
            saleNumber = sale.saleNumber,
            user = sale.user,
            client = sale.client,
            store = sale.store,
            saleType = sale.saleType,
            subtotal = subtotal,
            discountPercentage = sale.discountPercentage,
            discountAmount = sale.discountAmount,
            taxPercentage = sale.taxPercentage,
            taxAmount = taxAmount,
            totalAmount = totalAmount,
            paymentMethod = sale.paymentMethod,
            notes = sale.notes,
            isInvoiced = sale.isInvoiced
        ).apply { this.id = sale.id }

        saleRepository.save(updatedSale)
    }
}