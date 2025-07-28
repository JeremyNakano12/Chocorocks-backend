package com.puce.chocorocks_backend.controllers

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*
import com.puce.chocorocks_backend.services.*
import com.puce.chocorocks_backend.models.entities.ReceiptStatus
import com.puce.chocorocks_backend.exceptions.ResourceNotFoundException
import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import com.puce.chocorocks_backend.routes.Routes
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping(Routes.BASE_URL + Routes.RECEIPTS)
class ReceiptController(
    private val receiptService: ReceiptService,
    private val xmlGeneratorService: XMLGeneratorService,
    private val emailService: EmailService
) {

    @GetMapping
    fun getAllReceipts(): ResponseEntity<List<ReceiptResponse>> {
        val receipts = receiptService.findAll()
        return ResponseEntity.ok(receipts)
    }

    @GetMapping(Routes.ID)
    fun getReceiptById(@PathVariable id: Long): ResponseEntity<ReceiptResponse> {
        return try {
            val receipt = receiptService.findById(id)
            ResponseEntity.ok(receipt)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun createReceipt(@RequestBody request: ReceiptRequest): ResponseEntity<ReceiptResponse> {
        return try {
            val createdReceipt = receiptService.save(request)
            ResponseEntity.status(HttpStatus.CREATED).body(createdReceipt)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.badRequest().build()
        }
    }

    @PutMapping(Routes.ID)
    fun updateReceipt(
        @PathVariable id: Long,
        @RequestBody request: ReceiptRequest
    ): ResponseEntity<ReceiptResponse> {
        return try {
            val updatedReceipt = receiptService.update(id, request)
            ResponseEntity.ok(updatedReceipt)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping(Routes.ID)
    fun deleteReceipt(@PathVariable id: Long): ResponseEntity<Void> {
        return try {
            receiptService.delete(id)
            ResponseEntity.noContent().build()
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @PatchMapping("${Routes.ID}/cancel")
    fun cancelReceipt(@PathVariable id: Long): ResponseEntity<ReceiptResponse> {
        return try {
            val cancelledReceipt = receiptService.cancelReceipt(id)
            ResponseEntity.ok(cancelledReceipt)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @PatchMapping("${Routes.ID}/print")
    fun markAsPrinted(@PathVariable id: Long): ResponseEntity<ReceiptResponse> {
        return try {
            val printedReceipt = receiptService.markAsPrinted(id)
            ResponseEntity.ok(printedReceipt)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("${Routes.ID}/download-xml")
    fun downloadReceiptXml(@PathVariable id: Long): ResponseEntity<ByteArray> {
        return try {
            val receipt = receiptService.findById(id)
            val xmlBytes = xmlGeneratorService.generateReceiptXmlBytes(receipt)

            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_XML
            headers.setContentDispositionFormData("attachment", "recibo_${receipt.receiptNumber}.xml")
            headers.contentLength = xmlBytes.size.toLong()

            ResponseEntity.ok()
                .headers(headers)
                .body(xmlBytes)

        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("${Routes.ID}/view-xml")
    fun viewReceiptXml(@PathVariable id: Long): ResponseEntity<String> {
        return try {
            val receipt = receiptService.findById(id)
            val xmlContent = xmlGeneratorService.generateReceiptXml(receipt)

            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_XML

            ResponseEntity.ok()
                .headers(headers)
                .body(xmlContent)

        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("${Routes.ID}/send-email")
    fun sendReceiptByEmail(
        @PathVariable id: Long,
        @RequestBody request: EmailRequest
    ): ResponseEntity<Map<String, Any>> {
        return try {
            val receipt = receiptService.findById(id)

            val success = if (request.recipientEmail != null) {
                emailService.sendReceiptByEmail(receipt, request.recipientEmail)
            } else if (request.useClientEmail) {
                emailService.sendReceiptByEmail(receipt)
            } else {
                throw IllegalArgumentException("Debe especificar un email o usar el email del cliente")
            }

            val response: Map<String, Any> = mapOf(
                "success" to success,
                "message" to "Recibo enviado exitosamente por email",
                "receiptNumber" to receipt.receiptNumber,
                "emailSentTo" to (request.recipientEmail ?: receipt.client?.email ?: ""),
                "timestamp" to LocalDateTime.now().toString()
            )

            ResponseEntity.ok(response)

        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalArgumentException) {
            val errorResponse: Map<String, Any> = mapOf(
                "success" to false,
                "message" to (e.message ?: "Error en la solicitud"),
                "timestamp" to LocalDateTime.now().toString()
            )
            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    @PostMapping("${Routes.ID}/send-to-client")
    fun sendReceiptToClient(@PathVariable id: Long): ResponseEntity<Map<String, Any>> {
        return try {
            val receipt = receiptService.findById(id)
            val success = emailService.sendReceiptByEmail(receipt)

            val response: Map<String, Any> = mapOf(
                "success" to success,
                "message" to "Recibo enviado al cliente exitosamente",
                "receiptNumber" to receipt.receiptNumber,
                "clientEmail" to (receipt.client?.email ?: ""),
                "clientName" to (receipt.client?.nameLastname ?: ""),
                "timestamp" to LocalDateTime.now().toString()
            )

            ResponseEntity.ok(response)

        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/sale/{saleId}")
    fun getReceiptBySaleId(@PathVariable saleId: Long): ResponseEntity<ReceiptResponse> {
        val receipt = receiptService.findBySaleId(saleId)
        return if (receipt != null) {
            ResponseEntity.ok(receipt)
        } else {
            throw ResourceNotFoundException(
                resourceName = "Recibo",
                identifier = "venta ID $saleId",
                detalles = listOf(
                    "No existe un recibo asociado a esta venta",
                    "Verifique que la venta exista y tenga un recibo generado"
                )
            )
        }
    }

    @GetMapping("/store/{storeId}")
    fun getReceiptsByStore(
        @PathVariable storeId: Long,
        @RequestParam(required = false) startDate: String?,
        @RequestParam(required = false) endDate: String?
    ): ResponseEntity<List<ReceiptResponse>> {
        return try {
            val receipts = if (startDate != null && endDate != null) {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val start = LocalDateTime.parse("$startDate 00:00:00", formatter)
                val end = LocalDateTime.parse("$endDate 23:59:59", formatter)
                receiptService.findByStoreAndDateRange(storeId, start, end)
            } else {
                receiptService.findAll().filter { it.store.id == storeId }
            }
            ResponseEntity.ok(receipts)
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/status/{status}")
    fun getReceiptsByStatus(@PathVariable status: String): ResponseEntity<List<ReceiptResponse>> {
        return try {
            val receiptStatus = ReceiptStatus.valueOf(status.uppercase())
            val receipts = receiptService.getReceiptsByStatus(receiptStatus)
            ResponseEntity.ok(receipts)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/generate-number/{storeId}")
    fun generateReceiptNumber(@PathVariable storeId: Long): ResponseEntity<Map<String, String>> {
        return try {
            val receiptNumber = receiptService.generateReceiptNumber(storeId)
            ResponseEntity.ok(mapOf("receiptNumber" to receiptNumber))
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/auto-generate")
    fun createReceiptWithAutoNumber(@RequestBody request: ReceiptRequest): ResponseEntity<ReceiptResponse> {
        return try {
            val finalRequest = if (request.receiptNumber.isBlank()) {
                val autoNumber = receiptService.generateReceiptNumber(request.storeId)
                request.copy(receiptNumber = autoNumber)
            } else {
                request
            }

            val createdReceipt = receiptService.save(finalRequest)
            ResponseEntity.status(HttpStatus.CREATED).body(createdReceipt)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.badRequest().build()
        }
    }
}