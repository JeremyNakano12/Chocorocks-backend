package com.puce.chocorocks_backend.models.entities

//Receipt
enum class ReceiptStatus {
    ACTIVE, CANCELLED, REFUNDED
}

//InventoryMovement
enum class MovementType {
    IN, OUT, TRANSFER
}

enum class MovementReason {
    PRODUCTION, SALE, TRANSFER, ADJUSTMENT, DAMAGE, EXPIRED
}

//Invoice
enum class InvoiceStatus {
    PENDING, SENT, CANCELLED
}

//Sale
enum class SaleType {
    RETAIL, WHOLESALE
}

//Store
enum class StoreType {
    FISICA, MOVIL, BODEGA
}

//User
enum class UserRole {
    ADMIN, EMPLOYEE
}

enum class IdentificationType {
    CEDULA, PASAPORTE, RUC
}
