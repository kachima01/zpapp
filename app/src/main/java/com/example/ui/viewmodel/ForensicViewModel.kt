package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.DecisionRecordEntity
import com.example.data.model.OfficerEntity
import com.example.data.model.StationToolEntity
import com.example.data.repository.ForensicRepository
import com.example.engine.AssessmentInputs
import com.example.engine.CaseToolAllocator
import com.example.engine.ToolAllocation
import com.example.engine.ZcmffDecisionEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CaseFormState(
    val caseNumber: String = "",
    val incidentReference: String = "",
    val crimeType: String = "Mobile Money Fraud",
    val deviceType: String = "Smartphone / Mobile",
    // ── Location & suspect ──────────────────────────────────────────────────
    val incidentLocation: String = "Lusaka Central",
    val suspectInfo: String = "",
    // ── Device identification ────────────────────────────────────────────────
    val deviceMake: String = "Samsung",
    val deviceModel: String = "Galaxy Device",
    val deviceImeiOrSerial: String = "",
    // ── Device technical state ───────────────────────────────────────────────
    val osType: String = "Android",
    val osVersion: String = "14.0",
    val lockStatus: String = "Passcode Locked",
    val encryptionType: String = "File-Based (FBE) + Secure Enclave",
    val cloudDependency: String = "Cloud Backups Detected",
    // ── Examiner & legal ─────────────────────────────────────────────────────
    val competenceLevel: String = "Intermediate",
    val warrantType: String = "Full Search Warrant (Sec 118 CPC)",
    val warrantReference: String = "",
    val cloudScopeAuthorized: Boolean = true,
    // ── Tool selection ───────────────────────────────────────────────────────
    val selectedTools: Set<String> = emptySet(),
    // ── Examiner identity ────────────────────────────────────────────────────
    val examinerName: String = "",
    val examinerBadge: String = "",
    val examinerRank: String = "Detective Inspector",
    val stationUnit: String = "Forensic Laboratory Unit, Police HQ Lusaka",
    // ── Chain-of-custody checks ──────────────────────────────────────────────
    val isFaradayIsolated: Boolean = false,
    val isAirplaneModeConfirmed: Boolean = false,
    val isWriteBlockerAttached: Boolean = false
)

class ForensicViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ForensicRepository

    private val _currentOfficer = MutableStateFlow<OfficerEntity?>(null)
    val currentOfficer: StateFlow<OfficerEntity?> = _currentOfficer.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _caseFormState = MutableStateFlow(CaseFormState())
    val caseFormState: StateFlow<CaseFormState> = _caseFormState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterCategory = MutableStateFlow("ALL")
    val filterCategory: StateFlow<String> = _filterCategory.asStateFlow()

    val allTools: StateFlow<List<StationToolEntity>>
    val availableTools: StateFlow<List<StationToolEntity>>
    val allRecords: StateFlow<List<DecisionRecordEntity>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ForensicRepository(
            decisionRecordDao = database.decisionRecordDao(),
            stationToolDao = database.stationToolDao(),
            officerDao = database.officerDao()
        )

        allTools = repository.allTools
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        availableTools = repository.availableTools
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allRecords = repository.allRecords
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        viewModelScope.launch {
            repository.ensureDefaultToolsSeeded()
            // Auto-login to default field investigator for immediate accessibility
            val defaultOfficer = repository.getOfficer("ZP-8842") ?: OfficerEntity(
                officerId = "ZP-8842",
                fullName = "Officer Amon Bili",
                role = "Investigator",
                password = "password",
                station = "Forensic Laboratory Unit, Police HQ Lusaka"
            )
            repository.registerOfficer(defaultOfficer)
            _currentOfficer.value = defaultOfficer
            _isLoggedIn.value = true
            initNewCaseForm()
        }
    }

    val filteredRecords: StateFlow<List<DecisionRecordEntity>> = combine(
        allRecords,
        _searchQuery,
        _filterCategory
    ) { records, query, categoryFilter ->
        records.filter { record ->
            val matchesQuery = if (query.isBlank()) true else {
                record.caseNumber.contains(query, ignoreCase = true) ||
                        record.deviceImeiOrSerial.contains(query, ignoreCase = true) ||
                        record.deviceMake.contains(query, ignoreCase = true) ||
                        record.deviceModel.contains(query, ignoreCase = true) ||
                        record.officerName.contains(query, ignoreCase = true) ||
                        record.crimeType.contains(query, ignoreCase = true) ||
                        record.recommendedMethod.contains(query, ignoreCase = true)
            }
            val matchesFilter = when (categoryFilter) {
                "ALL" -> true
                "MOBILE" -> record.deviceType.contains("Mobile", ignoreCase = true) || record.deviceType.contains("Smartphone", ignoreCase = true)
                "COMPUTER" -> record.deviceType.contains("Computer", ignoreCase = true) || record.deviceType.contains("HDD", ignoreCase = true)
                "CLOUD" -> record.cloudScopeAuthorized || record.deviceType.contains("Cloud", ignoreCase = true)
                "RAM" -> record.deviceType.contains("Volatile", ignoreCase = true) || record.deviceType.contains("RAM", ignoreCase = true)
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun login(officerId: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val trimmedId = officerId.trim().ifBlank { "ZP-8842" }
            var officer = repository.getOfficer(trimmedId)
            if (officer == null) {
                officer = OfficerEntity(
                    officerId = trimmedId,
                    fullName = "Officer $trimmedId",
                    role = if (trimmedId.contains("1001")) "Supervisor" else "Investigator",
                    password = pass.trim().ifBlank { "password" }
                )
                repository.registerOfficer(officer)
            }
            _currentOfficer.value = officer
            _isLoggedIn.value = true
            initNewCaseForm()
            onResult(true, null)
        }
    }

    fun quickLoginAs(role: String) {
        viewModelScope.launch {
            val officerId = if (role == "Supervisor") "ZP-1001" else "ZP-8842"
            val fullName = if (role == "Supervisor") "Senior Inspector Banda" else "Officer Amon Bili"
            val officer = repository.getOfficer(officerId) ?: OfficerEntity(
                officerId = officerId,
                fullName = fullName,
                role = role,
                password = "password",
                station = if (role == "Supervisor") "Forensic Oversight Division, Police HQ Lusaka" else "Forensic Laboratory Unit, Police HQ Lusaka"
            )
            repository.registerOfficer(officer)
            _currentOfficer.value = officer
            _isLoggedIn.value = true
            initNewCaseForm()
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        _currentOfficer.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterCategory(category: String) {
        _filterCategory.value = category
    }

    fun initNewCaseForm() {
        val officer = _currentOfficer.value
        val autoCaseNumber = "ZP-CASE-${System.currentTimeMillis() % 100000}"
        val initialDevice = "Smartphone / Mobile"
        val initialCrime = "Mobile Money Fraud"
        val allocated = CaseToolAllocator.getAllocatedToolsForCase(
            deviceType = initialDevice,
            crimeType = initialCrime,
            isLocked = true,
            cloudAuthorized = true
        ).filter { it.isPrimaryAllocation }.map { it.toolName }.toSet()

        _caseFormState.value = CaseFormState(
            caseNumber = autoCaseNumber,
            incidentReference = "DOCKET-${(System.currentTimeMillis() / 1000) % 10000}",
            crimeType = initialCrime,
            deviceType = initialDevice,
            deviceMake = "Samsung",
            deviceModel = "Galaxy S22",
            deviceImeiOrSerial = "",
            warrantReference = "",
            examinerName = officer?.fullName ?: "Officer Amon Bili",
            examinerBadge = officer?.officerId ?: "ZP-8842",
            examinerRank = if (officer?.role == "Supervisor") "Senior Detective Inspector" else "Detective Inspector",
            selectedTools = allocated
        )
    }

    fun updateCaseForm(
        caseNumber: String = _caseFormState.value.caseNumber,
        incidentRef: String = _caseFormState.value.incidentReference,
        crimeType: String = _caseFormState.value.crimeType,
        deviceType: String = _caseFormState.value.deviceType,
        incidentLocation: String = _caseFormState.value.incidentLocation,
        suspectInfo: String = _caseFormState.value.suspectInfo,
        deviceMake: String = _caseFormState.value.deviceMake,
        deviceModel: String = _caseFormState.value.deviceModel,
        imeiOrSerial: String = _caseFormState.value.deviceImeiOrSerial,
        osType: String = _caseFormState.value.osType,
        osVersion: String = _caseFormState.value.osVersion,
        lockStatus: String = _caseFormState.value.lockStatus,
        encryptionType: String = _caseFormState.value.encryptionType,
        cloudDependency: String = _caseFormState.value.cloudDependency,
        competenceLevel: String = _caseFormState.value.competenceLevel,
        warrantType: String = _caseFormState.value.warrantType,
        warrantReference: String = _caseFormState.value.warrantReference,
        cloudAuthorized: Boolean = _caseFormState.value.cloudScopeAuthorized,
        selectedTools: Set<String>? = null,
        isFaradayIsolated: Boolean = _caseFormState.value.isFaradayIsolated,
        isAirplaneModeConfirmed: Boolean = _caseFormState.value.isAirplaneModeConfirmed,
        isWriteBlockerAttached: Boolean = _caseFormState.value.isWriteBlockerAttached
    ) {
        val isLocked = !lockStatus.contains("Unlocked", ignoreCase = true)
        val currentTools = selectedTools ?: run {
            // Recalculate allocated tools when device or crime changes
            CaseToolAllocator.getAllocatedToolsForCase(
                deviceType = deviceType,
                crimeType = crimeType,
                isLocked = isLocked,
                cloudAuthorized = cloudAuthorized
            ).filter { it.isPrimaryAllocation }.map { it.toolName }.toSet()
        }

        _caseFormState.value = _caseFormState.value.copy(
            caseNumber = caseNumber,
            incidentReference = incidentRef,
            crimeType = crimeType,
            deviceType = deviceType,
            incidentLocation = incidentLocation,
            suspectInfo = suspectInfo,
            deviceMake = deviceMake,
            deviceModel = deviceModel,
            deviceImeiOrSerial = imeiOrSerial,
            osType = osType,
            osVersion = osVersion,
            lockStatus = lockStatus,
            encryptionType = encryptionType,
            cloudDependency = cloudDependency,
            competenceLevel = competenceLevel,
            warrantType = warrantType,
            warrantReference = warrantReference,
            cloudScopeAuthorized = cloudAuthorized,
            selectedTools = currentTools,
            isFaradayIsolated = isFaradayIsolated,
            isAirplaneModeConfirmed = isAirplaneModeConfirmed,
            isWriteBlockerAttached = isWriteBlockerAttached
        )
    }

    fun toggleToolInCase(toolName: String) {
        val current = _caseFormState.value.selectedTools.toMutableSet()
        if (current.contains(toolName)) {
            current.remove(toolName)
        } else {
            current.add(toolName)
        }
        _caseFormState.value = _caseFormState.value.copy(selectedTools = current)
    }

    fun getAllocatedToolsForCurrentForm(): List<ToolAllocation> {
        val state = _caseFormState.value
        val isLocked = !state.lockStatus.contains("Unlocked", ignoreCase = true)
        return CaseToolAllocator.getAllocatedToolsForCase(
            deviceType = state.deviceType,
            crimeType = state.crimeType,
            isLocked = isLocked,
            cloudAuthorized = state.cloudScopeAuthorized
        )
    }

    /**
     * Builds AssessmentInputs for the ZcmffDecisionEngine using only the tools
     * that are currently marked *available* in the station inventory. This ensures
     * that toggling a tool off in Tool Inventory actually affects recommendations.
     */
    private fun buildAssessmentInputs(
        state: CaseFormState,
        liveAvailableTools: List<StationToolEntity>
    ): AssessmentInputs {
        // Intersect the investigator-selected tools with live-available station tools
        val availableToolNames = liveAvailableTools.map { it.name }
        val effectiveTools = state.selectedTools.filter { selected ->
            availableToolNames.any { avail -> avail.contains(selected, ignoreCase = true) || selected.contains(avail, ignoreCase = true) }
        }
        return AssessmentInputs(
            selectedTools = effectiveTools,
            osType = state.osType,
            osVersion = state.osVersion,
            lockStatus = state.lockStatus,
            encryptionType = state.encryptionType,
            cloudDependency = state.cloudDependency,
            competenceLevel = state.competenceLevel,
            warrantType = state.warrantType,
            warrantReference = state.warrantReference,
            cloudScopeAuthorized = state.cloudScopeAuthorized
        )
    }

    // Direct SQLite Saving with immediate callback
    fun saveCaseRecord(onSaved: (Long) -> Unit) {
        val state = _caseFormState.value
        val officer = _currentOfficer.value
        val liveAvailableTools = availableTools.value

        val allocated = getAllocatedToolsForCurrentForm()
        val allocatedNames = allocated.map { it.toolName }
        val selected = state.selectedTools.ifEmpty { allocatedNames.toSet() }

        // ── Run the real ZcmffDecisionEngine ──────────────────────────────────
        val inputs = buildAssessmentInputs(state, liveAvailableTools)
        val engineResult = ZcmffDecisionEngine.evaluate(inputs)

        // Serialise rejected alternatives for storage
        val rejectedRaw = engineResult.rejectedAlternatives.joinToString(" | ") { alt ->
            "${alt.methodName}: ${alt.primaryReason} [${alt.technicalBarrier}]"
        }
        val cautionsRaw = engineResult.cautionaryGuidance.joinToString(" | ")

        val entity = DecisionRecordEntity(
            caseNumber = state.caseNumber.ifBlank { "ZP-CASE-${System.currentTimeMillis() % 100000}" },
            incidentReference = state.incidentReference.ifBlank { "DOCKET-2025" },
            crimeType = state.crimeType,
            deviceType = state.deviceType,
            incidentLocation = state.incidentLocation,
            suspectInfo = state.suspectInfo,
            deviceMake = state.deviceMake.ifBlank { "Unknown Make" },
            deviceModel = state.deviceModel.ifBlank { "Device" },
            deviceImeiOrSerial = state.deviceImeiOrSerial.ifBlank { "N/A" },
            osType = state.osType,
            osVersion = state.osVersion,
            lockStatus = state.lockStatus,
            encryptionType = state.encryptionType,
            cloudDependency = state.cloudDependency,
            competenceLevel = state.competenceLevel,
            warrantType = state.warrantType,
            warrantReference = state.warrantReference.ifBlank { "N/A" },
            cloudScopeAuthorized = state.cloudScopeAuthorized,
            allocatedToolsCsv = allocatedNames.joinToString(", "),
            selectedToolsCsv = selected.joinToString(", "),
            recommendedMethod = engineResult.recommendedMethod,
            recommendedToolCategory = engineResult.recommendedToolCategory,
            recommendedTools = engineResult.recommendedTools.joinToString(", "),
            primaryRationale = engineResult.rationale,
            rejectedAlternativesRaw = rejectedRaw,
            cautionaryGuidanceCsv = cautionsRaw,
            chainOfCustodyCompleted = state.isFaradayIsolated && state.isAirplaneModeConfirmed,
            officerName = state.examinerName.ifBlank { officer?.fullName ?: "Officer Amon Bili" },
            officerRank = state.examinerRank,
            officerBadge = state.examinerBadge.ifBlank { officer?.officerId ?: "ZP-8842" },
            stationUnit = state.stationUnit,
            timestamp = System.currentTimeMillis(),
            supervisorStatus = "PENDING_REVIEW",
            supervisorNotes = "",
            supervisorReviewedAt = 0L
        )

        viewModelScope.launch {
            val recordId = repository.insertDecisionRecord(entity)
            onSaved(recordId)
        }
    }

    suspend fun getRecordByIdDirect(recordId: Long): DecisionRecordEntity? {
        return repository.getRecordByIdDirect(recordId)
    }

    fun updateSupervisorReview(recordId: Long, status: String, notes: String) {
        viewModelScope.launch {
            val existing = repository.getRecordByIdDirect(recordId) ?: return@launch
            val updated = existing.copy(
                supervisorStatus = status,
                supervisorNotes = notes,
                supervisorReviewedAt = System.currentTimeMillis()
            )
            repository.updateDecisionRecord(updated)
        }
    }

    fun deleteRecord(recordId: Long) {
        viewModelScope.launch {
            repository.deleteDecisionRecord(recordId)
        }
    }

    fun toggleStationTool(id: Long, isAvailable: Boolean) {
        viewModelScope.launch {
            repository.setToolAvailability(id, isAvailable)
        }
    }

    fun updateStationToolDetails(tool: StationToolEntity) {
        viewModelScope.launch {
            repository.updateTool(tool)
        }
    }
}
