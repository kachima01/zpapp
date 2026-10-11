package com.example.data.repository

import com.example.data.dao.DecisionRecordDao
import com.example.data.dao.StationToolDao
import com.example.data.database.AppDatabase
import com.example.data.model.DecisionRecordEntity
import com.example.data.model.StationToolEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ForensicRepository(
    private val decisionRecordDao: DecisionRecordDao,
    private val stationToolDao: StationToolDao,
    private val officerDao: com.example.data.dao.OfficerDao
) {

    val allRecords: Flow<List<DecisionRecordEntity>> = decisionRecordDao.getAllRecords()
    val allTools: Flow<List<StationToolEntity>> = stationToolDao.getAllTools()
    val availableTools: Flow<List<StationToolEntity>> = stationToolDao.getAvailableTools()
    val recordCount: Flow<Int> = decisionRecordDao.getCount()

    suspend fun getOfficer(officerId: String): com.example.data.model.OfficerEntity? {
        return withContext(Dispatchers.IO) {
            officerDao.getOfficerById(officerId)
        }
    }

    suspend fun registerOfficer(officer: com.example.data.model.OfficerEntity) {
        withContext(Dispatchers.IO) {
            officerDao.insertOfficer(officer)
        }
    }

    suspend fun getAnyOfficer(): com.example.data.model.OfficerEntity? {
        return withContext(Dispatchers.IO) {
            officerDao.getAnyOfficer()
        }
    }

    fun getRecordById(id: Long): Flow<DecisionRecordEntity?> {
        return decisionRecordDao.getRecordById(id)
    }

    suspend fun getRecordByIdDirect(id: Long): DecisionRecordEntity? {
        return withContext(Dispatchers.IO) {
            decisionRecordDao.getRecordByIdDirect(id)
        }
    }

    fun searchRecords(query: String): Flow<List<DecisionRecordEntity>> {
        return decisionRecordDao.searchRecords(query)
    }

    fun getRecordsByMethod(method: String): Flow<List<DecisionRecordEntity>> {
        return decisionRecordDao.getRecordsByMethod(method)
    }

    suspend fun insertDecisionRecord(record: DecisionRecordEntity): Long {
        return withContext(Dispatchers.IO) {
            decisionRecordDao.insertRecord(record)
        }
    }

    suspend fun updateDecisionRecord(record: DecisionRecordEntity) {
        withContext(Dispatchers.IO) {
            decisionRecordDao.updateRecord(record)
        }
    }

    suspend fun deleteDecisionRecord(id: Long) {
        withContext(Dispatchers.IO) {
            decisionRecordDao.deleteRecordById(id)
        }
    }

    suspend fun setToolAvailability(id: Long, isAvailable: Boolean) {
        withContext(Dispatchers.IO) {
            stationToolDao.setToolAvailability(id, isAvailable)
        }
    }

    suspend fun updateTool(tool: StationToolEntity) {
        withContext(Dispatchers.IO) {
            stationToolDao.updateTool(tool)
        }
    }

    suspend fun ensureDefaultToolsSeeded() {
        withContext(Dispatchers.IO) {
            if (stationToolDao.getToolCountDirect() == 0) {
                stationToolDao.insertTools(AppDatabase.defaultForensicTools)
            }
        }
    }
}
