package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.DecisionRecordEntity
import com.example.data.model.OfficerEntity
import com.example.data.model.StationToolEntity
import com.example.data.repository.ForensicRepository
import com.example.engine.CaseToolAllocator
import com.example.engine.ToolAllocation
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
    val incidentLocation: String = "Lusaka Central",
    val suspectInfo: String = "",
    val deviceMake: String = "Samsung",
    val deviceModel: String = "Galaxy Device",
    val deviceImeiOrSerial: String = "",
    val osType: String = "Android",
    val osVersion: String = "14.0",
    val lockStatus: String = "Passcode Locked",
    val encryptionType: String = "File-Based (FBE) + Secure Enclave",
    val cloudDependency: String = "Cloud Backups Detected",
    val competenceLevel: String = "Intermediate",
    val warrantType: String = "Full Search Warrant (Sec 118 CPC)",
    val warrantReference: String = "WARR-2025-091",
    val cloudScopeAuthorized: Boolean = true,
    val selectedTools: Set<String> = emptySet(),
    val examinerName: String = "",
    val examinerBadge: String = "",
    val examinerRank: String = "Detective Inspector",
    val stationUnit: String = "Forensic Laboratory Unit, Police HQ Lusaka",
    val isFaradayIsolated: Boolean = true,
    val isAirplaneModeConfirmed: Boolean = true,
    val isWriteBlockerAttached: Boolean = true
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
            deviceImeiOrSerial = "3589${(System.currentTimeMillis() % 1000000000)}",
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
        deviceMake: String = _caseFormState.value.deviceMake,
        deviceModel: String = _caseFormState.value.deviceModel,
        imeiOrSerial: String = _caseFormState.value.deviceImeiOrSerial,
        lockStatus: String = _caseFormState.value.lockStatus,
        warrantType: String = _caseFormState.value.warrantType,
        cloudAuthorized: Boolean = _caseFormState.value.cloudScopeAuthorized,
        selectedTools: Set<String>? = null
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
            deviceMake = deviceMake,
            deviceModel = deviceModel,
            deviceImeiOrSerial = imeiOrSerial,
            lockStatus = lockStatus,
            warrantType = warrantType,
            cloudScopeAuthorized = cloudAuthorized,
            selectedTools = currentTools
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

    // Direct SQLite Saving with immediate callback
    fun saveCaseRecord(onSaved: (Long) -> Unit) {
        val state = _caseFormState.value
        val officer = _currentOfficer.value

        val allocated = getAllocatedToolsForCurrentForm()
        val allocatedNames = allocated.map { it.toolName }
        val selected = state.selectedTools.ifEmpty { allocatedNames.toSet() }

        // Compute recommended method
        val recommendedMethod = when {
            state.deviceType.contains("Volatile", ignoreCase = true) || state.deviceType.contains("RAM", ignoreCase = true) ->
                "Volatile Memory Forensics"
            state.deviceType.contains("Computer", ignoreCase = true) || state.deviceType.contains("HDD", ignoreCase = true) ->
                "Physical Bit-Stream Disk Imaging"
            selected.any { it.contains("Cellebrite", ignoreCase = true) || it.contains("XRY", ignoreCase = true) } ->
                "Physical Bit-Stream Acquisition"
            selected.any { it.contains("Oxygen", ignoreCase = true) } && state.cloudScopeAuthorized ->
                "Live Cloud & Smartphone Acquisition"
            selected.any { it.contains("AXIOM", ignoreCase = true) || it.contains("Belkasoft", ignoreCase = true) } ->
                "Multi-Artifact Unified Correlation"
            else -> "Advanced File-System Acquisition"
        }

        val primaryRationale = when {
            recommendedMethod.contains("Volatile", ignoreCase = true) ->
                "Live volatile RAM capture preserves active malware, processes, and unencrypted keys before machine shutdown."
            recommendedMethod.contains("Disk Imaging", ignoreCase = true) ->
                "Full bit-stream acquisition preserves unallocated clusters and court-admissible disk hashes."
            recommendedMethod.contains("Physical", ignoreCase = true) ->
                "Bit-stream physical dump bypasses secure partitions and recovers deleted database rows."
            recommendedMethod.contains("Cloud", ignoreCase = true) ->
                "Live cloud extraction authorizes pulls of authenticated WhatsApp, Google, and iCloud remote backups."
            else -> "Parses decrypted application packages, SMS transactions, and system logs."
        }

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
            warrantReference = state.warrantReference,
            cloudScopeAuthorized = state.cloudScopeAuthorized,
            allocatedToolsCsv = allocatedNames.joinToString(", "),
            selectedToolsCsv = selected.joinToString(", "),
            recommendedMethod = recommendedMethod,
            recommendedToolCategory = state.deviceType,
            recommendedTools = selected.joinToString(", "),
            primaryRationale = primaryRationale,
            chainOfCustodyCompleted = state.isFaradayIsolated && state.isAirplaneModeConfirmed,
            officerName = state.examinerName.ifBlank { officer?.fullName ?: "Officer Amon Bili" },
            officerRank = state.examinerRank,
            officerBadge = state.examinerBadge.ifBlank { officer?.officerId ?: "ZP-8842" },
            stationUnit = state.stationUnit,
            timestamp = System.currentTimeMillis(),
            supervisorStatus = "PENDING_REVIEW",
            supervisorNotes = ""
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
                supervisorNotes = notes
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
}
