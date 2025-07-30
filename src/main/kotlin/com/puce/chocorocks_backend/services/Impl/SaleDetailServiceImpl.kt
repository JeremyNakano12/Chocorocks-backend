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
import com.puce.chocorocks_backend.exceptions.*
import com.puce.chocorocks_backend.utils.*

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
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Detalle de venta",
                    identifier = id,
                    detalles = listOf("Verifique que el ID del detalle sea correcto")
                )
            }
        return SaleDetailMapper.toResponse(saleDetail)
    }

    override fun save(request: SaleDetailRequest): SaleDetailResponse {
        ValidationUtils.validatePositiveQuantity(request.quantity, "cantidad")

        val sale = saleRepository.findById(request.saleId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Venta",
                    identifier = request.saleId,
                    detalles = listOf("Seleccione una venta válida")
                )
            }

        val product = productRepository.findById(request.productId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Producto",
                    identifier = request.productId,
                    detalles = listOf("Seleccione un producto válido")
                )
            }

        val batch = request.batchId?.let {
            val productBatch = productBatchRepository.findById(it)
                .orElseThrow {
                    ResourceNotFoundException(
                        resourceName = "Lote",
                        identifier = it,
                        detalles = listOf("Seleccione un lote válido")
                    )
                }

            ValidationUtils.validateBatchNotExpired(productBatch.expirationDate, productBatch.batchCode)

            ValidationUtils.validateSufficientStock(
                available = productBatch.currentQuantity,
                requested = request.quantity,
                productName = product.nameProduct
            )

            productBatch
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
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Detalle de venta",
                    identifier = id,
                    detalles = listOf("No se puede actualizar un detalle que no existe")
                )
            }

        ValidationUtils.validatePositiveQuantity(request.quantity, "cantidad")

        val sale = saleRepository.findById(request.saleId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Venta",
                    identifier = request.saleId,
                    detalles = listOf("Seleccione una venta válida")
                )
            }

        val product = productRepository.findById(request.productId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Producto",
                    identifier = request.productId,
                    detalles = listOf("Seleccione un producto válido")
                )
            }

        val batch = request.batchId?.let {
            val productBatch = productBatchRepository.findById(it)
                .orElseThrow {
                    ResourceNotFoundException(
                        resourceName = "Lote",
                        identifier = it,
                        detalles = listOf("Seleccione un lote válido")
                    )
                }

            ValidationUtils.validateBatchNotExpired(productBatch.expirationDate, productBatch.batchCode)
            ValidationUtils.validateSufficientStock(
                available = productBatch.currentQuantity,
                requested = request.quantity,
                productName = product.nameProduct
            )

            productBatch
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
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Detalle de venta",
                    identifier = id,
                    detalles = listOf("No se puede eliminar un detalle que no existe")
                )
            }

        val saleId = saleDetail.sale.id
        saleDetailRepository.deleteById(id)
        recalculateSaleTotals(saleId)
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