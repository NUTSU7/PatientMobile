package com.semanticsoft.patientmobile.data.repository

import com.semanticsoft.patientmobile.data.local.dao.MedicalResultDao
import com.semanticsoft.patientmobile.data.local.db.extensions.toDomain
import com.semanticsoft.patientmobile.data.local.db.extensions.toEntity
import com.semanticsoft.patientmobile.data.remote.api.PatientApiService
import com.semanticsoft.patientmobile.domain.model.MedicalResult
import com.semanticsoft.patientmobile.domain.repository.MedicalResultRepository
import com.semanticsoft.patientmobile.util.Resource
import com.semanticsoft.patientmobile.util.toUserMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MedicalResultRepositoryImpl(
    private val apiService: PatientApiService,
    private val medicalResultDao: MedicalResultDao,
    private val networkStateProvider: NetworkStateProvider = AlwaysOnlineStateProvider
) : MedicalResultRepository {

    override fun getByDocumentId(docId: String): Flow<Resource<List<MedicalResult>>> = flow {
        emit(Resource.Loading)

        val cached = medicalResultDao.getAllByDocumentId(docId).map { it.toDomain() }
        if (!networkStateProvider.isOnline()) {
            emit(Resource.Success(cached))
            return@flow
        }

        runCatching {
            val remote = apiService.getMedicalResults(docId)
            medicalResultDao.deleteByDocumentId(docId)
            medicalResultDao.insertAll(remote.map { it.toEntity() })
            remote
        }.onSuccess {
            emit(Resource.Success(it))
        }.onFailure {
            if (cached.isNotEmpty()) {
                emit(Resource.Success(cached))
            } else {
                emit(Resource.Error(it.toUserMessage()))
            }
        }
    }

    override suspend fun sync(docId: String): Resource<List<MedicalResult>> {
        if (!networkStateProvider.isOnline()) {
            val cached = medicalResultDao.getAllByDocumentId(docId).map { it.toDomain() }
            return if (cached.isNotEmpty()) {
                Resource.Success(cached)
            } else {
                Resource.Error("Cannot sync medical results while offline.")
            }
        }

        return runCatching {
            val remote = apiService.getMedicalResults(docId)
            medicalResultDao.deleteByDocumentId(docId)
            medicalResultDao.insertAll(remote.map { it.toEntity() })
            remote
        }.fold(
            onSuccess = { Resource.Success(it) },
            onFailure = { Resource.Error(it.toUserMessage()) }
        )
    }
}
