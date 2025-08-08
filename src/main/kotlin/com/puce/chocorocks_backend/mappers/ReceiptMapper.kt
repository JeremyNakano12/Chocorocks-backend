package com.puce.chocorocks_backend.mappers

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*
import com.puce.chocorocks_backend.models.entities.*

object ReceiptMapper {
    fun toEntity(
        request: ReceiptRequest,
        user: User,
        client: Client?,
        sale: Sale,
        store: Store
    ): Receipt {
        return Receipt(
            receiptNumber = request.receiptNumber,
            user = user,
            client = client,
            sale = sale,
            store = store,
            receiptStatus = request.receiptStatus,
            subtotal = sale.subtotal,
            discountAmount = sale.discountAmount,
            taxPercentage = sale.taxPercentage,
            taxAmount = sale.taxAmount,
            totalAmount = sale.totalAmount,
            paymentMethod = request.paymentMethod ?: sale.paymentMethod,
            additionalNotes = request.additionalNotes,
            customerName = request.customerName ?: client?.nameLastname,
            customerIdentification = request.customerIdentification ?: client?.identificationNumber,
            isPrinted = false,
            printCount = 0
        )
    }

    fun toResponse(entity: Receipt): ReceiptResponse {
        return ReceiptResponse(
            id = entity.id,
            receiptNumber = entity.receiptNumber,
            user = UserMapper.toResponse(entity.user),
            client = entity.client?.let { ClientMapper.toResponse(it) },
            sale = SaleMapper.toResponse(entity.sale),
            store = StoreMapper.toResponse(entity.store),
            issueDate = entity.issueDate,
            receiptStatus = entity.receiptStatus,
            subtotal = entity.subtotal,
            discountAmount = entity.discountAmount,
            taxPercentage = entity.taxPercentage,
            taxAmount = entity.taxAmount,
            totalAmount = entity.totalAmount,
            paymentMethod = entity.paymentMethod,
            additionalNotes = entity.additionalNotes,
            customerName = entity.customerName,
            customerIdentification = entity.customerIdentification,
            isPrinted = entity.isPrinted,
            printCount = entity.printCount,
            createdAt = entity.createdAt
        )
    }
}