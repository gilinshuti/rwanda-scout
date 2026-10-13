package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "events",
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
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val matchId: Long,
    val eventType: String, // "GOAL", "YELLOW_CARD", "RED_CARD", "SUBSTITUTION", "FOUL", "CORNER", "OFFSIDE", "SHOT_ON_TARGET", "SHOT_OFF_TARGET", "PENALTY", "SAVE"
    val team: String, // "HOME" or "AWAY"
    val playerName: String,
    val matchTimeSeconds: Int,
    val matchTimeDisplay: String, // e.g. "23'" or "45+2'"
    val period: String, // "FIRST_HALF", "SECOND_HALF", "EXTRA_TIME", "PENALTIES"
    val extraDataJson: String? = null, // e.g. sub in/out, card reason, assist
    val ballX: Float? = null,
    val ballY: Float? = null,
    val isDoubtful: Boolean = false,
    val isDoubtfulScorer: Boolean = false,
    val isPenaltyShootout: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)
