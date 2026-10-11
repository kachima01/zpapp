package com.example.engine

import com.example.data.model.RejectedAlternative

data class AssessmentInputs(
    val selectedTools: List<String>,
    val osType: String,
    val osVersion: String,
    val lockStatus: String,
    val encryptionType: String,
    val cloudDependency: String,
    val competenceLevel: String, // "Basic", "Intermediate", "Advanced"
    val warrantType: String,
    val warrantReference: String,
    val cloudScopeAuthorized: Boolean
)

data class RecommendationResult(
    val recommendedMethod: String,
    val recommendedToolCategory: String,
    val recommendedTools: List<String>,
    val rationale: String,
    val rejectedAlternatives: List<RejectedAlternative>,
    val cautionaryGuidance: List<String>
)

object ZcmffDecisionEngine {

    fun evaluate(inputs: AssessmentInputs): RecommendationResult {
        val rejected = mutableListOf<RejectedAlternative>()
        val cautions = mutableListOf<String>()

        val hasCellebrite = inputs.selectedTools.any { it.contains("Cellebrite", ignoreCase = true) }
        val hasXRY = inputs.selectedTools.any { it.contains("XRY", ignoreCase = true) }
        val hasOxygen = inputs.selectedTools.any { it.contains("Oxygen", ignoreCase = true) }
        val hasAxiom = inputs.selectedTools.any { it.contains("AXIOM", ignoreCase = true) }
        val hasBelkasoft = inputs.selectedTools.any { it.contains("Belkasoft", ignoreCase = true) }
        val hasAutopsy = inputs.selectedTools.any { it.contains("Autopsy", ignoreCase = true) }
        val hasAdb = inputs.selectedTools.any { it.contains("ADB", ignoreCase = true) }
        val hasWriteBlocker = inputs.selectedTools.any { it.contains("Write-Blocker", ignoreCase = true) }

        val isUnlocked = inputs.lockStatus.contains("Unlocked", ignoreCase = true)
        val isLocked = !isUnlocked
        val isDisplayBroken = inputs.lockStatus.contains("Broken", ignoreCase = true)

        val isLimitedWarrant = inputs.warrantType.contains("Limited Scope", ignoreCase = true)
        val isExigentOrPreservation = inputs.warrantType.contains("Exigent", ignoreCase = true) ||
                inputs.warrantType.contains("Preservation", ignoreCase = true)

        val isBasicCompetence = inputs.competenceLevel.equals("Basic", ignoreCase = true)

        // Evaluate PHYSICAL EXTRACTION
        var physicalFeasible = true
        var physicalRejectReason = ""
        var physicalBarrier = ""

        if (isLimitedWarrant) {
            physicalFeasible = false
            physicalRejectReason = "Warrant limited to specific app; physical imaging exceeds authorized scope."
            physicalBarrier = "Limited Warrant Scope"
        } else if (isExigentOrPreservation) {
            physicalFeasible = false
            physicalRejectReason = "Requires full search warrant under Section 118 CPC."
            physicalBarrier = "Search Warrant Required"
        } else if (isBasicCompetence && !hasCellebrite && !hasXRY) {
            physicalFeasible = false
            physicalRejectReason = "Officer competence is Basic (manual physical requires Advanced)."
            physicalBarrier = "Competence Threshold"
        } else if (inputs.encryptionType.contains("Secure Enclave", ignoreCase = true) && isLocked && !hasCellebrite) {
            physicalFeasible = false
            physicalRejectReason = "Secure Enclave active; passcode unknown without bypass suite."
            physicalBarrier = "Hardware Enclave Barrier"
        } else if (!hasCellebrite && !hasXRY && !hasAxiom && !hasWriteBlocker) {
            physicalFeasible = false
            physicalRejectReason = "Station lacks commercial physical imaging suite or write-blocker."
            physicalBarrier = "Tool Not Available"
        }

        if (!physicalFeasible) {
            rejected.add(
                RejectedAlternative(
                    methodName = "Physical Acquisition (Bit-Stream)",
                    primaryReason = physicalRejectReason,
                    technicalBarrier = physicalBarrier
                )
            )
        }

        // Evaluate CLOUD EXTRACTION
        if (!inputs.cloudScopeAuthorized) {
            rejected.add(
                RejectedAlternative(
                    methodName = "Cloud Account Extraction",
                    primaryReason = "Warrant covers device hardware only; cloud extraction excluded.",
                    technicalBarrier = "Cloud Scope Not Authorized"
                )
            )
        } else if (inputs.cloudDependency.contains("Local", ignoreCase = true)) {
            rejected.add(
                RejectedAlternative(
                    methodName = "Cloud Account Extraction",
                    primaryReason = "No cloud backups or accounts observed on device.",
                    technicalBarrier = "No Cloud Artifacts"
                )
            )
        } else if (!hasOxygen && !hasAxiom && !hasBelkasoft) {
            rejected.add(
                RejectedAlternative(
                    methodName = "Cloud Account Extraction",
                    primaryReason = "Station lacks active cloud extraction suite (Oxygen/AXIOM).",
                    technicalBarrier = "Tool Not Licensed"
                )
            )
        }

        // Evaluate FILE-SYSTEM EXTRACTION
        var fileSystemFeasible = true
        var fsRejectReason = ""
        var fsBarrier = ""

        if (isExigentOrPreservation) {
            fileSystemFeasible = false
            fsRejectReason = "Requires judicial search warrant."
            fsBarrier = "Warrant Scope"
        } else if (isLocked && !hasCellebrite && !hasOxygen && !hasXRY) {
            fileSystemFeasible = false
            fsRejectReason = "Passcode locked with no bypass suite active."
            fsBarrier = "Device Locked"
        } else if (isDisplayBroken && !hasCellebrite && !hasXRY) {
            fileSystemFeasible = false
            fsRejectReason = "Touchscreen unresponsive; cannot authorize USB connection."
            fsBarrier = "Damaged Screen"
        } else if (isBasicCompetence && !hasOxygen && !hasCellebrite && !hasAxiom) {
            fileSystemFeasible = false
            fsRejectReason = "File-system scripts require intermediate competence."
            fsBarrier = "Competence Level"
        } else if (!hasOxygen && !hasAxiom && !hasXRY && !hasCellebrite && !hasAutopsy && !hasBelkasoft) {
            fileSystemFeasible = false
            fsRejectReason = "No file-system extraction tool active in station."
            fsBarrier = "Tool Deficit"
        }

        if (!fileSystemFeasible) {
            rejected.add(
                RejectedAlternative(
                    methodName = "File-System Acquisition",
                    primaryReason = fsRejectReason,
                    technicalBarrier = fsBarrier
                )
            )
        }

        // Evaluate BASIC LOGICAL EXTRACTION
        var basicLogicalFeasible = true
        var logicalRejectReason = ""
        var logicalBarrier = ""

        if (isLocked && !hasCellebrite && !hasOxygen) {
            basicLogicalFeasible = false
            logicalRejectReason = "Device locked; USB debugging unauthorized."
            logicalBarrier = "Passcode Unknown"
        } else if (isDisplayBroken && !hasCellebrite) {
            basicLogicalFeasible = false
            logicalRejectReason = "Screen damaged; cannot confirm MTP transfer."
            logicalBarrier = "Screen Broken"
        } else if (!hasAdb && !hasAutopsy && !hasCellebrite && !hasOxygen && !hasAxiom && !hasXRY) {
            basicLogicalFeasible = false
            logicalRejectReason = "No digital extraction tools available."
            logicalBarrier = "No Extraction Tools"
        }

        if (!basicLogicalFeasible) {
            rejected.add(
                RejectedAlternative(
                    methodName = "Basic Logical Extraction",
                    primaryReason = logicalRejectReason,
                    technicalBarrier = logicalBarrier
                )
            )
        }

        // DECISION PATHWAY SELECTION
        if (physicalFeasible) {
            val tools = mutableListOf<String>()
            if (hasCellebrite) tools.add("Cellebrite UFED")
            if (hasXRY) tools.add("MSAB XRY")
            if (hasAxiom) tools.add("Magnet AXIOM")
            if (hasWriteBlocker) tools.add("Write-Blocker")

            return RecommendationResult(
                recommendedMethod = "Physical Acquisition (Bit-Stream)",
                recommendedToolCategory = "Physical Suite",
                recommendedTools = tools.ifEmpty { listOf("Station Forensic Station") },
                rationale = "Full bit-stream raw dump permitted by warrant and supported by available tools.",
                rejectedAlternatives = rejected,
                cautionaryGuidance = listOf("Maintain Faraday isolation", "Compute SHA-256 hash immediately")
            )
        }

        if (fileSystemFeasible) {
            val tools = mutableListOf<String>()
            if (hasOxygen) tools.add("Oxygen Forensics")
            if (hasAxiom) tools.add("Magnet AXIOM")
            if (hasXRY) tools.add("MSAB XRY")
            if (hasAutopsy) tools.add("Autopsy")
            if (hasBelkasoft) tools.add("Belkasoft X")
            if (hasAdb) tools.add("ADB Scripts")

            return RecommendationResult(
                recommendedMethod = "File-System Acquisition",
                recommendedToolCategory = "File System Suite",
                recommendedTools = tools.ifEmpty { listOf("Mobile Forensic Station") },
                rationale = "Extracts application databases, chats, and system logs. Highest feasible method.",
                rejectedAlternatives = rejected,
                cautionaryGuidance = listOf("Mount partitions read-only", "Log pushed agents in audit trail")
            )
        }

        if (basicLogicalFeasible) {
            val tools = mutableListOf<String>()
            if (hasAdb) tools.add("ADB Scripts")
            if (hasAutopsy) tools.add("Autopsy")
            if (hasOxygen) tools.add("Oxygen Forensics")
            if (hasAxiom) tools.add("Magnet AXIOM")

            return RecommendationResult(
                recommendedMethod = "Basic Logical Extraction",
                recommendedToolCategory = "Logical Suite",
                recommendedTools = tools.ifEmpty { listOf("ADB Toolchain") },
                rationale = "Extracts standard user data, calls, SMS, and contacts via backup APIs.",
                rejectedAlternatives = rejected,
                cautionaryGuidance = listOf("Deleted messages not recoverable", "Document lack of root access")
            )
        }

        // FALLBACK: Manual Photographic Documentation
        return RecommendationResult(
            recommendedMethod = "Manual Photographic Documentation",
            recommendedToolCategory = "Photographic Kit",
            recommendedTools = listOf("Manual Camera & Faraday Kit"),
            rationale = "Digital extraction precluded by device locks or tool deficit; manual inspection preserves visible evidence.",
            rejectedAlternatives = rejected,
            cautionaryGuidance = listOf("Photograph each screen under controlled light", "Keep in Faraday isolation")
        )
    }
}
