package com.puce.chocorocks_backend.services

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*
import com.puce.chocorocks_backend.models.entities.ReceiptStatus
import java.time.LocalDateTime

interface ReceiptService {
    fun findAll(): List<ReceiptResponse>
    fun findById(id: Long): ReceiptResponse
    fun save(request: ReceiptRequest): ReceiptResponse
    fun update(id: Long, request: ReceiptRequest): ReceiptResponse
    fun delete(id: Long)
    fun cancelReceipt(id: Long): ReceiptResponse
    fun markAsPrinted(id: Long): ReceiptResponse
    fun findBySaleId(saleId: Long): ReceiptResponse?
    fun findByStoreAndDateRange(storeId: Long, startDate: LocalDateTime, endDate: LocalDateTime): List<ReceiptResponse>
    fun getReceiptsByStatus(status: ReceiptStatus): List<ReceiptResponse>
    fun generateReceiptNumber(storeId: Long): String
}