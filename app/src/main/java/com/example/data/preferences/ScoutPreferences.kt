package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.scoutDataStore: DataStore<Preferences> by preferencesDataStore(name = "scout_preferences")

class ScoutPreferences(private val context: Context) {
    companion object {
        val SCOUT_NAME_KEY = stringPreferencesKey("scout_name")
        val REDUCE_MOTION_KEY = booleanPreferencesKey("reduce_motion")
        val HIGH_CONTRAST_KEY = booleanPreferencesKey("high_contrast")
        val LARGER_TEXT_KEY = booleanPreferencesKey("larger_text")
        val AI_SUMMARY_KEY = booleanPreferencesKey("ai_summary_enabled")
    }

    val scoutNameFlow: Flow<String> = context.scoutDataStore.data.map { prefs ->
        prefs[SCOUT_NAME_KEY] ?: ""
    }

    val reduceMotionFlow: Flow<Boolean> = context.scoutDataStore.data.map { prefs ->
        prefs[REDUCE_MOTION_KEY] ?: false
    }

    val highContrastFlow: Flow<Boolean> = context.scoutDataStore.data.map { prefs ->
        prefs[HIGH_CONTRAST_KEY] ?: false
    }

    val largerTextFlow: Flow<Boolean> = context.scoutDataStore.data.map { prefs ->
        prefs[LARGER_TEXT_KEY] ?: false
    }

    val aiSummaryFlow: Flow<Boolean> = context.scoutDataStore.data.map { prefs ->
        prefs[AI_SUMMARY_KEY] ?: false
    }

    suspend fun setScoutName(name: String) {
        context.scoutDataStore.edit { prefs ->
            prefs[SCOUT_NAME_KEY] = name
        }
    }

    suspend fun setReduceMotion(enabled: Boolean) {
        context.scoutDataStore.edit { prefs ->
            prefs[REDUCE_MOTION_KEY] = enabled
        }
    }

    suspend fun setHighContrast(enabled: Boolean) {
        context.scoutDataStore.edit { prefs ->
            prefs[HIGH_CONTRAST_KEY] = enabled
        }
    }

    suspend fun setLargerText(enabled: Boolean) {
        context.scoutDataStore.edit { prefs ->
            prefs[LARGER_TEXT_KEY] = enabled
        }
    }

    suspend fun setAiSummary(enabled: Boolean) {
        context.scoutDataStore.edit { prefs ->
            prefs[AI_SUMMARY_KEY] = enabled
        }
    }
}
