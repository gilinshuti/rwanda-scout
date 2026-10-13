package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE matchId = :matchId ORDER BY matchTimeSeconds ASC, id ASC")
    fun getEventsForMatchFlow(matchId: Long): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE matchId = :matchId ORDER BY matchTimeSeconds ASC, id ASC")
    suspend fun getEventsForMatch(matchId: Long): List<EventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Query("SELECT COUNT(*) FROM events")
    fun getTotalEventCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM events")
    suspend fun getTotalEventCount(): Int

    @Query("SELECT * FROM events")
    suspend fun getAllEvents(): List<EventEntity>

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEventById(id: Long)

    @Query("DELETE FROM events WHERE matchId = :matchId")
    suspend fun deleteEventsForMatch(matchId: Long)
}
