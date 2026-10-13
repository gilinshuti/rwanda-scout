package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lineups",
    foreignKeys = [
        ForeignKey(
            entity = MatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["matchId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["matchId"])]
)
data class LineupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val matchId: Long,
    val team: String, // "HOME" or "AWAY"
    val playerName: String,
    val jerseyNumber: Int,
    val position: String, // "GK", "DEF", "MID", "FWD"
    val isCaptain: Boolean = false,
    val isStarter: Boolean = true
)
