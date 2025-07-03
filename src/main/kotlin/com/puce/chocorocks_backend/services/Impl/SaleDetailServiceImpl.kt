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

        val saleDetail = SaleDetailMapper.toEntity(request, sale, product, batch)
        val savedSaleDetail = saleDetailRepository.save(saleDetail)
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

        val updatedSaleDetail = SaleDetail(
            sale = sale,
            product = product,
            batch = batch,
            quantity = request.quantity,
            unitPrice = request.unitPrice,
            subtotal = request.subtotal
        ).apply { this.id = existingSaleDetail.id }

        val savedSaleDetail = saleDetailRepository.save(updatedSaleDetail)
        return SaleDetailMapper.toResponse(savedSaleDetail)
    }

    override fun delete(id: Long) {
        if (!saleDetailRepository.existsById(id)) {
            throw EntityNotFoundException("Detalle de venta con ID $id no encontrado")
        }
        saleDetailRepository.deleteById(id)
    }
}