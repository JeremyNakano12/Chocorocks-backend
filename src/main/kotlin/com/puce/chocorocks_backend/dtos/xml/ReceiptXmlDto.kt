package com.puce.chocorocks_backend.dtos.xml

import jakarta.xml.bind.annotation.*

@XmlRootElement(name = "recibo")
@XmlAccessorType(XmlAccessType.FIELD)
data class ReceiptXmlDto(

    @field:XmlElement(name = "numeroRecibo")
    val receiptNumber: String = "",

    @field:XmlElement(name = "fechaEmision")
    val issueDate: String = "",

    @field:XmlElement(name = "estado")
    val status: String = "",

    @field:XmlElement(name = "tienda")
    val store: StoreXmlDto = StoreXmlDto(),

    @field:XmlElement(name = "usuario")
    val user: UserXmlDto = UserXmlDto(),

    @field:XmlElement(name = "cliente")
    val client: ClientXmlDto? = null,

    @field:XmlElement(name = "venta")
    val sale: SaleXmlDto = SaleXmlDto(),

    @field:XmlElement(name = "totales")
    val totals: TotalsXmlDto = TotalsXmlDto(),

    @field:XmlElement(name = "metodoPago")
    val paymentMethod: String? = null,

    @field:XmlElement(name = "notasAdicionales")
    val additionalNotes: String? = null,

    @field:XmlElement(name = "nombreCliente")
    val customerName: String? = null,

    @field:XmlElement(name = "identificacionCliente")
    val customerIdentification: String? = null,

    @field:XmlElement(name = "impreso")
    val isPrinted: Boolean = false,

    @field:XmlElement(name = "vecesImpreso")
    val printCount: Int = 0
) {
    constructor() : this(
        receiptNumber = "",
        issueDate = "",
        status = "",
        store = StoreXmlDto(),
        user = UserXmlDto(),
        client = null,
        sale = SaleXmlDto(),
        totals = TotalsXmlDto(),
        paymentMethod = null,
        additionalNotes = null,
        customerName = null,
        customerIdentification = null,
        isPrinted = false,
        printCount = 0
    )
}

@XmlAccessorType(XmlAccessType.FIELD)
data class StoreXmlDto(
    @field:XmlElement(name = "nombre")
    val name: String = "",

    @field:XmlElement(name = "direccion")
    val address: String = "",

    @field:XmlElement(name = "telefono")
    val phoneNumber: String? = null,

    @field:XmlElement(name = "tipo")
    val type: String = ""
) {
    constructor() : this("", "", null, "")
}

@XmlAccessorType(XmlAccessType.FIELD)
data class UserXmlDto(
    @field:XmlElement(name = "nombre")
    val name: String = "",

    @field:XmlElement(name = "email")
    val email: String = "",

    @field:XmlElement(name = "rol")
    val role: String = ""
) {
    constructor() : this("", "", "")
}

@XmlAccessorType(XmlAccessType.FIELD)
data class ClientXmlDto(
    @field:XmlElement(name = "nombreCompleto")
    val nameLastname: String = "",

    @field:XmlElement(name = "tipoIdentificacion")
    val typeIdentification: String = "",

    @field:XmlElement(name = "numeroIdentificacion")
    val identificationNumber: String = "",

    @field:XmlElement(name = "telefono")
    val phoneNumber: String? = null,

    @field:XmlElement(name = "email")
    val email: String? = null,

    @field:XmlElement(name = "direccion")
    val address: String? = null
) {
    constructor() : this("", "", "", null, null, null)
}

@XmlAccessorType(XmlAccessType.FIELD)
data class SaleXmlDto(
    @field:XmlElement(name = "numeroVenta")
    val saleNumber: String = "",

    @field:XmlElement(name = "tipoVenta")
    val saleType: String = "",

    @field:XmlElementWrapper(name = "detalles")
    @field:XmlElement(name = "detalle")
    val details: List<SaleDetailXmlDto> = emptyList()
) {
    constructor() : this("", "", emptyList())
}

@XmlAccessorType(XmlAccessType.FIELD)
data class SaleDetailXmlDto(
    @field:XmlElement(name = "producto")
    val productName: String = "",

    @field:XmlElement(name = "codigoProducto")
    val productCode: String = "",

    @field:XmlElement(name = "cantidad")
    val quantity: Int = 0,

    @field:XmlElement(name = "precioUnitario")
    val unitPrice: String = "",

    @field:XmlElement(name = "subtotal")
    val subtotal: String = "",

    @field:XmlElement(name = "lote")
    val batchCode: String? = null
) {
    constructor() : this("", "", 0, "", "", null)
}

@XmlAccessorType(XmlAccessType.FIELD)
data class TotalsXmlDto(
    @field:XmlElement(name = "subtotal")
    val subtotal: String = "",

    @field:XmlElement(name = "montoDescuento")
    val discountAmount: String = "",

    @field:XmlElement(name = "porcentajeImpuesto")
    val taxPercentage: String = "",

    @field:XmlElement(name = "montoImpuesto")
    val taxAmount: String = "",

    @field:XmlElement(name = "total")
    val totalAmount: String = ""
) {
    constructor() : this("", "", "", "", "")
}