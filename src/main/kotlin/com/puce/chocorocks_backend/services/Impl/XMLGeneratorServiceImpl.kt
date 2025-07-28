package com.puce.chocorocks_backend.services.Impl

import com.puce.chocorocks_backend.dtos.responses.ReceiptResponse
import com.puce.chocorocks_backend.dtos.xml.*
import com.puce.chocorocks_backend.services.XMLGeneratorService
import com.puce.chocorocks_backend.repositories.SaleDetailRepository
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.JAXBException
import jakarta.xml.bind.Marshaller
import org.springframework.stereotype.Service
import java.io.StringWriter
import java.time.format.DateTimeFormatter

@Service
class XMLGeneratorServiceImpl(
    private val saleDetailRepository: SaleDetailRepository
) : XMLGeneratorService {

    override fun generateReceiptXml(receipt: ReceiptResponse): String {
        val receiptXml = mapToXmlDto(receipt)
        return marshallToXml(receiptXml)
    }

    override fun generateReceiptXmlBytes(receipt: ReceiptResponse): ByteArray {
        val xmlString = generateReceiptXml(receipt)
        return xmlString.toByteArray(Charsets.UTF_8)
    }

    private fun mapToXmlDto(receipt: ReceiptResponse): ReceiptXmlDto {
        val saleDetails = saleDetailRepository.findBySaleId(receipt.sale.id)

        val saleDetailsXml = saleDetails.map { detail ->
            SaleDetailXmlDto(
                productName = detail.product.nameProduct,
                productCode = detail.product.code,
                quantity = detail.quantity,
                unitPrice = detail.unitPrice.toString(),
                subtotal = detail.subtotal.toString(),
                batchCode = detail.batch?.batchCode
            )
        }

        return ReceiptXmlDto(
            receiptNumber = receipt.receiptNumber,
            issueDate = receipt.issueDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            status = receipt.receiptStatus.name,
            store = StoreXmlDto(
                name = receipt.store.name,
                address = receipt.store.address,
                phoneNumber = receipt.store.phoneNumber,
                type = receipt.store.typeStore.name
            ),
            user = UserXmlDto(
                name = receipt.user.name,
                email = receipt.user.email,
                role = receipt.user.role.name
            ),
            client = receipt.client?.let { client ->
                ClientXmlDto(
                    nameLastname = client.nameLastname,
                    typeIdentification = client.typeIdentification.name,
                    identificationNumber = client.identificationNumber,
                    phoneNumber = client.phoneNumber,
                    email = client.email,
                    address = client.address
                )
            },
            sale = SaleXmlDto(
                saleNumber = receipt.sale.saleNumber,
                saleType = receipt.sale.saleType.name,
                details = saleDetailsXml
            ),
            totals = TotalsXmlDto(
                subtotal = receipt.subtotal.toString(),
                discountAmount = receipt.discountAmount.toString(),
                taxPercentage = receipt.taxPercentage.toString(),
                taxAmount = receipt.taxAmount.toString(),
                totalAmount = receipt.totalAmount.toString()
            ),
            paymentMethod = receipt.paymentMethod,
            additionalNotes = receipt.additionalNotes,
            customerName = receipt.customerName,
            customerIdentification = receipt.customerIdentification,
            isPrinted = receipt.isPrinted,
            printCount = receipt.printCount
        )
    }

    private fun marshallToXml(receiptXml: ReceiptXmlDto): String {
        return try {
            val context = JAXBContext.newInstance(ReceiptXmlDto::class.java)
            val marshaller = context.createMarshaller()

            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8")

            val stringWriter = StringWriter()

            marshaller.marshal(receiptXml, stringWriter)

            var result = stringWriter.toString()

            if (result.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")) {
                result = result.replace(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!-- Recibo generado por ChocorRocks System -->"
                )
            }

            result
        } catch (e: JAXBException) {
            throw RuntimeException("Error al generar XML del recibo: ${e.message}", e)
        }
    }
}