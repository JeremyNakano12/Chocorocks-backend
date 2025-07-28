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
import com.puce.chocorocks_backend.exceptions.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
@Transactional
class ReceiptServiceImpl(
    private val receiptRepository: ReceiptRepository,
    private val userRepository: UserRepository,
    private val clientRepository: ClientRepository,
    private val saleRepository: SaleRepository,
    private val storeRepository: StoreRepository
) : ReceiptService {

    override fun findAll(): List<ReceiptResponse> =
        receiptRepository.findAll().map { ReceiptMapper.toResponse(it) }

    override fun findById(id: Long): ReceiptResponse {
        val receipt = receiptRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Recibo",
                    identifier = id,
                    detalles = listOf("Verifique que el ID del recibo sea correcto")
                )
            }
        return ReceiptMapper.toResponse(receipt)
    }

    override fun save(request: ReceiptRequest): ReceiptResponse {
        validateReceiptData(request)

        val receiptNumberExists = receiptRepository.existsByReceiptNumber(request.receiptNumber)
        if (receiptNumberExists) {
            throw DuplicateResourceException(
                resourceName = "Recibo",
                field = "número",
                value = request.receiptNumber,
                detalles = listOf("El número de recibo debe ser único")
            )
        }

        val user = userRepository.findById(request.userId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Usuario",
                    identifier = request.userId,
                    detalles = listOf("Seleccione un usuario válido")
                )
            }

        val client = request.clientId?.let {
            clientRepository.findById(it)
                .orElseThrow {
                    ResourceNotFoundException(
                        resourceName = "Cliente",
                        identifier = it,
                        detalles = listOf("Seleccione un cliente válido")
                    )
                }
        }

        val sale = saleRepository.findById(request.saleId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Venta",
                    identifier = request.saleId,
                    detalles = listOf("Seleccione una venta válida")
                )
            }

        val existingReceipt = receiptRepository.findBySaleId(request.saleId)
        if (existingReceipt != null) {
            throw DuplicateResourceException(
                resourceName = "Recibo",
                field = "venta",
                value = request.saleId,
                detalles = listOf("La venta ya tiene un recibo asociado: ${existingReceipt.receiptNumber}")
            )
        }

        val store = storeRepository.findById(request.storeId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Tienda",
                    identifier = request.storeId,
                    detalles = listOf("Seleccione una tienda válida")
                )
            }

        if (sale.store.id != store.id) {
            throw BusinessValidationException(
                message = "La tienda del recibo debe coincidir con la tienda de la venta",
                detalles = listOf("Venta: ${sale.store.name}, Recibo: ${store.name}")
            )
        }

        val receipt = ReceiptMapper.toEntity(request, user, client, sale, store)
        val savedReceipt = receiptRepository.save(receipt)
        return ReceiptMapper.toResponse(savedReceipt)
    }

    override fun update(id: Long, request: ReceiptRequest): ReceiptResponse {
        val existingReceipt = receiptRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Recibo",
                    identifier = id,
                    detalles = listOf("No se puede actualizar un recibo que no existe")
                )
            }

        if (existingReceipt.isPrinted) {
            throw InvalidOperationException(
                operation = "actualizar el recibo '${existingReceipt.receiptNumber}'",
                reason = "ya ha sido impreso",
                detalles = listOf(
                    "Los recibos impresos no se pueden modificar",
                    "Veces impreso: ${existingReceipt.printCount}",
                    "Solo se permite cancelar recibos impresos"
                )
            )
        }

        if (existingReceipt.receiptStatus != ReceiptStatus.ACTIVE) {
            throw InvalidOperationException(
                operation = "actualizar el recibo '${existingReceipt.receiptNumber}'",
                reason = "el recibo no está activo",
                detalles = listOf("Estado actual: ${existingReceipt.receiptStatus}")
            )
        }

        validateReceiptData(request)

        val receiptNumberExists = receiptRepository.findAll()
            .any { it.receiptNumber == request.receiptNumber && it.id != id }
        if (receiptNumberExists) {
            throw DuplicateResourceException(
                resourceName = "Recibo",
                field = "número",
                value = request.receiptNumber,
                detalles = listOf("Ya existe otro recibo con este número")
            )
        }

        val user = userRepository.findById(request.userId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Usuario",
                    identifier = request.userId,
                    detalles = listOf("Seleccione un usuario válido")
                )
            }

        val client = request.clientId?.let {
            clientRepository.findById(it)
                .orElseThrow {
                    ResourceNotFoundException(
                        resourceName = "Cliente",
                        identifier = it,
                        detalles = listOf("Seleccione un cliente válido")
                    )
                }
        }

        val sale = saleRepository.findById(request.saleId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Venta",
                    identifier = request.saleId,
                    detalles = listOf("Seleccione una venta válida")
                )
            }

        val store = storeRepository.findById(request.storeId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Tienda",
                    identifier = request.storeId,
                    detalles = listOf("Seleccione una tienda válida")
                )
            }

        val updatedReceipt = Receipt(
            receiptNumber = request.receiptNumber,
            user = user,
            client = client,
            sale = sale,
            store = store,
            issueDate = existingReceipt.issueDate,
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
            isPrinted = existingReceipt.isPrinted,
            printCount = existingReceipt.printCount
        ).apply { this.id = existingReceipt.id }

        val savedReceipt = receiptRepository.save(updatedReceipt)
        return ReceiptMapper.toResponse(savedReceipt)
    }

    override fun delete(id: Long) {
        val receipt = receiptRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Recibo",
                    identifier = id,
                    detalles = listOf("No se puede eliminar un recibo que no existe")
                )
            }

        if (receipt.isPrinted) {
            throw InvalidOperationException(
                operation = "eliminar el recibo '${receipt.receiptNumber}'",
                reason = "ya ha sido impreso",
                detalles = listOf("Los recibos impresos no se pueden eliminar, solo cancelar")
            )
        }

        receiptRepository.deleteById(id)
    }

    override fun cancelReceipt(id: Long): ReceiptResponse {
        val receipt = receiptRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Recibo",
                    identifier = id,
                    detalles = listOf("No se puede cancelar un recibo que no existe")
                )
            }

        if (receipt.receiptStatus != ReceiptStatus.ACTIVE) {
            throw InvalidOperationException(
                operation = "cancelar el recibo '${receipt.receiptNumber}'",
                reason = "el recibo ya no está activo",
                detalles = listOf("Estado actual: ${receipt.receiptStatus}")
            )
        }

        val cancelledReceipt = Receipt(
            receiptNumber = receipt.receiptNumber,
            user = receipt.user,
            client = receipt.client,
            sale = receipt.sale,
            store = receipt.store,
            issueDate = receipt.issueDate,
            receiptStatus = ReceiptStatus.CANCELLED,
            subtotal = receipt.subtotal,
            discountAmount = receipt.discountAmount,
            taxPercentage = receipt.taxPercentage,
            taxAmount = receipt.taxAmount,
            totalAmount = receipt.totalAmount,
            paymentMethod = receipt.paymentMethod,
            additionalNotes = receipt.additionalNotes,
            customerName = receipt.customerName,
            customerIdentification = receipt.customerIdentification,
            isPrinted = receipt.isPrinted,
            printCount = receipt.printCount
        ).apply { this.id = receipt.id }

        val savedReceipt = receiptRepository.save(cancelledReceipt)
        return ReceiptMapper.toResponse(savedReceipt)
    }

    override fun markAsPrinted(id: Long): ReceiptResponse {
        val receipt = receiptRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Recibo",
                    identifier = id,
                    detalles = listOf("No se puede marcar como impreso un recibo que no existe")
                )
            }

        if (receipt.receiptStatus != ReceiptStatus.ACTIVE) {
            throw InvalidOperationException(
                operation = "imprimir el recibo '${receipt.receiptNumber}'",
                reason = "el recibo no está activo",
                detalles = listOf("Estado actual: ${receipt.receiptStatus}")
            )
        }

        val printedReceipt = Receipt(
            receiptNumber = receipt.receiptNumber,
            user = receipt.user,
            client = receipt.client,
            sale = receipt.sale,
            store = receipt.store,
            issueDate = receipt.issueDate,
            receiptStatus = receipt.receiptStatus,
            subtotal = receipt.subtotal,
            discountAmount = receipt.discountAmount,
            taxPercentage = receipt.taxPercentage,
            taxAmount = receipt.taxAmount,
            totalAmount = receipt.totalAmount,
            paymentMethod = receipt.paymentMethod,
            additionalNotes = receipt.additionalNotes,
            customerName = receipt.customerName,
            customerIdentification = receipt.customerIdentification,
            isPrinted = true,
            printCount = receipt.printCount + 1
        ).apply { this.id = receipt.id }

        val savedReceipt = receiptRepository.save(printedReceipt)
        return ReceiptMapper.toResponse(savedReceipt)
    }

    override fun findBySaleId(saleId: Long): ReceiptResponse? {
        val receipt = receiptRepository.findBySaleId(saleId)
        return receipt?.let { ReceiptMapper.toResponse(it) }
    }

    override fun findByStoreAndDateRange(
        storeId: Long,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<ReceiptResponse> {
        return receiptRepository.findByStoreAndDateRange(storeId, startDate, endDate)
            .map { ReceiptMapper.toResponse(it) }
    }

    override fun getReceiptsByStatus(status: ReceiptStatus): List<ReceiptResponse> {
        return receiptRepository.findByReceiptStatus(status)
            .map { ReceiptMapper.toResponse(it) }
    }

    override fun generateReceiptNumber(storeId: Long): String {
        val store = storeRepository.findById(storeId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Tienda",
                    identifier = storeId,
                    detalles = listOf("Tienda no encontrada para generar número de recibo")
                )
            }

        val currentDate = LocalDateTime.now()
        val dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd")
        val timeFormat = DateTimeFormatter.ofPattern("HHmmss")

        val storeCode = store.name.take(3).uppercase()
        val dateString = currentDate.format(dateFormat)
        val timeString = currentDate.format(timeFormat)

        return "REC-${storeCode}-${dateString}-${timeString}"
    }

    private fun validateReceiptData(request: ReceiptRequest) {
        if (request.receiptNumber.isBlank()) {
            throw BusinessValidationException(
                message = "El número de recibo no puede estar vacío",
                detalles = listOf("Proporcione un número único para el recibo")
            )
        }

        if (!request.receiptNumber.matches(Regex("^REC-[A-Z]{3}-\\d{8}-\\d{6}$"))) {
            throw BusinessValidationException(
                message = "Formato de número de recibo inválido",
                detalles = listOf(
                    "Formato esperado: REC-XXX-YYYYMMDD-HHMMSS",
                    "Ejemplo: REC-TDA-20240123-143000"
                )
            )
        }

        if (request.customerName != null && request.customerName.isBlank()) {
            throw BusinessValidationException(
                message = "El nombre del cliente no puede estar vacío",
                detalles = listOf("Si proporciona nombre de cliente, debe ser válido")
            )
        }

        if (request.customerIdentification != null && request.customerIdentification.isBlank()) {
            throw BusinessValidationException(
                message = "La identificación del cliente no puede estar vacía",
                detalles = listOf("Si proporciona identificación, debe ser válida")
            )
        }
    }
}