package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ballTaps",
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
data class BallTapEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val matchId: Long,
    val x: Float, // normalized 0f..1f across pitch
    val y: Float, // normalized 0f..1f across pitch
    val side: String, // "HOME" or "AWAY"
    val isDangerous: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)
