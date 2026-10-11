package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.DecisionRecordDao
import com.example.data.dao.OfficerDao
import com.example.data.dao.StationToolDao
import com.example.data.model.DecisionRecordEntity
import com.example.data.model.OfficerEntity
import com.example.data.model.StationToolEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [DecisionRecordEntity::class, StationToolEntity::class, OfficerEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun decisionRecordDao(): DecisionRecordDao
    abstract fun stationToolDao(): StationToolDao
    abstract fun officerDao(): OfficerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zcmff_forensics.db"
                ).fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.stationToolDao()?.insertTools(defaultForensicTools)
                            INSTANCE?.officerDao()?.insertOfficer(
                                OfficerEntity(
                                    officerId = "ZP-8842",
                                    fullName = "Officer Amon Bili",
                                    role = "Investigator",
                                    password = "password",
                                    station = "Forensic Laboratory Unit, Police HQ Lusaka"
                                )
                            )
                            INSTANCE?.officerDao()?.insertOfficer(
                                OfficerEntity(
                                    officerId = "ZP-1001",
                                    fullName = "Senior Inspector Banda",
                                    role = "Supervisor",
                                    password = "password",
                                    station = "Forensic Oversight Division, Police HQ Lusaka"
                                )
                            )
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        // Standard tool inventory matching user specifications
        val defaultForensicTools = listOf(
            // Mobile and Cloud Forensics
            StationToolEntity(
                name = "Cellebrite UFED & Physical Analyzer",
                category = "Mobile and Cloud Forensics",
                capabilities = "Physical extraction, bypassing device locks, decoding encrypted messaging data",
                isAvailable = true,
                licenseStatus = "DEPLOYED",
                description = "Law enforcement gold standard for physical extraction, decoding encrypted messaging, and unlocking smartphones."
            ),
            StationToolEntity(
                name = "MSAB XRY",
                category = "Mobile and Cloud Forensics",
                capabilities = "Extracting and reading system files from phones, tablets, and SIM cards",
                isAvailable = true,
                licenseStatus = "DEPLOYED",
                description = "Highly secure, field-ready mobile forensics tool for smartphones, tablets, and SIM card filesystems."
            ),
            StationToolEntity(
                name = "Oxygen Forensics",
                category = "Mobile and Cloud Forensics",
                capabilities = "Bypassing device locks, extracting smartphones, pulling live cloud accounts (iCloud, Google, social media)",
                isAvailable = true,
                licenseStatus = "DEPLOYED",
                description = "Specializes in bypassing device locks, extracting smartphones, and pulling data directly from live cloud accounts."
            ),

            // All-in-One and Artifact Correlation
            StationToolEntity(
                name = "Magnet AXIOM",
                category = "All-in-One and Artifact Correlation",
                capabilities = "Aggregates evidence across computers, smartphones, and cloud into singular unified timeline",
                isAvailable = true,
                licenseStatus = "DEPLOYED",
                description = "Stitches disparate pieces of evidence (e.g. computer browser history + phone location data) into a centralized timeline."
            ),
            StationToolEntity(
                name = "Belkasoft X",
                category = "All-in-One and Artifact Correlation",
                capabilities = "Parses hard drives, mobile data, cloud services, and volatile computer memory dumps in one sweep",
                isAvailable = true,
                licenseStatus = "DEPLOYED",
                description = "Integrated DFIR tool parsing hard drives, mobile devices, cloud backups, and volatile memory dumps."
            ),

            // Computer and Deep File System Forensics
            StationToolEntity(
                name = "EnCase Forensic",
                category = "Computer and Deep File System Forensics",
                capabilities = "Bit-stream disk imaging, deep file systems, court-admissible evidence packages",
                isAvailable = true,
                licenseStatus = "DEPLOYED",
                description = "Platform for bit-stream disk imaging, filesystem analysis, and generating structured court-admissible packages."
            ),
            StationToolEntity(
                name = "FTK (Forensic Toolkit)",
                category = "Computer and Deep File System Forensics",
                capabilities = "Cataloging and indexing massive enterprise datasets, raw processing speed",
                isAvailable = true,
                licenseStatus = "DEPLOYED",
                description = "Heavyweight analysis tool known for raw speed and indexing massive enterprise datasets, disks, and servers."
            ),

            // Free & Open-Source Forensic Frameworks
            StationToolEntity(
                name = "Autopsy",
                category = "Free & Open-Source Forensic Frameworks",
                capabilities = "Disk images, smartphone databases, recovering deleted media without licensing cost",
                isAvailable = true,
                licenseStatus = "DEPLOYED",
                description = "Graphical interface over The Sleuth Kit. Analyzes disk images, smartphone databases, and recovers deleted media."
            ),
            StationToolEntity(
                name = "Volatility Framework",
                category = "Free & Open-Source Forensic Frameworks",
                capabilities = "Volatile RAM memory dumps, active malware, hidden running processes, open network connections",
                isAvailable = true,
                licenseStatus = "DEPLOYED",
                description = "Specialized framework to analyze volatile RAM memory dumps, revealing active malware, hidden processes, and sockets."
            )
        )
    }
}
