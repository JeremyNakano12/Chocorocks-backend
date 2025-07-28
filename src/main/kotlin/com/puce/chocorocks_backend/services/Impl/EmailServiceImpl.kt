package com.puce.chocorocks_backend.services.Impl

import com.puce.chocorocks_backend.dtos.responses.ReceiptResponse
import com.puce.chocorocks_backend.services.EmailService
import com.puce.chocorocks_backend.services.XMLGeneratorService
import com.puce.chocorocks_backend.exceptions.BusinessValidationException
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import jakarta.mail.internet.MimeMessage
import org.slf4j.LoggerFactory
import java.time.format.DateTimeFormatter

@Service
class EmailServiceImpl(
    private val mailSender: JavaMailSender,
    private val xmlGeneratorService: XMLGeneratorService,
    @Value("\${spring.mail.username}") private val fromEmail: String,
    @Value("\${app.company.name:Chocorocks}") private val companyName: String,
    @Value("\${app.company.support-email:soporte@chocorocks.com}") private val supportEmail: String
) : EmailService {

    private val logger = LoggerFactory.getLogger(EmailServiceImpl::class.java)

    override fun sendReceiptByEmail(receipt: ReceiptResponse, recipientEmail: String): Boolean {
        if (!validateEmailAddress(recipientEmail)) {
            throw BusinessValidationException(
                message = "Dirección de email inválida: $recipientEmail",
                detalles = listOf("Proporcione una dirección de email válida")
            )
        }

        return try {
            val mimeMessage: MimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(mimeMessage, true, "UTF-8")

            helper.setFrom(fromEmail, companyName)
            helper.setTo(recipientEmail)
            helper.setSubject("Recibo de Compra - ${receipt.receiptNumber}")

            val htmlContent = createEmailContent(receipt)
            helper.setText(htmlContent, true)

            val xmlBytes = xmlGeneratorService.generateReceiptXmlBytes(receipt)
            val xmlResource = ByteArrayResource(xmlBytes)
            helper.addAttachment("recibo_${receipt.receiptNumber}.xml", xmlResource)

            mailSender.send(mimeMessage)

            logger.info("Recibo ${receipt.receiptNumber} enviado por email a: $recipientEmail")
            true

        } catch (e: Exception) {
            logger.error("Error al enviar recibo por email a $recipientEmail: ${e.message}", e)
            throw BusinessValidationException(
                message = "No se pudo enviar el recibo por email",
                detalles = listOf(
                    "Verifique la dirección de email",
                    "Error: ${e.message}"
                )
            )
        }
    }

    override fun sendReceiptByEmail(receipt: ReceiptResponse): Boolean {
        val clientEmail = receipt.client?.email

        if (clientEmail.isNullOrBlank()) {
            throw BusinessValidationException(
                message = "El cliente no tiene email asociado",
                detalles = listOf(
                    "Cliente: ${receipt.customerName ?: "Sin nombre"}",
                    "Agregue un email al cliente o especifique un email de destino"
                )
            )
        }

        return sendReceiptByEmail(receipt, clientEmail)
    }

    override fun validateEmailAddress(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return email.isNotBlank() && emailRegex.matches(email)
    }

    private fun createEmailContent(receipt: ReceiptResponse): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        val formattedDate = receipt.issueDate.format(formatter)

        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Recibo de Compra</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #8B4513; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                    .content { background-color: #f9f9f9; padding: 20px; border: 1px solid #ddd; }
                    .receipt-info { background-color: white; padding: 15px; margin: 10px 0; border-radius: 5px; }
                    .totals { background-color: #fff3cd; padding: 15px; margin: 10px 0; border-radius: 5px; border-left: 4px solid #ffc107; }
                    .footer { background-color: #6c757d; color: white; padding: 15px; text-align: center; border-radius: 0 0 5px 5px; font-size: 12px; }
                    h1, h2 { margin: 0 0 10px 0; }
                    .company-name { font-size: 24px; font-weight: bold; }
                </style>
            </head>
            <body>
            <div class="container">
                <div class="header">
                    <img src="https://www.chocorocksec.com/static/products/images/logo.PNG" alt="Logo de la empresa" style="max-height: 60px; margin-bottom: 10px; display: block; margin-left: auto; margin-right: auto;">
        
                    <div class="company-name" style="color: white;">$companyName</div>
                    <p style="color: white;">Recibo de Compra Electrónico</p>
                </div>
            
                <div class="content">
                    <div class="receipt-info">
                        <h2>Información del Recibo</h2>
                        <p><strong>Número de Recibo:</strong> ${receipt.receiptNumber}</p>
                        <p><strong>Fecha de Emisión:</strong> $formattedDate</p>
                        <p><strong>Tienda:</strong> ${receipt.store.name}</p>
                        <p><strong>Dirección:</strong> ${receipt.store.address}</p>
                        ${if (receipt.paymentMethod != null) "<p><strong>Método de Pago:</strong> ${receipt.paymentMethod}</p>" else ""}
                        <p><strong>Número de Venta:</strong> ${receipt.sale.saleNumber}</p>
                        <p><strong>Atendido por:</strong> ${receipt.user.name}</p>
                    </div>
            
                    ${
                        if (receipt.client != null) """
                    <div class="receipt-info">
                        <h2>Información del Cliente</h2>
                        <p><strong>Nombre:</strong> ${receipt.client.nameLastname}</p>
                        ${if (receipt.client.email != null) "<p><strong>Email:</strong> ${receipt.client.email}</p>" else ""}
                        ${if (receipt.client.phoneNumber != null) "<p><strong>Teléfono:</strong> ${receipt.client.phoneNumber}</p>" else ""}
                    </div>
                    """ else ""
                    }
            
                    <div class="totals">
                        <h2>Resumen de Totales</h2>
                        <p><strong>Subtotal:</strong> ${'$'}${receipt.subtotal}</p>
                        ${if (receipt.discountAmount.compareTo(java.math.BigDecimal.ZERO) > 0) "<p><strong>Descuento:</strong> -$${receipt.discountAmount}</p>" else ""}
                        <p><strong>Impuesto (${receipt.taxPercentage}%):</strong> ${'$'}${receipt.taxAmount}</p>
                        <p style="font-size: 18px; font-weight: bold; color: #8B4513;"><strong>Total:</strong> ${'$'}${receipt.totalAmount}</p>
                    </div>
            
                    ${
                        if (!receipt.additionalNotes.isNullOrBlank()) """
                    <div class="receipt-info">
                        <h2>Notas Adicionales</h2>
                        <p>${receipt.additionalNotes}</p>
                    </div>
                    """ else ""
                    }
            
                    <div class="receipt-info">
                        <p><strong>Nota:</strong> Se adjunta el archivo XML con el detalle completo de la compra.</p>
                        <p><em>Este es un recibo electrónico generado automáticamente.</em></p>
                    </div>
                </div>
            
                <div class="footer">
                    <p>Gracias por su compra en $companyName</p>
                    <p>Para soporte técnico contacte: $supportEmail</p>
                    <p>Este correo fue generado automáticamente, por favor no responda a esta dirección.</p>
                </div>
            </div>
            </body>
            </html>
        """.trimIndent()
    }
}