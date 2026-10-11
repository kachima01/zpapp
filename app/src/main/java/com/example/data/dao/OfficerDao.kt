package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.OfficerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OfficerDao {

    @Query("SELECT * FROM officers WHERE officerId = :officerId LIMIT 1")
    suspend fun getOfficerById(officerId: String): OfficerEntity?

    @Query("SELECT * FROM officers LIMIT 1")
    suspend fun getAnyOfficer(): OfficerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfficer(officer: OfficerEntity)

    @Query("SELECT COUNT(*) FROM officers")
    suspend fun getOfficerCount(): Int
}
