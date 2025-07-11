package com.puce.chocorocks_backend.mappers

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*
import com.puce.chocorocks_backend.models.entities.*
import java.math.BigDecimal

object SaleDetailMapper {
    fun toEntity(
        request: SaleDetailRequest,
        sale: Sale,
        product: Product,
        batch: ProductBatch? = null,
        unitPrice: BigDecimal,
        subtotal: BigDecimal
    ): SaleDetail {
        return SaleDetail(
            sale = sale,
            product = product,
            batch = batch,
            quantity = request.quantity,
            unitPrice = unitPrice,
            subtotal = subtotal
        )
    }

    fun toResponse(entity: SaleDetail): SaleDetailResponse {
        return SaleDetailResponse(
            id = entity.id,
            sale = SaleMapper.toResponse(entity.sale),
            product = ProductMapper.toResponse(entity.product),
            batch = entity.batch?.let { ProductBatchMapper.toResponse(it) },
            quantity = entity.quantity,
            unitPrice = entity.unitPrice,
            subtotal = entity.subtotal
        )
    }
}