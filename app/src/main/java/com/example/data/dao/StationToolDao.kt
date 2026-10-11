package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.StationToolEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StationToolDao {

    @Query("SELECT * FROM station_tools ORDER BY category ASC, name ASC")
    fun getAllTools(): Flow<List<StationToolEntity>>

    @Query("SELECT * FROM station_tools WHERE isAvailable = 1 ORDER BY name ASC")
    fun getAvailableTools(): Flow<List<StationToolEntity>>

    @Query("SELECT COUNT(*) FROM station_tools")
    suspend fun getToolCountDirect(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTool(tool: StationToolEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTools(tools: List<StationToolEntity>)

    @Update
    suspend fun updateTool(tool: StationToolEntity)

    @Query("UPDATE station_tools SET isAvailable = :isAvailable WHERE id = :id")
    suspend fun setToolAvailability(id: Long, isAvailable: Boolean)

    @Query("DELETE FROM station_tools WHERE id = :id")
    suspend fun deleteToolById(id: Long)
}
