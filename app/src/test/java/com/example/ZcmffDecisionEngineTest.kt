package com.example

import com.example.engine.AssessmentInputs
import com.example.engine.ZcmffDecisionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ZcmffDecisionEngineTest {

    @Test
    fun testPhysicalExtractionSelectedWhenToolsAndFullWarrantAvailable() {
        val inputs = AssessmentInputs(
            selectedTools = listOf("Cellebrite UFED & Physical Analyzer", "MSAB XRY", "Hardware Write-Blocker"),
            osType = "Android",
            osVersion = "13.0",
            lockStatus = "Unlocked / Passcode Known",
            encryptionType = "File-Based (FBE) + Secure Enclave",
            cloudDependency = "WhatsApp / Signal Cloud Backups",
            competenceLevel = "Advanced",
            warrantType = "Full Search Warrant (Sec 118 CPC)",
            warrantReference = "WARR/LUS/2026/001",
            cloudScopeAuthorized = false
        )

        val result = ZcmffDecisionEngine.evaluate(inputs)
        assertTrue(result.recommendedMethod.contains("Physical", ignoreCase = true))
        assertTrue(result.recommendedTools.any { it.contains("Cellebrite") })
    }

    @Test
    fun testPhysicalRejectedWhenWarrantIsLimitedScope() {
        val inputs = AssessmentInputs(
            selectedTools = listOf("Cellebrite UFED & Physical Analyzer", "MSAB XRY", "Oxygen Forensics Detective"),
            osType = "Android",
            osVersion = "14.0",
            lockStatus = "Unlocked / Passcode Known",
            encryptionType = "File-Based (FBE) + Secure Enclave",
            cloudDependency = "Local Storage Only",
            competenceLevel = "Advanced",
            warrantType = "Limited Scope Warrant (App/Date Specific)",
            warrantReference = "WARR/SPECIFIC/092",
            cloudScopeAuthorized = false
        )

        val result = ZcmffDecisionEngine.evaluate(inputs)
        assertFalse(result.recommendedMethod.contains("Physical", ignoreCase = true))
        assertTrue(result.rejectedAlternatives.any { it.methodName.contains("Physical") })
        assertTrue(result.recommendedMethod.contains("File-System") || result.recommendedMethod.contains("Logical"))
    }

    @Test
    fun testCloudExtractionRejectedWhenCloudScopeNotAuthorized() {
        val inputs = AssessmentInputs(
            selectedTools = listOf("Oxygen Forensics Detective", "Magnet AXIOM"),
            osType = "iOS",
            osVersion = "17.0",
            lockStatus = "Unlocked / Passcode Known",
            encryptionType = "File-Based (FBE) + Secure Enclave",
            cloudDependency = "WhatsApp / Signal Cloud Backups",
            competenceLevel = "Intermediate",
            warrantType = "Full Search Warrant (Sec 118 CPC)",
            warrantReference = "WARR/LOCAL/041",
            cloudScopeAuthorized = false // Cloud NOT authorized!
        )

        val result = ZcmffDecisionEngine.evaluate(inputs)
        assertTrue(result.rejectedAlternatives.any {
            it.methodName.contains("Cloud") && it.technicalBarrier.contains("Not Authorized")
        })
    }

    @Test
    fun testFallbackToManualPhotographicWhenDeviceLockedAndNoBypassTools() {
        val inputs = AssessmentInputs(
            selectedTools = listOf("Manual Camera Kit & Faraday Shield Isolation"),
            osType = "Android",
            osVersion = "14.0",
            lockStatus = "Locked - PIN / Passcode Unknown",
            encryptionType = "File-Based (FBE) + Secure Enclave",
            cloudDependency = "Local Storage Only",
            competenceLevel = "Basic",
            warrantType = "Full Search Warrant (Sec 118 CPC)",
            warrantReference = "WARR/LUS/909",
            cloudScopeAuthorized = false
        )

        val result = ZcmffDecisionEngine.evaluate(inputs)
        assertTrue(result.recommendedMethod.contains("Manual Photographic", ignoreCase = true))
        assertTrue(result.rejectedAlternatives.any { it.methodName.contains("Physical") })
        assertTrue(result.rejectedAlternatives.any { it.methodName.contains("File-System") })
        assertTrue(result.rejectedAlternatives.any { it.methodName.contains("Basic Logical") })
    }
}
