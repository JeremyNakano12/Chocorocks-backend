package com.puce.chocorocks_backend.mappers

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*
import com.puce.chocorocks_backend.models.entities.*

object SaleDetailMapper {
    fun toEntity(
        request: SaleDetailRequest,
        sale: Sale,
        product: Product,
        batch: ProductBatch? = null
    ): SaleDetail {
        return SaleDetail(
            sale = sale,
            product = product,
            batch = batch,
            quantity = request.quantity,
            unitPrice = request.unitPrice,
            subtotal = request.subtotal
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
