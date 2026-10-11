package com.example.data.repository

import com.example.data.dao.DecisionRecordDao
import com.example.data.dao.StationToolDao
import com.example.data.database.AppDatabase
import com.example.data.model.DecisionRecordEntity
import com.example.data.model.StationToolEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ForensicRepository(
    private val decisionRecordDao: DecisionRecordDao,
    private val stationToolDao: StationToolDao,
    private val officerDao: com.example.data.dao.OfficerDao
) {

    val allRecords: Flow<List<DecisionRecordEntity>> = decisionRecordDao.getAllRecords()
    val allTools: Flow<List<StationToolEntity>> = stationToolDao.getAllTools()
    val availableTools: Flow<List<StationToolEntity>> = stationToolDao.getAvailableTools()
    val recordCount: Flow<Int> = decisionRecordDao.getCount()

    suspend fun getOfficer(officerId: String): com.example.data.model.OfficerEntity? {
        return withContext(Dispatchers.IO) {
            officerDao.getOfficerById(officerId)
        }
    }

    suspend fun registerOfficer(officer: com.example.data.model.OfficerEntity) {
        withContext(Dispatchers.IO) {
            officerDao.insertOfficer(officer)
        }
    }

    suspend fun getAnyOfficer(): com.example.data.model.OfficerEntity? {
        return withContext(Dispatchers.IO) {
            officerDao.getAnyOfficer()
        }
    }

    fun getRecordById(id: Long): Flow<DecisionRecordEntity?> {
        return decisionRecordDao.getRecordById(id)
    }

    suspend fun getRecordByIdDirect(id: Long): DecisionRecordEntity? {
        return withContext(Dispatchers.IO) {
            decisionRecordDao.getRecordByIdDirect(id)
        }
    }

    fun searchRecords(query: String): Flow<List<DecisionRecordEntity>> {
        return decisionRecordDao.searchRecords(query)
    }

    fun getRecordsByMethod(method: String): Flow<List<DecisionRecordEntity>> {
        return decisionRecordDao.getRecordsByMethod(method)
    }

    suspend fun insertDecisionRecord(record: DecisionRecordEntity): Long {
        return withContext(Dispatchers.IO) {
            decisionRecordDao.insertRecord(record)
        }
    }

    suspend fun updateDecisionRecord(record: DecisionRecordEntity) {
        withContext(Dispatchers.IO) {
            decisionRecordDao.updateRecord(record)
        }
    }

    suspend fun deleteDecisionRecord(id: Long) {
        withContext(Dispatchers.IO) {
            decisionRecordDao.deleteRecordById(id)
        }
    }

    suspend fun setToolAvailability(id: Long, isAvailable: Boolean) {
        withContext(Dispatchers.IO) {
            stationToolDao.setToolAvailability(id, isAvailable)
        }
    }

    suspend fun updateTool(tool: StationToolEntity) {
        withContext(Dispatchers.IO) {
            stationToolDao.updateTool(tool)
        }
    }

    suspend fun ensureDefaultToolsSeeded() {
        withContext(Dispatchers.IO) {
            ensureDefaultDataSeeded()
        }
    }

    suspend fun ensureDefaultDataSeeded() {
        withContext(Dispatchers.IO) {
            if (stationToolDao.getToolCountDirect() == 0) {
                stationToolDao.insertTools(AppDatabase.defaultForensicTools)
            }
            if (officerDao.getOfficerCount() == 0) {
                officerDao.insertOfficer(
                    com.example.data.model.OfficerEntity(
                        officerId = "ZP-8842",
                        fullName = "Officer Amon Bili",
                        role = "Investigator",
                        password = "password",
                        station = "Forensic Laboratory Unit, Police HQ Lusaka"
                    )
                )
                officerDao.insertOfficer(
                    com.example.data.model.OfficerEntity(
                        officerId = "ZP-1001",
                        fullName = "Senior Inspector Banda",
                        role = "Supervisor",
                        password = "password",
                        station = "Forensic Oversight Division, Police HQ Lusaka"
                    )
                )
            }
            if (decisionRecordDao.getCountDirect() == 0) {
                val seedCases = listOf(
                    DecisionRecordEntity(
                        caseNumber = "ZP-2025-0891",
                        incidentReference = "DOCKET-CR/489/25",
                        crimeType = "Mobile Money Fraud",
                        deviceType = "Smartphone / Mobile",
                        incidentLocation = "Lusaka Central Business District",
                        suspectInfo = "K. Mwape (SIM card syndicates)",
                        deviceMake = "Samsung",
                        deviceModel = "Galaxy S22 Ultra",
                        deviceImeiOrSerial = "356891104829104",
                        osType = "Android",
                        osVersion = "14.0",
                        lockStatus = "Passcode Locked",
                        encryptionType = "File-Based (FBE) + Knox Enclave",
                        cloudDependency = "Cloud Backups Detected",
                        competenceLevel = "Intermediate",
                        warrantType = "Full Search Warrant (Sec 118 CPC)",
                        warrantReference = "WR-LUS-2025-088",
                        cloudScopeAuthorized = true,
                        allocatedToolsCsv = "Cellebrite UFED & Physical Analyzer, MSAB XRY, Oxygen Forensics",
                        selectedToolsCsv = "Cellebrite UFED & Physical Analyzer, MSAB XRY, Oxygen Forensics",
                        recommendedMethod = "Physical Bit-Stream Acquisition (EDL/BootROM)",
                        recommendedToolCategory = "Mobile and Cloud Forensics",
                        recommendedTools = "Cellebrite UFED & Physical Analyzer, MSAB XRY",
                        primaryRationale = "Passcode locked device with hardware keystore. Physical extraction via bootloader/EDL required to bypass screen lock and extract encrypted Airtel/MTN mobile money databases.",
                        rejectedAlternativesRaw = "Manual Inspection: Infeasible [Passcode Locked] | Logical Extraction: Incomplete [FBE Encryption Blocks SQLite]",
                        cautionaryGuidanceCsv = "Maintain Faraday isolation to prevent remote wipe | Document battery percentage prior to physical dump",
                        chainOfCustodyCompleted = true,
                        officerName = "Officer Amon Bili",
                        officerRank = "Detective Inspector",
                        officerBadge = "ZP-8842",
                        stationUnit = "Forensic Laboratory Unit, Police HQ Lusaka",
                        timestamp = System.currentTimeMillis() - 7200000,
                        supervisorStatus = "APPROVED",
                        supervisorNotes = "Authorized for forensic presentation in Subordinate Court.",
                        supervisorReviewedAt = System.currentTimeMillis() - 3600000
                    ),
                    DecisionRecordEntity(
                        caseNumber = "ZP-2025-0412",
                        incidentReference = "DOCKET-CY/102/25",
                        crimeType = "Computer Hacking & Intrusion",
                        deviceType = "Computer / Server / HDD",
                        incidentLocation = "Ministry Data Center, Lusaka",
                        suspectInfo = "Unauthorized SSH tunnel",
                        deviceMake = "Dell PowerEdge",
                        deviceModel = "R740 Rack Server (RAID 5)",
                        deviceImeiOrSerial = "SRV-LUS-99120",
                        osType = "Windows Server",
                        osVersion = "2022 DataCenter",
                        lockStatus = "BitLocker Encrypted",
                        encryptionType = "BitLocker (TPM 2.0)",
                        cloudDependency = "Local Only",
                        competenceLevel = "Advanced",
                        warrantType = "Special High Court Production Order",
                        warrantReference = "HC-ORD-449/25",
                        cloudScopeAuthorized = false,
                        allocatedToolsCsv = "EnCase Forensic, FTK (Forensic Toolkit), Autopsy",
                        selectedToolsCsv = "EnCase Forensic, FTK (Forensic Toolkit)",
                        recommendedMethod = "Bit-Stream Forensic Imaging with Hardware Write-Blocker",
                        recommendedToolCategory = "Computer and Deep File System Forensics",
                        recommendedTools = "EnCase Forensic, FTK (Forensic Toolkit)",
                        primaryRationale = "Server RAID storage requires forensically verified bit-stream E01 image. EnCase preserves chain-of-custody hashes while FTK indexes multi-gigabyte event logs.",
                        rejectedAlternativesRaw = "Live File Copy: Invalidates metadata and hashes | Manual Browsing: Alters access timestamps",
                        cautionaryGuidanceCsv = "Hardware write-blocker mandatory before connection | Compute SHA-256 and MD5 hashes simultaneously",
                        chainOfCustodyCompleted = true,
                        officerName = "Officer Amon Bili",
                        officerRank = "Detective Inspector",
                        officerBadge = "ZP-8842",
                        stationUnit = "Forensic Laboratory Unit, Police HQ Lusaka",
                        timestamp = System.currentTimeMillis() - 86400000,
                        supervisorStatus = "PENDING_REVIEW",
                        supervisorNotes = "",
                        supervisorReviewedAt = 0L
                    ),
                    DecisionRecordEntity(
                        caseNumber = "ZP-2025-0724",
                        incidentReference = "DOCKET-DF/301/25",
                        crimeType = "Active Malware / Live Infection",
                        deviceType = "Volatile RAM & Memory",
                        incidentLocation = "Commercial Bank HQ, Lusaka",
                        suspectInfo = "Banking Trojan injection",
                        deviceMake = "Lenovo ThinkPad",
                        deviceModel = "P1 Workstation (Live Session)",
                        deviceImeiOrSerial = "PF-38812A",
                        osType = "Windows",
                        osVersion = "11 Enterprise (Build 22631)",
                        lockStatus = "Unlocked (Live Session)",
                        encryptionType = "Full-Disk (FDE)",
                        cloudDependency = "Unknown",
                        competenceLevel = "Advanced",
                        warrantType = "Exigent Circumstances Emergency Search",
                        warrantReference = "EX-EMERG-712/25",
                        cloudScopeAuthorized = false,
                        allocatedToolsCsv = "Volatility Framework, Belkasoft X",
                        selectedToolsCsv = "Volatility Framework, Belkasoft X",
                        recommendedMethod = "Live Volatile RAM Dump & Memory Forensics",
                        recommendedToolCategory = "Free & Open-Source Forensic Frameworks",
                        recommendedTools = "Volatility Framework, Belkasoft X",
                        primaryRationale = "Target system is active and running in memory. Shutting down would wipe encryption keys and injected malware DLLs. Volatility parses kernel structures and active connections.",
                        rejectedAlternativesRaw = "Cold Shutdown: Destroys volatile encryption keys and unwritten malware payload in RAM",
                        cautionaryGuidanceCsv = "Do not power down or reboot machine | Execute memory capture tool directly from external read-only drive",
                        chainOfCustodyCompleted = true,
                        officerName = "Senior Inspector Banda",
                        officerRank = "Senior Detective Inspector",
                        officerBadge = "ZP-1001",
                        stationUnit = "Forensic Oversight Division, Police HQ Lusaka",
                        timestamp = System.currentTimeMillis() - 172800000,
                        supervisorStatus = "PENDING_REVIEW",
                        supervisorNotes = "",
                        supervisorReviewedAt = 0L
                    ),
                    DecisionRecordEntity(
                        caseNumber = "ZP-2025-0105",
                        incidentReference = "DOCKET-FN/044/25",
                        crimeType = "Financial Theft & Embezzlement",
                        deviceType = "Cross-Platform & Cloud Accounts",
                        incidentLocation = "Mining Executive Offices, Kitwe",
                        suspectInfo = "C. Chanda (Procurement Director)",
                        deviceMake = "Apple / MacBook & iPhone",
                        deviceModel = "MacBook Air M2 & iPhone 15 Pro",
                        deviceImeiOrSerial = "C02XYZ1234 & 359123456789012",
                        osType = "macOS / iOS",
                        osVersion = "macOS Sonoma & iOS 17.4",
                        lockStatus = "Biometric / Password Known",
                        encryptionType = "APFS FileVault + Secure Enclave",
                        cloudDependency = "Cloud Synced Apps",
                        competenceLevel = "Intermediate",
                        warrantType = "Full Search Warrant (Sec 118 CPC)",
                        warrantReference = "WR-KTW-2025-019",
                        cloudScopeAuthorized = true,
                        allocatedToolsCsv = "Magnet AXIOM, Oxygen Forensics, Belkasoft X",
                        selectedToolsCsv = "Magnet AXIOM, Oxygen Forensics",
                        recommendedMethod = "Cross-Platform Artifact Correlation & Cloud Extraction",
                        recommendedToolCategory = "All-in-One and Artifact Correlation",
                        recommendedTools = "Magnet AXIOM, Oxygen Forensics",
                        primaryRationale = "Financial transactions involve offshore cloud bank portals, WhatsApp spreadsheets, and macOS ledger files. Magnet AXIOM correlates cross-device timestamps into a unified timeline.",
                        rejectedAlternativesRaw = "Isolated Device Examination: Fails to detect coordinated cross-platform transaction timeline",
                        cautionaryGuidanceCsv = "Verify cloud extraction date parameters stay strictly within authorized warrant period",
                        chainOfCustodyCompleted = true,
                        officerName = "Officer Amon Bili",
                        officerRank = "Detective Inspector",
                        officerBadge = "ZP-8842",
                        stationUnit = "Forensic Laboratory Unit, Police HQ Lusaka",
                        timestamp = System.currentTimeMillis() - 259200000,
                        supervisorStatus = "APPROVED",
                        supervisorNotes = "Correlated timeline confirmed match with central bank ledger.",
                        supervisorReviewedAt = System.currentTimeMillis() - 200000000
                    )
                )
                seedCases.forEach { decisionRecordDao.insertRecord(it) }
            }
        }
    }
}
