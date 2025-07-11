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
class SaleDetailServiceImpl(
    private val saleDetailRepository: SaleDetailRepository,
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository,
    private val productBatchRepository: ProductBatchRepository
) : SaleDetailService {

    override fun findAll(): List<SaleDetailResponse> =
        saleDetailRepository.findAll().map { SaleDetailMapper.toResponse(it) }

    override fun findById(id: Long): SaleDetailResponse {
        val saleDetail = saleDetailRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Detalle de venta con ID $id no encontrado") }
        return SaleDetailMapper.toResponse(saleDetail)
    }

    override fun save(request: SaleDetailRequest): SaleDetailResponse {
        val sale = saleRepository.findById(request.saleId)
            .orElseThrow { EntityNotFoundException("Venta con ID ${request.saleId} no encontrada") }

        val product = productRepository.findById(request.productId)
            .orElseThrow { EntityNotFoundException("Producto con ID ${request.productId} no encontrado") }

        val batch = request.batchId?.let {
            productBatchRepository.findById(it)
                .orElseThrow { EntityNotFoundException("Lote con ID $it no encontrado") }
        }

        val unitPrice = when (sale.saleType) {
            SaleType.RETAIL -> product.retailPrice
            SaleType.WHOLESALE -> product.wholesalePrice
        }

        val subtotal = unitPrice * BigDecimal(request.quantity)

        val saleDetail = SaleDetailMapper.toEntity(request, sale, product, batch, unitPrice, subtotal)
        val savedSaleDetail = saleDetailRepository.save(saleDetail)

        recalculateSaleTotals(sale.id)

        return SaleDetailMapper.toResponse(savedSaleDetail)
    }

    override fun update(id: Long, request: SaleDetailRequest): SaleDetailResponse {
        val existingSaleDetail = saleDetailRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Detalle de venta con ID $id no encontrado") }

        val sale = saleRepository.findById(request.saleId)
            .orElseThrow { EntityNotFoundException("Venta con ID ${request.saleId} no encontrada") }

        val product = productRepository.findById(request.productId)
            .orElseThrow { EntityNotFoundException("Producto con ID ${request.productId} no encontrado") }

        val batch = request.batchId?.let {
            productBatchRepository.findById(it)
                .orElseThrow { EntityNotFoundException("Lote con ID $it no encontrado") }
        }

        val unitPrice = when (sale.saleType) {
            SaleType.RETAIL -> product.retailPrice
            SaleType.WHOLESALE -> product.wholesalePrice
        }

        val subtotal = unitPrice * BigDecimal(request.quantity)

        val updatedSaleDetail = SaleDetail(
            sale = sale,
            product = product,
            batch = batch,
            quantity = request.quantity,
            unitPrice = unitPrice,
            subtotal = subtotal
        ).apply { this.id = existingSaleDetail.id }

        val savedSaleDetail = saleDetailRepository.save(updatedSaleDetail)

        recalculateSaleTotals(sale.id)

        return SaleDetailMapper.toResponse(savedSaleDetail)
    }

    override fun delete(id: Long) {
        val saleDetail = saleDetailRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Detalle de venta con ID $id no encontrado") }

        val saleId = saleDetail.sale.id

        saleDetailRepository.deleteById(id)

        recalculateSaleTotals(saleId)
    }

    // Método interno para recalcular totales de la venta
    private fun recalculateSaleTotals(saleId: Long) {
        val sale = saleRepository.findById(saleId).orElse(null) ?: return

        // Obtener todos los detalles de la venta
        val saleDetails = saleDetailRepository.findBySaleId(saleId)

        // Calcular subtotal (suma de todos los subtotales de los detalles)
        val subtotal = saleDetails.sumOf { it.subtotal }

        // Calcular total con descuento
        val totalWithDiscount = subtotal - sale.discountAmount

        // Calcular impuestos sobre el total con descuento
        val taxAmount = totalWithDiscount * sale.taxPercentage / BigDecimal(100)

        // Calcular total final
        val totalAmount = totalWithDiscount + taxAmount

        // Actualizar la venta
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