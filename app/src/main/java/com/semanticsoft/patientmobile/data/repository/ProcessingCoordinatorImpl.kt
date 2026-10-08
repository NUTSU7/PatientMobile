package com.semanticsoft.patientmobile.data.repository

import com.semanticsoft.patientmobile.di.ApplicationScope
import com.semanticsoft.patientmobile.domain.model.OcrStatus
import com.semanticsoft.patientmobile.domain.model.ProcessingOutcome
import com.semanticsoft.patientmobile.domain.model.ProcessingStatus
import com.semanticsoft.patientmobile.domain.repository.DashboardRepository
import com.semanticsoft.patientmobile.domain.repository.DocumentRepository
import com.semanticsoft.patientmobile.domain.repository.ExtractionTarget
import com.semanticsoft.patientmobile.domain.repository.GlobalSyncManager
import com.semanticsoft.patientmobile.domain.repository.OcrRepository
import com.semanticsoft.patientmobile.domain.repository.ProcessingCoordinator
import com.semanticsoft.patientmobile.util.ApiResult
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Singleton
class ProcessingCoordinatorImpl @Inject constructor(
    private val ocrRepository: OcrRepository,
    private val documentRepository: DocumentRepository,
    private val dashboardRepository: DashboardRepository,
    private val globalSyncManager: GlobalSyncManager,
    @ApplicationScope private val scope: CoroutineScope
) : ProcessingCoordinator {

    private val _status = MutableStateFlow(ProcessingStatus())
    override val status: StateFlow<ProcessingStatus> = _status.asStateFlow()

    private val lock = Any()
    @Volatile
    private var activeJob: Job? = null
    private val auxJobs = mutableListOf<Job>()
    private val pendingAdditions = ArrayDeque<ExtractionTarget>()
    private val activeKeys = HashSet<String>()

    @Synchronized
    override fun start(targets: List<ExtractionTarget>) {
        if (targets.isEmpty()) return
        if (activeJob?.isActive == true) {
            merge(targets)
            return
        }
        beginBatch(targets)
    }

    override fun resumeIfPending() {
        scope.launch {
            if (activeJob?.isActive == true) return@launch
            val discovery = discoverPending()
            if (discovery.targets.isNotEmpty()) {
                beginBatch(discovery.targets)
            } else if (discovery.nonLab) {
                _status.update { it.copy(lastOutcome = ProcessingOutcome.NON_LAB) }
                globalSyncManager.triggerSync()
            }
        }
    }

    @Synchronized
    override fun reset() {
        activeJob?.cancel()
        activeJob = null
        auxJobs.forEach { it.cancel() }
        auxJobs.clear()
        synchronized(lock) {
            pendingAdditions.clear()
            activeKeys.clear()
        }
        _status.value = ProcessingStatus()
    }

    @Synchronized
    private fun beginBatch(targets: List<ExtractionTarget>) {
        if (activeJob?.isActive == true) {
            merge(targets)
            return
        }
        synchronized(lock) {
            pendingAdditions.clear()
            activeKeys.clear()
            targets.forEach { activeKeys.add(keyOf(it)) }
        }
        _status.value = ProcessingStatus(
            isProcessing = true,
            totalJobs = targets.size,
            message = BASE_MESSAGE
        )
        activeJob = scope.launch { runProcessing(targets) }
    }

    @Synchronized
    private fun merge(targets: List<ExtractionTarget>) {
        val newlyAdded = synchronized(lock) {
            var added = 0
            for (target in targets) {
                if (activeKeys.add(keyOf(target))) {
                    pendingAdditions.addLast(target)
                    added++
                }
            }
            added
        }
        if (newlyAdded > 0) {
            val total = activeKeys.size
            _status.update {
                it.copy(
                    isProcessing = true,
                    totalJobs = total,
                    message = buildMessage(
                        slow = it.isSlowWarning,
                        timedOut = it.isTimedOut,
                        done = it.completedJobs + it.failedJobs,
                        total = total
                    )
                )
            }
        }
    }

    private data class Discovery(
        val targets: List<ExtractionTarget>,
        val nonLab: Boolean
    )

    private suspend fun discoverPending(): Discovery {
        val targets = mutableListOf<ExtractionTarget>()
        var nonLab = false
        var page = 0
        while (page < MAX_DISCOVERY_PAGES) {
            val docsResult = documentRepository.getDocuments(
                page = page,
                size = DISCOVERY_PAGE_SIZE,
                sortBy = "uploadedAt",
                sortDir = "desc"
            )
            if (docsResult !is ApiResult.Success) break
            val docs = docsResult.data
            if (docs.isEmpty()) break
            for (doc in docs) {
                val extractions = ocrRepository.listExtractions(doc.id)
                if (extractions !is ApiResult.Success) continue
                for (run in extractions.data) {
                    if (run.status == OcrStatus.PENDING || run.status == OcrStatus.PROCESSING) {
                        targets += ExtractionTarget(doc.id, run.id)
                    }
                    if (run.reports.any { isNonLabReport(it.clinicalType) }) {
                        nonLab = true
                    }
                }
            }
            if (docs.size < DISCOVERY_PAGE_SIZE) break
            page++
        }
        return Discovery(targets, nonLab)
    }

    private suspend fun runProcessing(initial: List<ExtractionTarget>) {
        val states = HashMap<String, RunState>()
        val order = mutableListOf<ExtractionTarget>()
        val consecutiveErrors = HashMap<String, Int>()
        var nonLabReports = 0
        var labReports = 0

        fun register(target: ExtractionTarget) {
            val key = keyOf(target)
            if (!states.containsKey(key)) {
                states[key] = RunState.PENDING
                order.add(target)
            }
        }

        initial.forEach(::register)
        val startTime = System.currentTimeMillis()

        while (true) {
            val additions = synchronized(lock) {
                if (pendingAdditions.isEmpty()) {
                    emptyList()
                } else {
                    val snapshot = pendingAdditions.toList()
                    pendingAdditions.clear()
                    snapshot
                }
            }
            if (additions.isNotEmpty()) {
                additions.forEach(::register)
                _status.update { it.copy(totalJobs = states.size) }
            }

            for (target in order) {
                val key = keyOf(target)
                if (states[key] != RunState.PENDING) continue
                when (val result = ocrRepository.getExtractionStatus(target.documentId, target.runId)) {
                    is ApiResult.Success -> {
                        consecutiveErrors[key] = 0
                        val extraction = result.data
                        when (classify(extraction.status)) {
                            RunState.PENDING -> Unit
                            RunState.SUCCESS -> {
                                states[key] = RunState.SUCCESS
                                nonLabReports += extraction.reports.count { isNonLabReport(it.clinicalType) }
                                labReports += extraction.reports.count { !isNonLabReport(it.clinicalType) }
                            }
                            RunState.FAILED -> states[key] = RunState.FAILED
                        }
                    }
                    else -> {
                        val errors = (consecutiveErrors[key] ?: 0) + 1
                        consecutiveErrors[key] = errors
                        if (errors >= MAX_STATUS_ERRORS) {
                            states[key] = RunState.FAILED
                        }
                    }
                }
            }

            val completed = states.values.count { it == RunState.SUCCESS }
            val failed = states.values.count { it == RunState.FAILED }
            val pending = states.values.count { it == RunState.PENDING }
            val elapsed = System.currentTimeMillis() - startTime
            val slow = elapsed >= SLOW_WARNING_MS
            val timedOut = elapsed >= TIMEOUT_MS

            _status.update {
                it.copy(
                    completedJobs = completed,
                    failedJobs = failed,
                    nonLabReports = nonLabReports,
                    isSlowWarning = slow,
                    isTimedOut = timedOut,
                    message = buildMessage(slow, timedOut, completed + failed, states.size)
                )
            }

            if (pending == 0 || timedOut) break
            delay(POLL_DELAY_MS)
        }

        finalize(
            completed = states.values.count { it == RunState.SUCCESS },
            failed = states.values.count { it == RunState.FAILED },
            total = states.size,
            nonLabReports = nonLabReports,
            labReports = labReports,
            timedOut = _status.value.isTimedOut
        )

        synchronized(lock) { activeKeys.clear() }
    }

    private suspend fun finalize(
        completed: Int,
        failed: Int,
        total: Int,
        nonLabReports: Int,
        labReports: Int,
        timedOut: Boolean
    ) {
        val outcome = when {
            labReports > 0 -> ProcessingOutcome.COMPLETED
            nonLabReports > 0 -> ProcessingOutcome.NON_LAB
            else -> ProcessingOutcome.NO_RESULTS
        }

        if (timedOut) {
            globalSyncManager.triggerSync()
            _status.update { it.copy(isProcessing = false, lastOutcome = outcome) }
            synchronized(this) {
                auxJobs += scope.launch {
                    delay(TIMEOUT_REARM_DELAY_MS)
                    resumeIfPending()
                }
            }
            return
        }

        globalSyncManager.triggerSync()
        synchronized(this) {
            auxJobs += scope.launch {
                delay(FINAL_REFRESH_DELAY_MS)
                globalSyncManager.triggerSync()
            }
        }

        if (failed == 0 && completed == total) {
            _status.update { it.copy(aiRegenerationInFlight = true) }
            runCatching { dashboardRepository.regenerateAiSummary() }
            _status.update { it.copy(aiRegenerationInFlight = false) }
        }

        _status.update { it.copy(isProcessing = false, lastOutcome = outcome) }
    }

    private fun keyOf(target: ExtractionTarget): String =
        "${target.documentId}|${target.runId}"

    private fun classify(status: OcrStatus): RunState = when (status) {
        OcrStatus.PENDING, OcrStatus.PROCESSING -> RunState.PENDING
        OcrStatus.SUCCESS, OcrStatus.COMPLETED -> RunState.SUCCESS
        OcrStatus.PARTIAL, OcrStatus.FAILED -> RunState.FAILED
    }

    private fun isNonLabReport(clinicalType: String?): Boolean =
        (clinicalType ?: LAB_CLINICAL_TYPE) != LAB_CLINICAL_TYPE

    private fun buildMessage(slow: Boolean, timedOut: Boolean, done: Int, total: Int): String {
        val base = when {
            timedOut -> "Procesarea a durat prea mult. Po\u021Bi reveni mai t\u00E2rziu."
            slow -> "Procesarea dureaz\u0103 mai mult dec\u00E2t de obicei..."
            else -> BASE_MESSAGE
        }
        return "$base ($done din $total)"
    }

    private enum class RunState { PENDING, SUCCESS, FAILED }

    companion object {
        private const val BASE_MESSAGE = "Fi\u0219ierele s-au \u00EEnc\u0103rcat, iar acum se proceseaz\u0103..."
        private const val POLL_DELAY_MS = 5_000L
        private const val TIMEOUT_MS = 720_000L
        private const val SLOW_WARNING_MS = 180_000L
        private const val FINAL_REFRESH_DELAY_MS = 2_500L
        private const val TIMEOUT_REARM_DELAY_MS = 60_000L
        private const val MAX_STATUS_ERRORS = 3
        private const val DISCOVERY_PAGE_SIZE = 20
        private const val MAX_DISCOVERY_PAGES = 10
        private const val LAB_CLINICAL_TYPE = "LAB_RESULTS"
    }
}
