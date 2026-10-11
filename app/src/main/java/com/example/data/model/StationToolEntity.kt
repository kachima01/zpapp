package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "station_tools")
data class StationToolEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String, // Mobile & Cloud, All-in-One, Deep File System, Free & Open-Source, Hardware
    val capabilities: String, // Comma separated: Physical, File-System, Logical, Cloud, Memory Dump, Live Triage
    val isAvailable: Boolean = true,
    val licenseStatus: String = "ACTIVE", // ACTIVE, EXPIRED, NOT_LICENSED, COMMUNITY_EDITION, HARDWARE_READY, DEPLOYED
    val description: String = "",
    val unavailabilityReason: String = "", // Free text: why tool is marked unavailable (maintenance, seized, expired, etc.)
    val licenseExpiryDate: String = ""    // ISO date string "YYYY-MM-DD" or blank if not applicable
)
