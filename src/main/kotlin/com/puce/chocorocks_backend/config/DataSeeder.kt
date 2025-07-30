package com.puce.chocorocks_backend.config

import com.puce.chocorocks_backend.dtos.requests.*
import com.puce.chocorocks_backend.services.*
import com.puce.chocorocks_backend.models.entities.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalTime

@Component
class DataSeeder(
    private val categoryService: CategoryService,
    private val storeService: StoreService,
    private val userService: UserService,
    private val clientService: ClientService,
    private val productService: ProductService,
    @Value("\${app.data.seed:false}") private val enableSeeding: Boolean
) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(DataSeeder::class.java)

    override fun run(vararg args: String?) {
        if (!enableSeeding) {
            logger.info("🚫 Creación de datos por defecto deshabilitada")
            return
        }

        logger.info("🌱 Iniciando creación de datos por defecto...")

        try {
            createDefaultCategories()
            createDefaultStores()
            createDefaultUsers()
            createDefaultClients()
            createDefaultProducts()

            logger.info("✅ Datos por defecto creados exitosamente")
        } catch (e: Exception) {
            logger.error("❌ Error creando datos por defecto: ${e.message}", e)
        }
    }

    private fun createDefaultCategories() {
        logger.info("📂 Creando categorías por defecto...")

        val categories = listOf(
            CategoryRequest(
                name = "Chocolates",
                description = "Productos de chocolate artesanal"
            ),
            CategoryRequest(
                name = "Dulces",
                description = "Variedad de dulces y golosinas"
            ),
            CategoryRequest(
                name = "Bombones",
                description = "Bombones rellenos premium"
            ),
            CategoryRequest(
                name = "Temporada",
                description = "Productos de temporada especial"
            )
        )

        categories.forEach { categoryRequest ->
            try {
                categoryService.save(categoryRequest)
                logger.info("✅ Categoría creada: ${categoryRequest.name}")
            } catch (e: Exception) {
                logger.warn("⚠️ Categoría ya existe o error: ${categoryRequest.name}")
            }
        }
    }

    private fun createDefaultStores() {
        logger.info("🏪 Creando tiendas por defecto...")

        val stores = listOf(
            StoreRequest(
                name = "Tienda Principal",
                address = "Av. Principal 123, Centro, Quito",
                typeStore = StoreType.FISICA,
                phoneNumber = "02-2345678",
                scheduleOpen = LocalTime.of(8, 0),
                scheduleClosed = LocalTime.of(20, 0),
                isActive = true
            ),
            StoreRequest(
                name = "Sucursal Norte",
                address = "Centro Comercial El Bosque, Local 45",
                typeStore = StoreType.FISICA,
                phoneNumber = "02-2987654",
                scheduleOpen = LocalTime.of(10, 0),
                scheduleClosed = LocalTime.of(22, 0),
                isActive = true
            ),
            StoreRequest(
                name = "Bodega Central",
                address = "Zona Industrial, Calle 15 y Av. 10 de Agosto",
                typeStore = StoreType.BODEGA,
                phoneNumber = "02-2111222",
                isActive = true
            )
        )

        stores.forEach { storeRequest ->
            try {
                storeService.save(storeRequest)
                logger.info("✅ Tienda creada: ${storeRequest.name}")
            } catch (e: Exception) {
                logger.warn("⚠️ Tienda ya existe o error: ${storeRequest.name}")
            }
        }
    }

    private fun createDefaultUsers() {
        logger.info("👥 Creando usuarios por defecto...")

        val users = listOf(
            UserRequest(
                name = "Administrador Principal",
                email = "admin@chocorocks.com",
                role = UserRole.ADMIN,
                typeIdentification = IdentificationType.CEDULA,
                identificationNumber = "1234567890",
                phoneNumber = "0999888777",
                isActive = true
            ),
            UserRequest(
                name = "María González",
                email = "maria.gonzalez@chocorocks.com",
                role = UserRole.EMPLOYEE,
                typeIdentification = IdentificationType.CEDULA,
                identificationNumber = "0987654321",
                phoneNumber = "0999777666",
                isActive = true
            ),
            UserRequest(
                name = "Carlos Pérez",
                email = "carlos.perez@chocorocks.com",
                role = UserRole.EMPLOYEE,
                typeIdentification = IdentificationType.CEDULA,
                identificationNumber = "1122334455",
                phoneNumber = "0999666555",
                isActive = true
            )
        )

        users.forEach { userRequest ->
            try {
                userService.save(userRequest)
                logger.info("✅ Usuario creado: ${userRequest.email}")
            } catch (e: Exception) {
                logger.warn("⚠️ Usuario ya existe o error: ${userRequest.email} - ${e.message}")
            }
        }
    }

    private fun createDefaultClients() {
        logger.info("👤 Creando clientes por defecto...")

        val clients = listOf(
            ClientRequest(
                nameLastname = "Ana Rodríguez",
                typeIdentification = IdentificationType.CEDULA,
                identificationNumber = "1700123456",
                phoneNumber = "0998765432",
                email = "ana.rodriguez@email.com",
                address = "Av. 6 de Diciembre N24-253, Quito",
                requiresInvoice = true,
                isActive = true
            ),
            ClientRequest(
                nameLastname = "Jorge Morales",
                typeIdentification = IdentificationType.CEDULA,
                identificationNumber = "1700654321",
                phoneNumber = "0987654321",
                email = "jorge.morales@email.com",
                address = "Calle García Moreno 721, Quito",
                requiresInvoice = false,
                isActive = true
            ),
            ClientRequest(
                nameLastname = "Empresa ABC S.A.",
                typeIdentification = IdentificationType.RUC,
                identificationNumber = "1790123456001",
                phoneNumber = "02-2345678",
                email = "compras@empresaabc.com",
                address = "Av. República del Salvador N35-17, Quito",
                requiresInvoice = true,
                isActive = true
            ),
            ClientRequest(
                nameLastname = "Cliente General",
                typeIdentification = IdentificationType.CEDULA,
                identificationNumber = "9999999999",
                phoneNumber = null,
                email = null,
                address = null,
                requiresInvoice = false,
                isActive = true
            )
        )

        clients.forEach { clientRequest ->
            try {
                clientService.save(clientRequest)
                logger.info("✅ Cliente creado: ${clientRequest.nameLastname}")
            } catch (e: Exception) {
                logger.warn("⚠️ Cliente ya existe o error: ${clientRequest.nameLastname}")
            }
        }
    }

    private fun createDefaultProducts() {
        logger.info("🍫 Creando productos por defecto...")

        // Nota: Asumiendo que las categorías ya están creadas con IDs 1, 2, 3, 4
        val products = listOf(
            ProductRequest(
                code = "CHOC-001",
                nameProduct = "Chocolate Amargo 70%",
                description = "Chocolate artesanal con 70% de cacao",
                categoryId = 1, // Chocolates
                flavor = "Amargo",
                size = "100g",
                productionCost = BigDecimal("2.50"),
                wholesalePrice = BigDecimal("4.00"),
                retailPrice = BigDecimal("6.00"),
                minStockLevel = 20,
                isActive = true
            ),
            ProductRequest(
                code = "CHOC-002",
                nameProduct = "Chocolate con Leche",
                description = "Chocolate suave con leche premium",
                categoryId = 1, // Chocolates
                flavor = "Dulce",
                size = "100g",
                productionCost = BigDecimal("2.30"),
                wholesalePrice = BigDecimal("3.80"),
                retailPrice = BigDecimal("5.50"),
                minStockLevel = 25,
                isActive = true
            ),
            ProductRequest(
                code = "BOMB-001",
                nameProduct = "Bombón de Fresa",
                description = "Bombón relleno de crema de fresa",
                categoryId = 3, // Bombones
                flavor = "Fresa",
                size = "Unidad",
                productionCost = BigDecimal("0.80"),
                wholesalePrice = BigDecimal("1.20"),
                retailPrice = BigDecimal("2.00"),
                minStockLevel = 50,
                isActive = true
            ),
            ProductRequest(
                code = "DULC-001",
                nameProduct = "Caramelos Surtidos",
                description = "Mezcla de caramelos de diferentes sabores",
                categoryId = 2, // Dulces
                flavor = "Mixto",
                size = "250g",
                productionCost = BigDecimal("1.50"),
                wholesalePrice = BigDecimal("2.50"),
                retailPrice = BigDecimal("4.00"),
                minStockLevel = 15,
                isActive = true
            ),
            ProductRequest(
                code = "TEMP-001",
                nameProduct = "Chocolate Navideño",
                description = "Chocolate especial con forma navideña",
                categoryId = 4, // Temporada
                flavor = "Dulce",
                size = "150g",
                productionCost = BigDecimal("3.00"),
                wholesalePrice = BigDecimal("5.00"),
                retailPrice = BigDecimal("8.00"),
                minStockLevel = 10,
                isActive = true
            )
        )

        products.forEach { productRequest ->
            try {
                productService.save(productRequest)
                logger.info("✅ Producto creado: ${productRequest.nameProduct}")
            } catch (e: Exception) {
                logger.warn("⚠️ Producto ya existe o error: ${productRequest.nameProduct}")
            }
        }
    }
}