package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.BallTapEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BallTapDao {
    @Query("SELECT * FROM ballTaps WHERE matchId = :matchId ORDER BY createdAtMillis ASC")
    fun getBallTapsForMatchFlow(matchId: Long): Flow<List<BallTapEntity>>

    @Query("SELECT * FROM ballTaps WHERE matchId = :matchId ORDER BY createdAtMillis ASC")
    suspend fun getBallTapsForMatch(matchId: Long): List<BallTapEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBallTap(tap: BallTapEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBallTaps(taps: List<BallTapEntity>)

    @Query("SELECT * FROM ballTaps")
    suspend fun getAllBallTaps(): List<BallTapEntity>

    @Delete
    suspend fun deleteBallTap(tap: BallTapEntity)

    @Query("DELETE FROM ballTaps WHERE matchId = :matchId")
    suspend fun deleteBallTapsForMatch(matchId: Long)
}
