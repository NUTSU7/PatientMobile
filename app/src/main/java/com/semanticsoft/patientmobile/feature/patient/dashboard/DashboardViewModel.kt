package com.semanticsoft.patientmobile.feature.patient.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semanticsoft.patientmobile.domain.model.patient.AiSummary
import com.semanticsoft.patientmobile.domain.model.patient.BasicIndicatorItem as UiBasicIndicatorItem
import com.semanticsoft.patientmobile.domain.model.patient.ClinicalPillarCardItem as UiClinicalPillarCardItem
import com.semanticsoft.patientmobile.domain.model.patient.GeneralMarkerCardItem as UiGeneralMarkerCardItem
import com.semanticsoft.patientmobile.domain.model.patient.MarkerCategoryItem as UiMarkerCategoryItem
import com.semanticsoft.patientmobile.domain.model.patient.MedicalResult
import com.semanticsoft.patientmobile.domain.model.patient.ProcessingOutcome
import com.semanticsoft.patientmobile.domain.model.patient.UiAttentionItem
import com.semanticsoft.patientmobile.domain.model.patient.UiMarkerSummary
import com.semanticsoft.patientmobile.domain.model.patient.WarningCardItem as UiWarningCardItem
import com.semanticsoft.patientmobile.domain.repository.common.AuthRepository
import com.semanticsoft.patientmobile.domain.repository.common.GlobalSyncManager
import com.semanticsoft.patientmobile.domain.repository.patient.DashboardRepository
import com.semanticsoft.patientmobile.domain.repository.patient.DocumentRepository
import com.semanticsoft.patientmobile.domain.repository.patient.MedicalResultRepository
import com.semanticsoft.patientmobile.domain.repository.patient.ProcessingCoordinator
import com.semanticsoft.patientmobile.domain.usecase.common.LogoutUseCase
import com.semanticsoft.patientmobile.domain.usecase.patient.BuildDashboardUseCase
import com.semanticsoft.patientmobile.domain.usecase.patient.DashboardCardData
import com.semanticsoft.patientmobile.domain.usecase.patient.LoadIndicatorHistoryUseCase
import com.semanticsoft.patientmobile.core.util.ApiResult
import com.semanticsoft.patientmobile.core.util.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class DashboardUiState(
    val greetingName: String = "",
    val fullName: String = "",
    val role: String = "",
    val profilePhotoResId: Int? = null,
    val hasUploadedDocuments: Boolean = false,
    val lastAnalysisDate: String = "",
    val attentionItems: List<UiAttentionItem> = emptyList(),
    val basicIndicators: List<UiBasicIndicatorItem> = emptyList(),
    val markerCategories: List<UiMarkerCategoryItem> = emptyList(),
    val generalMarkerCards: List<UiGeneralMarkerCardItem> = emptyList(),
    val markerSummary: UiMarkerSummary = UiMarkerSummary(0, 0, 0, 0),
    val documentCount: Int = 0,
    val aiSummary: String = "",
    val isAiSummaryLoading: Boolean = false,
    val warningCards: List<UiWarningCardItem> = emptyList(),
    val clinicalPillarCards: List<UiClinicalPillarCardItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val emptyReason: DashboardEmptyReason? = null
)

enum class DashboardEmptyReason {
    NO_DOCUMENTS,
    PROCESSING,
    NO_RESULTS_EXTRACTED,
    NON_LAB_DOCUMENT
}

sealed class DashboardEvent {
    data object RefreshCompleted : DashboardEvent()
    data class RefreshFailed(val message: String) : DashboardEvent()
    data object LogoutSuccess : DashboardEvent()
    data class LogoutFailure(val message: String) : DashboardEvent()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val dashboardRepository: DashboardRepository,
    private val medicalResultRepository: MedicalResultRepository,
    private val documentRepository: DocumentRepository,
    private val globalSyncManager: GlobalSyncManager,
    private val processingCoordinator: ProcessingCoordinator,
    private val buildDashboardUseCase: BuildDashboardUseCase,
    private val loadIndicatorHistoryUseCase: LoadIndicatorHistoryUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _events = MutableSharedFlow<DashboardEvent>()
    val events: SharedFlow<DashboardEvent> = _events.asSharedFlow()

    private var refreshJob: Job? = null
    private var aiJob: Job? = null
    private var lastResultsSignature: String? = null
    private var aiRequestToken = 0L

    private val _state = MutableStateFlow(DashboardUiState(isLoading = true))
    val state: StateFlow<DashboardUiState> = combine(
        _state,
        processingCoordinator.status
    ) { internalState, processing ->
        val base = if (processing.isProcessing) {
            internalState.copy(isAiSummaryLoading = internalState.aiSummary.isBlank())
        } else {
            internalState
        }
        if (processing.isProcessing &&
            (base.emptyReason != null || !base.hasUploadedDocuments)
        ) {
            base.copy(
                isLoading = false,
                emptyReason = if (processing.isTimedOut) {
                    DashboardEmptyReason.NO_RESULTS_EXTRACTED
                } else {
                    DashboardEmptyReason.PROCESSING
                },
                hasUploadedDocuments = true
            )
        } else {
            base
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState(isLoading = true))

    init {
        refresh()
        viewModelScope.launch {
            globalSyncManager.syncEvents.collect { refresh() }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                logoutUseCase()
                resetTransientState()
                _events.emit(DashboardEvent.LogoutSuccess)
            } catch (e: Exception) {
                val message = e.message ?: "Deconectarea a e\u0219uat."
                _events.emit(DashboardEvent.LogoutFailure(message))
            }
        }
    }

    private fun resetTransientState() {
        processingCoordinator.reset()
        lastResultsSignature = null
    }

    fun refresh() {
        refreshJob?.cancel()
        aiRequestToken++
        aiJob?.cancel()
        aiJob = null
        refreshJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val userDeferred = async { authRepository.getCurrentUser() }
            val resultsDeferred = async { medicalResultRepository.getLatestResults() }
            val docsDeferred = async { documentRepository.getDocumentStats() }

            val userResult = userDeferred.await()
            val resultsResult = resultsDeferred.await()
            val docsResult = docsDeferred.await()

            val docsTotal = if (docsResult is ApiResult.Success) docsResult.data.totalCount else 0
            val hasDocuments = docsTotal > 0
            val hasResults = resultsResult is ApiResult.Success && resultsResult.data.isNotEmpty()
            val processing = processingCoordinator.status.value

            var greetingName = ""
            var fullName = ""
            var role = "Pacient"

            if (userResult is ApiResult.Success) {
                val user = userResult.data
                greetingName = user.firstName.ifBlank { user.email.substringBefore("@") }
                fullName = listOfNotNull(user.firstName, user.lastName)
                    .filter { it.isNotBlank() }
                    .joinToString(" ").ifBlank { user.email }
                role = user.role.displayLabel.ifBlank { "Pacient" }
            }

            if (!hasDocuments && !hasResults && !processing.isProcessing) {
                val failure = listOf(userResult, resultsResult, docsResult)
                    .firstOrNull { it !is ApiResult.Success }
                if (failure != null) {
                    val message = failure.toUserMessage()
                    _state.update {
                        it.copy(
                            greetingName = greetingName.ifBlank { it.greetingName },
                            fullName = fullName.ifBlank { it.fullName },
                            role = role,
                            isLoading = false,
                            errorMessage = message
                        )
                    }
                    _events.emit(DashboardEvent.RefreshFailed(message))
                    return@launch
                }
                _state.update {
                    it.copy(
                        greetingName = greetingName.ifBlank { "Pacient" },
                        fullName = fullName,
                        role = role,
                        hasUploadedDocuments = false,
                        documentCount = 0,
                        isLoading = false,
                        errorMessage = null,
                        emptyReason = DashboardEmptyReason.NO_DOCUMENTS
                    )
                }
                _events.emit(DashboardEvent.RefreshCompleted)
                return@launch
            }

            if (!hasResults) {
                val checkReason = if (processing.lastOutcome == ProcessingOutcome.NON_LAB) {
                    DashboardEmptyReason.NON_LAB_DOCUMENT
                } else {
                    DashboardEmptyReason.NO_RESULTS_EXTRACTED
                }
                _state.update {
                    it.copy(
                        greetingName = greetingName.ifBlank { "Pacient" },
                        fullName = fullName,
                        role = role,
                        hasUploadedDocuments = true,
                        documentCount = docsTotal,
                        isLoading = false,
                        errorMessage = null,
                        emptyReason = checkReason
                    )
                }
                _events.emit(DashboardEvent.RefreshCompleted)
                return@launch
            }

            val results = if (resultsResult is ApiResult.Success) resultsResult.data else emptyList()

            val aiResult = dashboardRepository.getAiSummary()
            val signature = buildDashboardUseCase.buildResultsSignature(results, aiResult, docsTotal)
            if (signature == lastResultsSignature && _state.value.hasUploadedDocuments) {
                _state.update {
                    it.copy(
                        greetingName = greetingName,
                        fullName = fullName,
                        role = role,
                        documentCount = docsTotal,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                _events.emit(DashboardEvent.RefreshCompleted)
                return@launch
            }
            lastResultsSignature = signature

            val historyResults = loadIndicatorHistoryUseCase()
            val indicators = buildDashboardUseCase.buildBasicIndicators(
                if (historyResults.isNotEmpty()) historyResults else results
            )

            buildCardsFromResults(greetingName, fullName, role, indicators, buildDashboardUseCase(results), aiResult, docsTotal)

            _events.emit(DashboardEvent.RefreshCompleted)
        }
    }

    fun regenerateAiSummary() {
        requestAiSummary()
    }

    private fun requestAiSummary() {
        if (aiJob?.isActive == true) return
        val token = ++aiRequestToken
        aiJob = viewModelScope.launch {
            _state.update { it.copy(isAiSummaryLoading = true) }
            var attempts = 0
            while (attempts < MAX_AI_RETRIES) {
                if (token != aiRequestToken) return@launch
                val result = dashboardRepository.regenerateAiSummary()
                if (token != aiRequestToken) return@launch
                if (result !is ApiResult.Success) {
                    _state.update { it.copy(isAiSummaryLoading = false) }
                    return@launch
                }
                if (result.data.status.uppercase() in READY_AI_STATUSES) {
                    _state.update {
                        it.copy(aiSummary = result.data.summaryText, isAiSummaryLoading = false)
                    }
                    return@launch
                }
                attempts++
                delay(5_000L)
            }
            if (token == aiRequestToken) {
                _state.update { it.copy(isAiSummaryLoading = false) }
            }
        }
    }

    private fun buildCardsFromResults(
        greetingName: String,
        fullName: String,
        role: String,
        indicators: List<UiBasicIndicatorItem>,
        cards: DashboardCardData,
        aiResult: ApiResult<AiSummary>,
        documentCount: Int
    ) {
        var aiText = ""
        var aiLoading = false

        val processingState = processingCoordinator.status.value
        val aiOwnedByProcessing = processingState.isProcessing || processingState.aiRegenerationInFlight
        val currentSummary = _state.value.aiSummary

        when {
            aiResult is ApiResult.Success &&
                aiResult.data.status.uppercase() in READY_AI_STATUSES &&
                !processingState.isProcessing -> aiText = aiResult.data.summaryText

            currentSummary.isNotBlank() -> aiText = currentSummary

            !processingState.isProcessing -> {
                aiLoading = true
                if (!aiOwnedByProcessing) requestAiSummary()
            }

            else -> Unit
        }

        _state.update {
            it.copy(
                greetingName = greetingName,
                fullName = fullName,
                role = role,
                hasUploadedDocuments = true,
                attentionItems = cards.attentionItems,
                basicIndicators = indicators,
                markerCategories = cards.markerCategories,
                generalMarkerCards = emptyList(),
                markerSummary = cards.markerSummary,
                documentCount = documentCount,
                aiSummary = aiText,
                isAiSummaryLoading = aiLoading,
                warningCards = cards.warningCards,
                clinicalPillarCards = cards.clinicalPillarCards,
                isLoading = false,
                errorMessage = null,
                emptyReason = null
            )
        }
    }

    companion object {
        private const val MAX_AI_RETRIES = 12
        private val READY_AI_STATUSES = setOf("READY", "COMPLETED")
    }
}
