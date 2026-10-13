package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.BallTapDao
import com.example.data.dao.EventDao
import com.example.data.dao.LineupDao
import com.example.data.dao.MatchDao
import com.example.data.dao.SummaryDao
import com.example.data.model.BallTapEntity
import com.example.data.model.EventEntity
import com.example.data.model.LineupEntity
import com.example.data.model.MatchEntity
import com.example.data.model.SummaryEntity

@Database(
    entities = [
        MatchEntity::class,
        LineupEntity::class,
        EventEntity::class,
        BallTapEntity::class,
        SummaryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
    abstract fun lineupDao(): LineupDao
    abstract fun eventDao(): EventDao
    abstract fun ballTapDao(): BallTapDao
    abstract fun summaryDao(): SummaryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rwanda_scout_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
