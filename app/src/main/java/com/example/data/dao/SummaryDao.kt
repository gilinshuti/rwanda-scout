package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SummaryDao {
    @Query("SELECT * FROM summaries WHERE matchId = :matchId LIMIT 1")
    fun getSummaryForMatchFlow(matchId: Long): Flow<SummaryEntity?>

    @Query("SELECT * FROM summaries WHERE matchId = :matchId LIMIT 1")
    suspend fun getSummaryForMatch(matchId: Long): SummaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: SummaryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummaries(summaries: List<SummaryEntity>)

    @Query("SELECT * FROM summaries")
    suspend fun getAllSummaries(): List<SummaryEntity>

    @Delete
    suspend fun deleteSummary(summary: SummaryEntity)

    @Query("DELETE FROM summaries WHERE matchId = :matchId")
    suspend fun deleteSummaryForMatch(matchId: Long)
}
