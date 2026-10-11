package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.model.DecisionRecordEntity
import com.example.data.model.OfficerEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppDatabaseTest {

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
    fun testInsertAndRetrieveOfficer() = runBlocking {
        val officer = OfficerEntity(
            officerId = "ZP-8842",
            fullName = "Officer Amon Bili",
            role = "Investigator",
            password = "password"
        )
        db.officerDao().insertOfficer(officer)
        val loaded = db.officerDao().getOfficerById("ZP-8842")
        assertNotNull(loaded)
        assertEquals("Officer Amon Bili", loaded?.fullName)
    }

    @Test
    fun testInsertAndRetrieveDecisionRecord() = runBlocking {
        val record = DecisionRecordEntity(
            caseNumber = "ZP-CASE-1001",
            incidentReference = "DOCKET-2001",
            crimeType = "Mobile Money Fraud",
            incidentLocation = "Lusaka Central",
            suspectInfo = "Suspect John Doe, IMEI 354890123456789",
            deviceMake = "Samsung",
            deviceModel = "Galaxy A12",
            deviceImeiOrSerial = "354890123456789",
            osType = "Android",
            osVersion = "12.0",
            lockStatus = "Unlocked",
            encryptionType = "File-Based (FBE)",
            cloudDependency = "WhatsApp Backups",
            competenceLevel = "Intermediate",
            warrantType = "Full Search Warrant (Sec 118 CPC)",
            warrantReference = "WARR-2026-01",
            cloudScopeAuthorized = false,
            selectedToolsCsv = "MSAB XRY, Cellebrite UFED",
            recommendedMethod = "Physical Acquisition",
            recommendedToolCategory = "Mobile Suite",
            recommendedTools = "MSAB XRY",
            primaryRationale = "Full extraction permitted",
            rejectedAlternativesRaw = "",
            officerName = "Officer Amon Bili",
            officerRank = "Detective Inspector",
            officerBadge = "ZP-8842",
            stationUnit = "Lusaka HQ"
        )

        val id = db.decisionRecordDao().insertRecord(record)
        val all = db.decisionRecordDao().getAllRecords().first()
        assertEquals(1, all.size)
        assertEquals("ZP-CASE-1001", all[0].caseNumber)
        assertEquals("Mobile Money Fraud", all[0].crimeType)
    }
}
