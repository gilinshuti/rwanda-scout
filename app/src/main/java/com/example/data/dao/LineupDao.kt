package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LineupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LineupDao {
    @Query("SELECT * FROM lineups WHERE matchId = :matchId ORDER BY isStarter DESC, jerseyNumber ASC")
    fun getLineupsForMatchFlow(matchId: Long): Flow<List<LineupEntity>>

    @Query("SELECT * FROM lineups WHERE matchId = :matchId ORDER BY isStarter DESC, jerseyNumber ASC")
    suspend fun getLineupsForMatch(matchId: Long): List<LineupEntity>

    @Query("SELECT * FROM lineups WHERE matchId = :matchId AND team = :team ORDER BY isStarter DESC, jerseyNumber ASC")
    fun getTeamLineupFlow(matchId: Long, team: String): Flow<List<LineupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLineup(lineup: LineupEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLineups(lineups: List<LineupEntity>)

    @Update
    suspend fun updateLineup(lineup: LineupEntity)

    @Query("UPDATE lineups SET isCaptain = 0 WHERE matchId = :matchId AND team = :team")
    suspend fun clearCaptainsForTeam(matchId: Long, team: String)

    @Query("SELECT * FROM lineups")
    suspend fun getAllLineups(): List<LineupEntity>

    @Delete
    suspend fun deleteLineup(lineup: LineupEntity)

    @Query("DELETE FROM lineups WHERE id = :id")
    suspend fun deleteLineupById(id: Long)

    @Query("DELETE FROM lineups WHERE matchId = :matchId")
    suspend fun deleteLineupsForMatch(matchId: Long)
}
