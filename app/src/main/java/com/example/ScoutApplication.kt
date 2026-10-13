package com.example

import android.app.Application
import com.example.data.db.AppDatabase
import com.example.data.repository.ScoutRepository

class ScoutApplication : Application() {
    lateinit var database: AppDatabase
        private set
    lateinit var repository: ScoutRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        repository = ScoutRepository(database)
    }
}
