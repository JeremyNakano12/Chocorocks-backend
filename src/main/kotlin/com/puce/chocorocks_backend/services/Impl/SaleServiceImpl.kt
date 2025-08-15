package com.puce.chocorocks_backend.services.Impl

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.dtos.responses.*
import com.puce.chocorocks_backend.mappers.*
import com.puce.chocorocks_backend.repositories.*
import com.puce.chocorocks_backend.services.*
import com.puce.chocorocks_backend.models.entities.*
import com.puce.chocorocks_backend.utils.UserActivityHelper
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import com.puce.chocorocks_backend.exceptions.*

@Service
@Transactional
class SaleServiceImpl(
    private val saleRepository: SaleRepository,
    private val userRepository: UserRepository,
    private val clientRepository: ClientRepository,
    private val storeRepository: StoreRepository,
    private val saleDetailRepository: SaleDetailRepository,
    private val receiptService: ReceiptService,
    private val userActivityService: UserActivityService,
    private val userActivityHelper: UserActivityHelper
) : SaleService {

    override fun findAll(): List<SaleResponse> =
        saleRepository.findAll().map { SaleMapper.toResponse(it) }

    override fun findById(id: Long): SaleResponse {
        val sale = saleRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Venta",
                    identifier = id,
                    detalles = listOf("Verifique que el ID de la venta sea correcto")
                )
            }
        return SaleMapper.toResponse(sale)
    }

    override fun save(request: SaleRequest): SaleResponse {
        validateSaleData(request)

        val saleNumberExists = saleRepository.existsBySaleNumber(request.saleNumber)
        if (saleNumberExists) {
            throw DuplicateResourceException(
                resourceName = "Venta",
                field = "número",
                value = request.saleNumber,
                detalles = listOf("El número de venta debe ser único")
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

        val store = storeRepository.findById(request.storeId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Tienda",
                    identifier = request.storeId,
                    detalles = listOf("Seleccione una tienda válida")
                )
            }

        val sale = SaleMapper.toEntity(request, user, client, store)
        val savedSale = saleRepository.save(sale)

        recalculateSaleTotals(savedSale.id)

        val finalSale = saleRepository.findById(savedSale.id).get()

        try {
            val clientInfo = client?.let { " para cliente ${it.nameLastname}" } ?: ""
            val activityRequest = userActivityHelper.createActivityRequest(
                actionType = "CREATE",
                tableName = "sales",
                recordId = finalSale.id,
                description = "Creó venta ${finalSale.saleNumber} por $${finalSale.totalAmount} en tienda ${store.name}$clientInfo"
            )
            userActivityService.save(activityRequest)

            println("✅ Actividad registrada: Usuario ${userActivityHelper.getCurrentUserEmail()} creó venta ${finalSale.saleNumber}")
        } catch (e: Exception) {
            println("❌ Error logging user activity: ${e.message}")
        }

        return SaleMapper.toResponse(finalSale)
    }

    override fun update(id: Long, request: SaleRequest): SaleResponse {
        val existingSale = saleRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Venta",
                    identifier = id,
                    detalles = listOf("No se puede actualizar una venta que no existe")
                )
            }

        validateSaleData(request)

        val saleNumberExists = saleRepository.existsBySaleNumber(request.saleNumber)
        if (saleNumberExists && existingSale.saleNumber != request.saleNumber) {
            throw DuplicateResourceException(
                resourceName = "Venta",
                field = "número",
                value = request.saleNumber,
                detalles = listOf("Ya existe otra venta con este número")
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

        val store = storeRepository.findById(request.storeId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Tienda",
                    identifier = request.storeId,
                    detalles = listOf("Seleccione una tienda válida")
                )
            }

        val oldTotal = existingSale.totalAmount
        val oldClient = existingSale.client?.nameLastname

        val updatedSale = Sale(
            saleNumber = request.saleNumber,
            user = user,
            client = client,
            store = store,
            saleType = request.saleType,
            subtotal = existingSale.subtotal,
            discountPercentage = request.discountPercentage,
            discountAmount = request.discountAmount,
            taxPercentage = request.taxPercentage,
            taxAmount = request.taxAmount,
            totalAmount = existingSale.totalAmount,
            paymentMethod = request.paymentMethod,
            notes = request.notes,
            isInvoiced = request.isInvoiced
        ).apply { this.id = existingSale.id }

        val savedSale = saleRepository.save(updatedSale)

        recalculateSaleTotals(savedSale.id)

        val finalSale = saleRepository.findById(savedSale.id).get()

        try {
            val changes = mutableListOf<String>()

            if (oldTotal != finalSale.totalAmount) {
                changes.add("Total: $${oldTotal} → $${finalSale.totalAmount}")
            }

            if (oldClient != client?.nameLastname) {
                val oldClientText = oldClient ?: "Sin cliente"
                val newClientText = client?.nameLastname ?: "Sin cliente"
                changes.add("Cliente: $oldClientText → $newClientText")
            }

            val changesText = if (changes.isNotEmpty()) " (${changes.joinToString(", ")})" else ""

            val activityRequest = userActivityHelper.createActivityRequest(
                actionType = "UPDATE",
                tableName = "sales",
                recordId = finalSale.id,
                description = "Actualizó venta ${finalSale.saleNumber} en tienda ${store.name}$changesText"
            )
            userActivityService.save(activityRequest)
        } catch (e: Exception) {
            println("❌ Error logging user activity: ${e.message}")
        }

        return SaleMapper.toResponse(finalSale)
    }

    override fun delete(id: Long) {
        val sale = saleRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Venta",
                    identifier = id,
                    detalles = listOf("No se puede eliminar una venta que no existe")
                )
            }

        if (sale.isInvoiced) {
            throw InvalidOperationException(
                operation = "eliminar la venta '${sale.saleNumber}'",
                reason = "ya ha sido facturada",
                detalles = listOf("No se pueden eliminar ventas facturadas")
            )
        }

        val receipt = receiptService.findBySaleId(sale.id)
        if (receipt != null) {
            throw InvalidOperationException(
                operation = "eliminar la venta '${sale.saleNumber}'",
                reason = "ya tiene un recibo asociado",
                detalles = listOf("Cancele primero el recibo: ${receipt.receiptNumber}")
            )
        }

        try {
            val activityRequest = userActivityHelper.createActivityRequest(
                actionType = "DELETE",
                tableName = "sales",
                recordId = sale.id,
                description = "Eliminó venta ${sale.saleNumber} (Total: $${sale.totalAmount}) de tienda ${sale.store.name}"
            )
            userActivityService.save(activityRequest)
        } catch (e: Exception) {
            println("❌ Error logging user activity: ${e.message}")
        }

        saleRepository.deleteById(id)
    }

    fun completeWithReceipt(id: Long, paymentMethod: String?, additionalNotes: String?): ReceiptResponse {
        val sale = saleRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Venta",
                    identifier = id,
                    detalles = listOf("No se puede completar una venta que no existe")
                )
            }

        val receiptRequest = ReceiptRequest(
            receiptNumber = receiptService.generateReceiptNumber(sale.store.id),
            userId = sale.user.id,
            clientId = sale.client?.id,
            saleId = sale.id,
            storeId = sale.store.id,
            receiptStatus = ReceiptStatus.ACTIVE,
            paymentMethod = paymentMethod ?: sale.paymentMethod,
            additionalNotes = additionalNotes,
            customerName = sale.client?.nameLastname,
            customerIdentification = sale.client?.identificationNumber
        )

        val receipt = receiptService.save(receiptRequest)

        try {
            val activityRequest = userActivityHelper.createActivityRequest(
                actionType = "CREATE",
                tableName = "receipts",
                recordId = receipt.id,
                description = "Generó recibo ${receipt.receiptNumber} para venta ${sale.saleNumber} (Total: $${sale.totalAmount})"
            )
            userActivityService.save(activityRequest)
        } catch (e: Exception) {
            println("❌ Error logging user activity: ${e.message}")
        }

        return receipt
    }

    private fun validateSaleData(request: SaleRequest) {
        if (request.saleNumber.isBlank()) {
            throw BusinessValidationException(
                message = "El número de venta no puede estar vacío",
                detalles = listOf("Proporcione un número único para la venta")
            )
        }

        if (request.discountPercentage < BigDecimal.ZERO || request.discountPercentage > BigDecimal(100)) {
            throw BusinessValidationException(
                message = "El porcentaje de descuento debe estar entre 0 y 100",
                detalles = listOf("Porcentaje actual: ${request.discountPercentage}")
            )
        }

        if (request.taxPercentage < BigDecimal.ZERO) {
            throw BusinessValidationException(
                message = "El porcentaje de impuesto no puede ser negativo",
                detalles = listOf("Porcentaje actual: ${request.taxPercentage}")
            )
        }

        if (request.discountAmount < BigDecimal.ZERO) {
            throw BusinessValidationException(
                message = "El monto de descuento no puede ser negativo",
                detalles = listOf("Monto actual: ${request.discountAmount}")
            )
        }
    }

    private fun recalculateSaleTotals(saleId: Long) {
        val sale = saleRepository.findById(saleId).orElse(null) ?: return

        val saleDetails = saleDetailRepository.findBySaleId(saleId)
        val subtotal = saleDetails.sumOf { it.subtotal }

        val discountAmount = if (sale.discountPercentage > BigDecimal.ZERO) {
            subtotal * sale.discountPercentage / BigDecimal(100)
        } else {
            sale.discountAmount
        }

        val totalWithDiscount = subtotal - discountAmount
        val taxAmount = totalWithDiscount * sale.taxPercentage / BigDecimal(100)
        val totalAmount = totalWithDiscount + taxAmount

        val updatedSale = Sale(
            saleNumber = sale.saleNumber,
            user = sale.user,
            client = sale.client,
            store = sale.store,
            saleType = sale.saleType,
            subtotal = subtotal,
            discountPercentage = sale.discountPercentage,
            discountAmount = discountAmount,
            taxPercentage = sale.taxPercentage,
            taxAmount = taxAmount,
            totalAmount = totalAmount,
            paymentMethod = sale.paymentMethod,
            notes = sale.notes,
            isInvoiced = sale.isInvoiced
        ).apply { this.id = sale.id }

        saleRepository.save(updatedSale)
    }
}