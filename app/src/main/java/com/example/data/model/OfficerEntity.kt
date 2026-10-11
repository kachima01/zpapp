package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "officers")
data class OfficerEntity(
    @PrimaryKey
    val officerId: String, // e.g. "ZP-8842"
    val fullName: String,  // e.g. "Officer Amon Bili"
    val role: String,      // "Investigator" or "Supervisor"
    val password: String,
    val station: String = "Forensic Laboratory Unit, Police HQ Lusaka"
)
