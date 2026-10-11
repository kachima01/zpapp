package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DecisionRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DecisionRecordDao {

    @Query("SELECT * FROM decision_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<DecisionRecordEntity>>

    @Query("SELECT * FROM decision_records WHERE id = :id LIMIT 1")
    fun getRecordById(id: Long): Flow<DecisionRecordEntity?>

    @Query("SELECT * FROM decision_records WHERE id = :id LIMIT 1")
    suspend fun getRecordByIdDirect(id: Long): DecisionRecordEntity?

    @Query("""
        SELECT * FROM decision_records 
        WHERE caseNumber LIKE '%' || :query || '%' 
           OR deviceImeiOrSerial LIKE '%' || :query || '%'
           OR officerName LIKE '%' || :query || '%'
           OR deviceMake LIKE '%' || :query || '%'
           OR deviceModel LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
    """)
    fun searchRecords(query: String): Flow<List<DecisionRecordEntity>>

    @Query("SELECT * FROM decision_records WHERE recommendedMethod = :method ORDER BY timestamp DESC")
    fun getRecordsByMethod(method: String): Flow<List<DecisionRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: DecisionRecordEntity): Long

    @Update
    suspend fun updateRecord(record: DecisionRecordEntity)

    @Query("DELETE FROM decision_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("SELECT COUNT(*) FROM decision_records")
    fun getCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM decision_records")
    suspend fun getCountDirect(): Int
}
