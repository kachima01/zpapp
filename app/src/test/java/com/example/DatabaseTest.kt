package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.DecisionRecordEntity
import com.example.engine.CaseToolAllocator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DatabaseTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testInsertAndRetrieveRecord() = runBlocking {
        val record = DecisionRecordEntity(
            caseNumber = "ZP-TEST-001",
            incidentReference = "INC-001",
            crimeType = "Mobile Money Fraud",
            deviceType = "Smartphone / Mobile",
            deviceMake = "Samsung",
            deviceModel = "Galaxy S21",
            deviceImeiOrSerial = "123456789012345",
            osType = "Android",
            osVersion = "13",
            lockStatus = "Passcode Locked",
            encryptionType = "FBE",
            cloudDependency = "None",
            competenceLevel = "Intermediate",
            warrantType = "Full Search Warrant",
            warrantReference = "W-123",
            cloudScopeAuthorized = true,
            allocatedToolsCsv = "Cellebrite UFED & Physical Analyzer, MSAB XRY, Oxygen Forensics",
            selectedToolsCsv = "Cellebrite UFED & Physical Analyzer, MSAB XRY",
            recommendedMethod = "Physical Bit-Stream Acquisition",
            recommendedToolCategory = "Smartphone / Mobile",
            recommendedTools = "Cellebrite UFED & Physical Analyzer, MSAB XRY",
            primaryRationale = "Full physical extraction possible",
            rejectedAlternativesRaw = "",
            officerName = "Officer Amon Bili",
            officerRank = "Detective Inspector",
            officerBadge = "ZP-8842",
            stationUnit = "Cyber Lab"
        )

        val id = db.decisionRecordDao().insertRecord(record)
        assertTrue(id > 0)

        val records = db.decisionRecordDao().getAllRecords().first()
        assertEquals(1, records.size)
        assertEquals("ZP-TEST-001", records[0].caseNumber)
        assertEquals("Smartphone / Mobile", records[0].deviceType)
        assertEquals("Samsung", records[0].deviceMake)
        assertTrue(records[0].allocatedToolsCsv.contains("Cellebrite"))
    }

    @Test
    fun testToolAllocationEngine() {
        // Mobile test
        val mobileTools = CaseToolAllocator.getAllocatedToolsForCase(
            deviceType = "Smartphone / Mobile",
            crimeType = "Mobile Money Fraud",
            isLocked = true,
            cloudAuthorized = true
        )
        assertTrue(mobileTools.any { it.toolName.contains("Cellebrite") })
        assertTrue(mobileTools.any { it.toolName.contains("MSAB XRY") })
        assertTrue(mobileTools.any { it.toolName.contains("Oxygen") })

        // Computer disk test
        val computerTools = CaseToolAllocator.getAllocatedToolsForCase(
            deviceType = "Computer / Server / HDD",
            crimeType = "Computer Hacking & Intrusion",
            isLocked = false,
            cloudAuthorized = false
        )
        assertTrue(computerTools.any { it.toolName.contains("EnCase") })
        assertTrue(computerTools.any { it.toolName.contains("FTK") })
        assertTrue(computerTools.any { it.toolName.contains("Autopsy") })

        // Volatile memory / RAM test
        val ramTools = CaseToolAllocator.getAllocatedToolsForCase(
            deviceType = "Volatile Memory / RAM",
            crimeType = "Active Malware / Live Infection",
            isLocked = false,
            cloudAuthorized = false
        )
        assertTrue(ramTools.any { it.toolName.contains("Volatility") })
        assertTrue(ramTools.any { it.toolName.contains("Belkasoft") })

        // Cross-platform timeline correlation test
        val timelineTools = CaseToolAllocator.getAllocatedToolsForCase(
            deviceType = "Cross-Platform Correlation",
            crimeType = "Financial Theft & Embezzlement",
            isLocked = false,
            cloudAuthorized = true
        )
        assertTrue(timelineTools.any { it.toolName.contains("Magnet AXIOM") })
        assertTrue(timelineTools.any { it.toolName.contains("Belkasoft X") })
    }
}
