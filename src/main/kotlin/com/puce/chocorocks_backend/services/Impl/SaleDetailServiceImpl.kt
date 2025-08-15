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
import com.puce.chocorocks_backend.utils.*

@Service
@Transactional
class SaleDetailServiceImpl(
    private val saleDetailRepository: SaleDetailRepository,
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository,
    private val productBatchRepository: ProductBatchRepository,
    private val productStoreRepository: ProductStoreRepository,
    private val inventoryMovementService: InventoryMovementService,
    private val userRepository: UserRepository,
    private val userActivityService: UserActivityService,
    private val userActivityHelper: UserActivityHelper
) : SaleDetailService {

    override fun findAll(): List<SaleDetailResponse> =
        saleDetailRepository.findAll().map { SaleDetailMapper.toResponse(it) }

    override fun findById(id: Long): SaleDetailResponse {
        val saleDetail = saleDetailRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Detalle de venta",
                    identifier = id,
                    detalles = listOf("Verifique que el ID del detalle sea correcto")
                )
            }
        return SaleDetailMapper.toResponse(saleDetail)
    }

    override fun save(request: SaleDetailRequest): SaleDetailResponse {
        ValidationUtils.validatePositiveQuantity(request.quantity, "cantidad")

        val sale = saleRepository.findById(request.saleId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Venta",
                    identifier = request.saleId,
                    detalles = listOf("Seleccione una venta válida")
                )
            }

        val product = productRepository.findById(request.productId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Producto",
                    identifier = request.productId,
                    detalles = listOf("Seleccione un producto válido")
                )
            }

        val batch = request.batchId?.let {
            val productBatch = productBatchRepository.findById(it)
                .orElseThrow {
                    ResourceNotFoundException(
                        resourceName = "Lote",
                        identifier = it,
                        detalles = listOf("Seleccione un lote válido")
                    )
                }

            ValidationUtils.validateBatchNotExpired(productBatch.expirationDate, productBatch.batchCode)

            val currentQuantity = productBatchRepository.getCurrentQuantityById(it) ?: 0
            ValidationUtils.validateSufficientStock(
                available = currentQuantity,
                requested = request.quantity,
                productName = product.nameProduct
            )

            productBatch
        }

        val unitPrice = when (sale.saleType) {
            SaleType.RETAIL -> product.retailPrice
            SaleType.WHOLESALE -> product.wholesalePrice
        }

        val subtotal = unitPrice * BigDecimal(request.quantity)

        val saleDetail = SaleDetailMapper.toEntity(request, sale, product, batch, unitPrice, subtotal)
        val savedSaleDetail = saleDetailRepository.save(saleDetail)

        updateStockAfterSale(savedSaleDetail)

        recalculateSaleTotals(sale.id)

        try {
            val batchInfo = batch?.let { " del lote '${it.batchCode}'" } ?: ""
            val saleTypeInfo = when (sale.saleType) {
                SaleType.RETAIL -> "al por menor"
                SaleType.WHOLESALE -> "al por mayor"
            }

            val activityRequest = userActivityHelper.createActivityRequest(
                actionType = "CREATE",
                tableName = "sale_details",
                recordId = savedSaleDetail.id,
                description = "Agregó ${request.quantity} unidades de '${product.nameProduct}'$batchInfo a venta ${sale.saleNumber} ($saleTypeInfo) - Precio unitario: $${unitPrice}, Subtotal: $${subtotal}"
            )
            userActivityService.save(activityRequest)

            println("✅ Actividad registrada: Usuario ${userActivityHelper.getCurrentUserEmail()} agregó ${request.quantity} ${product.nameProduct} a venta ${sale.saleNumber}")
        } catch (e: Exception) {
            println("❌ Error logging user activity: ${e.message}")
        }

        return SaleDetailMapper.toResponse(savedSaleDetail)
    }

    override fun update(id: Long, request: SaleDetailRequest): SaleDetailResponse {
        val existingSaleDetail = saleDetailRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Detalle de venta",
                    identifier = id,
                    detalles = listOf("No se puede actualizar un detalle que no existe")
                )
            }

        ValidationUtils.validatePositiveQuantity(request.quantity, "cantidad")

        val sale = saleRepository.findById(request.saleId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Venta",
                    identifier = request.saleId,
                    detalles = listOf("Seleccione una venta válida")
                )
            }

        val product = productRepository.findById(request.productId)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Producto",
                    identifier = request.productId,
                    detalles = listOf("Seleccione un producto válido")
                )
            }

        val batch = request.batchId?.let {
            val productBatch = productBatchRepository.findById(it)
                .orElseThrow {
                    ResourceNotFoundException(
                        resourceName = "Lote",
                        identifier = it,
                        detalles = listOf("Seleccione un lote válido")
                    )
                }

            ValidationUtils.validateBatchNotExpired(productBatch.expirationDate, productBatch.batchCode)

            val currentQuantity = productBatchRepository.getCurrentQuantityById(it) ?: 0
            val availableQuantity = currentQuantity + existingSaleDetail.quantity
            ValidationUtils.validateSufficientStock(
                available = availableQuantity,
                requested = request.quantity,
                productName = product.nameProduct
            )

            productBatch
        }

        val unitPrice = when (sale.saleType) {
            SaleType.RETAIL -> product.retailPrice
            SaleType.WHOLESALE -> product.wholesalePrice
        }

        val subtotal = unitPrice * BigDecimal(request.quantity)

        val oldQuantity = existingSaleDetail.quantity
        val oldSubtotal = existingSaleDetail.subtotal
        val oldProduct = existingSaleDetail.product.nameProduct

        val quantityDifference = request.quantity - existingSaleDetail.quantity
        if (quantityDifference != 0) {
            updateStockAfterQuantityChange(existingSaleDetail, quantityDifference)
        }

        val updatedSaleDetail = SaleDetail(
            sale = sale,
            product = product,
            batch = batch,
            quantity = request.quantity,
            unitPrice = unitPrice,
            subtotal = subtotal
        ).apply { this.id = existingSaleDetail.id }

        val savedSaleDetail = saleDetailRepository.save(updatedSaleDetail)

        recalculateSaleTotals(sale.id)

        try {
            val changes = mutableListOf<String>()

            if (oldProduct != product.nameProduct) {
                changes.add("Producto: '$oldProduct' → '${product.nameProduct}'")
            }

            if (oldQuantity != request.quantity) {
                changes.add("Cantidad: $oldQuantity → ${request.quantity}")
            }

            if (oldSubtotal != subtotal) {
                changes.add("Subtotal: $${oldSubtotal} → $${subtotal}")
            }

            val batchInfo = batch?.let { " del lote '${it.batchCode}'" } ?: ""
            val changesText = if (changes.isNotEmpty()) " (${changes.joinToString(", ")})" else ""

            val activityRequest = userActivityHelper.createActivityRequest(
                actionType = "UPDATE",
                tableName = "sale_details",
                recordId = savedSaleDetail.id,
                description = "Actualizó detalle de '${product.nameProduct}'$batchInfo en venta ${sale.saleNumber}$changesText"
            )
            userActivityService.save(activityRequest)
        } catch (e: Exception) {
            println("❌ Error logging user activity: ${e.message}")
        }

        return SaleDetailMapper.toResponse(savedSaleDetail)
    }

    override fun delete(id: Long) {
        val saleDetail = saleDetailRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Detalle de venta",
                    identifier = id,
                    detalles = listOf("No se puede eliminar un detalle que no existe")
                )
            }

        val saleId = saleDetail.sale.id

        try {
            val batchInfo = saleDetail.batch?.let { " del lote '${it.batchCode}'" } ?: ""
            val activityRequest = userActivityHelper.createActivityRequest(
                actionType = "DELETE",
                tableName = "sale_details",
                recordId = saleDetail.id,
                description = "Eliminó ${saleDetail.quantity} unidades de '${saleDetail.product.nameProduct}'$batchInfo de venta ${saleDetail.sale.saleNumber} (Subtotal eliminado: $${saleDetail.subtotal})"
            )
            userActivityService.save(activityRequest)
        } catch (e: Exception) {
            println("❌ Error logging user activity: ${e.message}")
        }

        restoreStockAfterDelete(saleDetail)

        saleDetailRepository.deleteById(id)

        recalculateSaleTotals(saleId)
    }

    private fun updateStockAfterSale(saleDetail: SaleDetail) {
        saleDetail.batch?.let { batch ->
            val updatedBatch = ProductBatch(
                batchCode = batch.batchCode,
                product = batch.product,
                productionDate = batch.productionDate,
                expirationDate = batch.expirationDate,
                initialQuantity = batch.initialQuantity,
                currentQuantity = (productBatchRepository.getCurrentQuantityById(batch.id) ?: 0) - saleDetail.quantity,
                batchCost = batch.batchCost,
                store = batch.store,
                isActive = batch.isActive
            ).apply { this.id = batch.id }

            productBatchRepository.save(updatedBatch)
        }

        val productStore = productStoreRepository.findByProductIdAndStoreId(
            saleDetail.product.id,
            saleDetail.sale.store.id
        )

        productStore?.let { ps ->
            val updatedProductStore = ProductStore(
                product = ps.product,
                store = ps.store,
                currentStock = ps.currentStock - saleDetail.quantity,
                minStockLevel = ps.minStockLevel
            ).apply { this.id = ps.id }

            productStoreRepository.save(updatedProductStore)
        }

        createInventoryMovement(saleDetail, MovementType.OUT, MovementReason.SALE)
    }

    private fun updateStockAfterQuantityChange(saleDetail: SaleDetail, quantityDifference: Int) {
        saleDetail.batch?.let { batch ->
            val updatedBatch = ProductBatch(
                batchCode = batch.batchCode,
                product = batch.product,
                productionDate = batch.productionDate,
                expirationDate = batch.expirationDate,
                initialQuantity = batch.initialQuantity,
                currentQuantity = batch.currentQuantity - quantityDifference,
                batchCost = batch.batchCost,
                store = batch.store,
                isActive = batch.isActive
            ).apply { this.id = batch.id }

            productBatchRepository.save(updatedBatch)
        }

        val productStore = productStoreRepository.findByProductIdAndStoreId(
            saleDetail.product.id,
            saleDetail.sale.store.id
        )

        productStore?.let { ps ->
            val updatedProductStore = ProductStore(
                product = ps.product,
                store = ps.store,
                currentStock = ps.currentStock - quantityDifference,
                minStockLevel = ps.minStockLevel
            ).apply { this.id = ps.id }

            productStoreRepository.save(updatedProductStore)
        }

        if (quantityDifference > 0) {
            createInventoryMovementForQuantity(saleDetail, quantityDifference, MovementType.OUT, MovementReason.SALE)
        } else {
            createInventoryMovementForQuantity(saleDetail, -quantityDifference, MovementType.IN, MovementReason.ADJUSTMENT)
        }
    }

    private fun restoreStockAfterDelete(saleDetail: SaleDetail) {
        saleDetail.batch?.let { batch ->
            val updatedBatch = ProductBatch(
                batchCode = batch.batchCode,
                product = batch.product,
                productionDate = batch.productionDate,
                expirationDate = batch.expirationDate,
                initialQuantity = batch.initialQuantity,
                currentQuantity = batch.currentQuantity + saleDetail.quantity,
                batchCost = batch.batchCost,
                store = batch.store,
                isActive = batch.isActive
            ).apply { this.id = batch.id }

            productBatchRepository.save(updatedBatch)
        }

        val productStore = productStoreRepository.findByProductIdAndStoreId(
            saleDetail.product.id,
            saleDetail.sale.store.id
        )

        productStore?.let { ps ->
            val updatedProductStore = ProductStore(
                product = ps.product,
                store = ps.store,
                currentStock = ps.currentStock + saleDetail.quantity,
                minStockLevel = ps.minStockLevel
            ).apply { this.id = ps.id }

            productStoreRepository.save(updatedProductStore)
        }

        createInventoryMovement(saleDetail, MovementType.IN, MovementReason.ADJUSTMENT)
    }

    private fun createInventoryMovement(saleDetail: SaleDetail, movementType: MovementType, reason: MovementReason) {
        val movementRequest = InventoryMovementRequest(
            movementType = movementType,
            productId = saleDetail.product.id,
            batchId = saleDetail.batch?.id,
            fromStoreId = if (movementType == MovementType.OUT) saleDetail.sale.store.id else null,
            toStoreId = if (movementType == MovementType.IN) saleDetail.sale.store.id else null,
            quantity = saleDetail.quantity,
            reason = reason,
            referenceId = saleDetail.sale.id,
            referenceType = "SALE",
            userId = saleDetail.sale.user.id,
            notes = "Movimiento automático por venta ${saleDetail.sale.saleNumber}"
        )

        inventoryMovementService.save(movementRequest)
    }

    private fun createInventoryMovementForQuantity(saleDetail: SaleDetail, quantity: Int, movementType: MovementType, reason: MovementReason) {
        val movementRequest = InventoryMovementRequest(
            movementType = movementType,
            productId = saleDetail.product.id,
            batchId = saleDetail.batch?.id,
            fromStoreId = if (movementType == MovementType.OUT) saleDetail.sale.store.id else null,
            toStoreId = if (movementType == MovementType.IN) saleDetail.sale.store.id else null,
            quantity = quantity,
            reason = reason,
            referenceId = saleDetail.sale.id,
            referenceType = "SALE_ADJUSTMENT",
            userId = saleDetail.sale.user.id,
            notes = "Ajuste automático por modificación de venta ${saleDetail.sale.saleNumber}"
        )

        inventoryMovementService.save(movementRequest)
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