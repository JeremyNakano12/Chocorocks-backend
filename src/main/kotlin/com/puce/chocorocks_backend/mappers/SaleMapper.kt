package com.puce.chocorocks_backend.mappers

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*
import com.puce.chocorocks_backend.models.entities.*

object SaleMapper {
    fun toEntity(
        request: SaleRequest,
        user: User,
        client: Client? = null,
        store: Store
    ): Sale {
        return Sale(
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
        )
    }

    fun toResponse(entity: Sale): SaleResponse {
        return SaleResponse(
            id = entity.id,
            saleNumber = entity.saleNumber,
            user = UserMapper.toResponse(entity.user),
            client = entity.client?.let { ClientMapper.toResponse(it) },
            store = StoreMapper.toResponse(entity.store),
            saleType = entity.saleType,
            subtotal = entity.subtotal,
            discountPercentage = entity.discountPercentage,
            discountAmount = entity.discountAmount,
            taxPercentage = entity.taxPercentage,
            taxAmount = entity.taxAmount,
            totalAmount = entity.totalAmount,
            paymentMethod = entity.paymentMethod,
            notes = entity.notes,
            isInvoiced = entity.isInvoiced
        )
    }
}
