package com.puce.chocorocks_backend.services

import com.puce.chocorocks_backend.dtos.responses.ReceiptResponse

interface XMLGeneratorService {
    fun generateReceiptXml(receipt: ReceiptResponse): String
    fun generateReceiptXmlBytes(receipt: ReceiptResponse): ByteArray
}