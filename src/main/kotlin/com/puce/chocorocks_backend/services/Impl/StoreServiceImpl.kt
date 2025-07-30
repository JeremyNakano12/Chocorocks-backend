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

@Service
@Transactional
class StoreServiceImpl(
    private val storeRepository: StoreRepository,
    private val userRepository: UserRepository
) : StoreService {

    override fun findAll(): List<StoreResponse> =
        storeRepository.findAll().map { StoreMapper.toResponse(it) }

    override fun findById(id: Long): StoreResponse {
        val store = storeRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Tienda",
                    identifier = id,
                    detalles = listOf("Verifique que el ID de la tienda sea correcto")
                )
            }
        return StoreMapper.toResponse(store)
    }

    override fun save(request: StoreRequest): StoreResponse {
        validateStoreData(request)

        val nameExists = storeRepository.existsByName(request.name)
        if (nameExists) {
            throw DuplicateResourceException(
                resourceName = "Tienda",
                field = "nombre",
                value = request.name,
                detalles = listOf("El nombre de la tienda debe ser único")
            )
        }

        val manager = request.managerId?.let {
            userRepository.findById(it)
                .orElseThrow {
                    ResourceNotFoundException(
                        resourceName = "Usuario",
                        identifier = it,
                        detalles = listOf("Seleccione un manager válido")
                    )
                }
        }

        validateSchedule(request)

        val store = StoreMapper.toEntity(request, manager)
        val savedStore = storeRepository.save(store)
        return StoreMapper.toResponse(savedStore)
    }

    override fun update(id: Long, request: StoreRequest): StoreResponse {
        val existingStore = storeRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Tienda",
                    identifier = id,
                    detalles = listOf("No se puede actualizar una tienda que no existe")
                )
            }

        validateStoreData(request)

        val nameExists = storeRepository.findAll()
            .any { it.name == request.name && it.id != id }
        if (nameExists) {
            throw DuplicateResourceException(
                resourceName = "Tienda",
                field = "nombre",
                value = request.name,
                detalles = listOf("Ya existe otra tienda con este nombre")
            )
        }

        val manager = request.managerId?.let {
            userRepository.findById(it)
                .orElseThrow {
                    ResourceNotFoundException(
                        resourceName = "Usuario",
                        identifier = it,
                        detalles = listOf("Seleccione un manager válido")
                    )
                }
        }

        validateSchedule(request)

        val updatedStore = Store(
            name = request.name,
            address = request.address,
            manager = manager,
            typeStore = request.typeStore,
            phoneNumber = request.phoneNumber,
            scheduleOpen = request.scheduleOpen,
            scheduleClosed = request.scheduleClosed,
            isActive = request.isActive
        ).apply { this.id = existingStore.id }

        val savedStore = storeRepository.save(updatedStore)
        return StoreMapper.toResponse(savedStore)
    }

    override fun delete(id: Long) {
        val store = storeRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(
                    resourceName = "Tienda",
                    identifier = id,
                    detalles = listOf("No se puede eliminar una tienda que no existe")
                )
            }

        try {
            storeRepository.deleteById(id)
        } catch (ex: Exception) {
            throw InvalidOperationException(
                operation = "eliminar la tienda '${store.name}'",
                reason = "tiene ventas, productos o movimientos asociados",
                detalles = listOf("No se puede eliminar una tienda con historial en el sistema")
            )
        }
    }

    private fun validateStoreData(request: StoreRequest) {
        if (request.name.isBlank()) {
            throw BusinessValidationException(
                message = "El nombre de la tienda no puede estar vacío",
                detalles = listOf("Proporcione un nombre descriptivo para la tienda")
            )
        }

        if (request.address.isBlank()) {
            throw BusinessValidationException(
                message = "La dirección de la tienda no puede estar vacía",
                detalles = listOf("Proporcione una dirección válida")
            )
        }

        request.phoneNumber?.let { phone ->
            if (phone.isNotBlank() && phone.length < 7) {
                throw BusinessValidationException(
                    message = "Formato de teléfono inválido",
                    detalles = listOf("El teléfono debe tener al menos 7 dígitos")
                )
            }
        }
    }

    private fun validateSchedule(request: StoreRequest) {
        val openTime = request.scheduleOpen
        val closeTime = request.scheduleClosed

        if (openTime != null && closeTime != null) {
            if (openTime.isAfter(closeTime)) {
                throw BusinessValidationException(
                    message = "La hora de apertura no puede ser posterior a la hora de cierre",
                    detalles = listOf("Apertura: $openTime, Cierre: $closeTime")
                )
            }
        }

        if ((openTime != null && closeTime == null) || (openTime == null && closeTime != null)) {
            throw BusinessValidationException(
                message = "Debe proporcionar tanto la hora de apertura como la de cierre",
                detalles = listOf("Complete ambos horarios o deje ambos vacíos")
            )
        }
    }
}

