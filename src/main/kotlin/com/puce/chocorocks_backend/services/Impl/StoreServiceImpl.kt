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
            .orElseThrow { EntityNotFoundException("Tienda con ID $id no encontrada") }
        return StoreMapper.toResponse(store)
    }

    override fun save(request: StoreRequest): StoreResponse {
        val manager = request.managerId?.let {
            userRepository.findById(it)
                .orElseThrow { EntityNotFoundException("Manager con ID $it no encontrado") }
        }

        val store = StoreMapper.toEntity(request, manager)
        val savedStore = storeRepository.save(store)
        return StoreMapper.toResponse(savedStore)
    }

    override fun update(id: Long, request: StoreRequest): StoreResponse {
        val existingStore = storeRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Tienda con ID $id no encontrada") }

        val manager = request.managerId?.let {
            userRepository.findById(it)
                .orElseThrow { EntityNotFoundException("Manager con ID $it no encontrado") }
        }

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
        if (!storeRepository.existsById(id)) {
            throw EntityNotFoundException("Tienda con ID $id no encontrada")
        }
        storeRepository.deleteById(id)
    }
}
