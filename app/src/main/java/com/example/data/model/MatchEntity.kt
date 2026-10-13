package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val homeTeam: String,
    val awayTeam: String,
    val competition: String,
    val venue: String,
    val matchDateMillis: Long,
    val kickoffTimeMillis: Long,
    val weather: String? = null,
    val weatherIntensity: Int? = null,
    val fog: Boolean = false,
    val wind: String? = null,
    val pitchCondition: String? = null,
    val homeColor: String? = null,
    val awayColor: String? = null,
    val status: String = "SCHEDULED", // "SCHEDULED", "LIVE", "FINISHED"
    val currentPeriod: String = "NOT_STARTED", // "NOT_STARTED", "FIRST_HALF", "HALF_TIME", "SECOND_HALF", "FULL_TIME", "EXTRA_TIME", "PENALTIES"
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val homePenScore: Int = 0,
    val awayPenScore: Int = 0,
    val startedAtMillis: Long? = null,
    val periodStartedAtMillis: Long? = null,
    val accumulatedSeconds: Int = 0,
    val addedTime1st: Int = 0,
    val addedTime2nd: Int = 0,
    val attendance: Int? = null,
    val resultLink: String? = null,
    val prePlayersOnPitch: Boolean = false,
    val prePlayersWarmingUp: Boolean = false,
    val preAboutToStart: Boolean = false,
    val kickoffTeam: String? = null, // e.g. "HOME" or "AWAY"
    val leftTeamSide: String? = null, // e.g. "HOME" or "AWAY"
    val createdAtMillis: Long = System.currentTimeMillis()
)
