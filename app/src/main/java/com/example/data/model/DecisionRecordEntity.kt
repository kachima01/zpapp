package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "decision_records")
data class DecisionRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val caseNumber: String,
    val incidentReference: String,
    val crimeType: String = "Mobile Money Fraud",
    val deviceType: String = "Smartphone / Mobile", // Smartphone / Mobile, Computer / Server / HDD, Cloud Account, Volatile Memory, Cross-Platform Correlation
    val incidentLocation: String = "",
    val suspectInfo: String = "",
    val deviceMake: String,
    val deviceModel: String,
    val deviceImeiOrSerial: String,
    val osType: String, // Android, iOS, Windows, Linux, macOS, Feature Phone, Other
    val osVersion: String,
    val lockStatus: String, // Unlocked, Locked Passcode/PIN, BitLocker / FBE, Pattern Unknown, Broken Display
    val encryptionType: String, // File-Based (FBE) + Secure Enclave, Full-Disk (FDE), BitLocker, Unencrypted
    val cloudDependency: String, // Cloud Backups Detected, Cloud Synced Apps, Local Only, Unknown
    val competenceLevel: String, // Basic, Intermediate, Advanced
    val warrantType: String, // Full Search Warrant (Sec 118 CPC), Limited Scope Warrant, Owner Signed Consent, Exigent Circumstances (Sec 22), Preliminary Preservation
    val warrantReference: String,
    val cloudScopeAuthorized: Boolean,
    val allocatedToolsCsv: String = "", // Comma-separated tools allocated for this case
    val selectedToolsCsv: String = "", // Tools confirmed by investigator
    val recommendedMethod: String, // Physical Extraction, File-System Acquisition, Basic Logical, Memory Forensics, Manual Photographic Inspection
    val recommendedToolCategory: String,
    val recommendedTools: String,
    val primaryRationale: String,
    val rejectedAlternativesRaw: String = "", // Formatted rejected methods & barriers
    val chainOfCustodyCompleted: Boolean = true,
    val officerName: String,
    val officerRank: String,
    val officerBadge: String,
    val stationUnit: String,
    val timestamp: Long = System.currentTimeMillis(),
    val supervisorStatus: String = "PENDING_REVIEW", // PENDING_REVIEW, APPROVED, FLAGGED
    val supervisorNotes: String = ""
)
