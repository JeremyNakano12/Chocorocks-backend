package com.puce.chocorocks_backend.services

import com.puce.chocorocks_backend.dtos.responses.ReceiptResponse

interface EmailService {
    fun sendReceiptByEmail(receipt: ReceiptResponse, recipientEmail: String): Boolean
    fun sendReceiptByEmail(receipt: ReceiptResponse): Boolean
    fun validateEmailAddress(email: String): Boolean
}