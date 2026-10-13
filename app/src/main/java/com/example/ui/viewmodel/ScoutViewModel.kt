package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ScoutApplication
import com.example.data.ai.GeminiSummaryService
import com.example.data.ai.NoInternetException
import com.example.data.ai.SummaryGenerator
import com.example.data.model.BallTapEntity
import com.example.data.model.EventEntity
import com.example.data.model.LineupEntity
import com.example.data.model.MatchEntity
import com.example.data.model.SummaryEntity
import com.example.data.preferences.ScoutPreferences
import com.example.data.repository.ScoutRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

enum class MatchFilter {
    ALL,
    SCHEDULED,
    LIVE,
    FINISHED
}

class ScoutViewModel(
    application: Application,
    private val repository: ScoutRepository
) : AndroidViewModel(application) {

    // Filter
    private val _selectedFilter = MutableStateFlow(MatchFilter.ALL)
    val selectedFilter: StateFlow<MatchFilter> = _selectedFilter.asStateFlow()

    // Matches List
    val allMatches: StateFlow<List<MatchEntity>> = repository.getAllMatches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredMatches: StateFlow<List<MatchEntity>> = combine(allMatches, _selectedFilter) { list, filter ->
        when (filter) {
            MatchFilter.ALL -> list
            MatchFilter.SCHEDULED -> list.filter { it.status.equals("SCHEDULED", ignoreCase = true) }
            MatchFilter.LIVE -> list.filter { it.status.equals("LIVE", ignoreCase = true) }
            MatchFilter.FINISHED -> list.filter { it.status.equals("FINISHED", ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Database size info
    private val _storageSizeMb = MutableStateFlow("0.2 MB")
    val storageSizeMb: StateFlow<String> = _storageSizeMb.asStateFlow()

    // Active Match Detail
    private val _activeMatchId = MutableStateFlow<Long?>(null)
    val activeMatchId: StateFlow<Long?> = _activeMatchId.asStateFlow()

    private val _activeMatch = MutableStateFlow<MatchEntity?>(null)
    val activeMatch: StateFlow<MatchEntity?> = _activeMatch.asStateFlow()

    private val _activeLineups = MutableStateFlow<List<LineupEntity>>(emptyList())
    val activeLineups: StateFlow<List<LineupEntity>> = _activeLineups.asStateFlow()

    private val _activeEvents = MutableStateFlow<List<EventEntity>>(emptyList())
    val activeEvents: StateFlow<List<EventEntity>> = _activeEvents.asStateFlow()

    private val _activeBallTaps = MutableStateFlow<List<BallTapEntity>>(emptyList())
    val activeBallTaps: StateFlow<List<BallTapEntity>> = _activeBallTaps.asStateFlow()

    private val _activeSummary = MutableStateFlow<SummaryEntity?>(null)
    val activeSummary: StateFlow<SummaryEntity?> = _activeSummary.asStateFlow()

    // Match Timer State
    private var timerJob: Job? = null
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _timerSeconds = MutableStateFlow(0)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    // Possession: "HOME" or "AWAY"
    private val _currentPossession = MutableStateFlow("HOME")
    val currentPossession: StateFlow<String> = _currentPossession.asStateFlow()

    // Advanced Player Data setting
    private val _advancedPlayerDataEnabled = MutableStateFlow(false)
    val advancedPlayerDataEnabled: StateFlow<Boolean> = _advancedPlayerDataEnabled.asStateFlow()

    // Total Events count for Data Management
    val totalEventsCount: StateFlow<Int> = repository.getTotalEventCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // DataStore Preferences
    private val scoutPreferences = ScoutPreferences(application)
    val scoutName: StateFlow<String> = scoutPreferences.scoutNameFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")
    val reduceMotion: StateFlow<Boolean> = scoutPreferences.reduceMotionFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val highContrast: StateFlow<Boolean> = scoutPreferences.highContrastFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val largerText: StateFlow<Boolean> = scoutPreferences.largerTextFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val isAiSummaryEnabled: StateFlow<Boolean> = scoutPreferences.aiSummaryFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _isGeneratingSummary = MutableStateFlow(false)
    val isGeneratingSummary: StateFlow<Boolean> = _isGeneratingSummary.asStateFlow()

    fun setScoutName(name: String) {
        viewModelScope.launch { scoutPreferences.setScoutName(name) }
    }

    fun setReduceMotion(enabled: Boolean) {
        viewModelScope.launch { scoutPreferences.setReduceMotion(enabled) }
    }

    fun setHighContrast(enabled: Boolean) {
        viewModelScope.launch { scoutPreferences.setHighContrast(enabled) }
    }

    fun setLargerText(enabled: Boolean) {
        viewModelScope.launch { scoutPreferences.setLargerText(enabled) }
    }

    fun setAiSummaryEnabled(enabled: Boolean) {
        viewModelScope.launch { scoutPreferences.setAiSummary(enabled) }
    }

    init {
        calculateStorage()
    }

    fun setFilter(filter: MatchFilter) {
        _selectedFilter.value = filter
    }

    fun calculateStorage() {
        viewModelScope.launch {
            try {
                val dbFile = getApplication<Application>().getDatabasePath("rwanda_scout_database")
                val bytes = if (dbFile.exists()) dbFile.length() else 45000L
                val mb = (bytes / (1024.0 * 1024.0)).coerceAtLeast(0.1)
                _storageSizeMb.value = String.format("%.2f MB", mb)
            } catch (e: Exception) {
                _storageSizeMb.value = "0.2 MB"
            }
        }
    }

    fun loadMatch(matchId: Long) {
        _activeMatchId.value = matchId
        viewModelScope.launch {
            repository.getMatchFlow(matchId).collect { match ->
                _activeMatch.value = match
                match?.let {
                    _timerSeconds.value = it.accumulatedSeconds
                }
            }
        }
        viewModelScope.launch {
            repository.getLineupsFlow(matchId).collect { lineups ->
                _activeLineups.value = lineups
            }
        }
        viewModelScope.launch {
            repository.getEventsFlow(matchId).collect { events ->
                _activeEvents.value = events
            }
        }
        viewModelScope.launch {
            repository.getBallTapsFlow(matchId).collect { taps ->
                _activeBallTaps.value = taps
            }
        }
        viewModelScope.launch {
            repository.getSummaryFlow(matchId).collect { summary ->
                _activeSummary.value = summary
            }
        }
    }

    fun createMatch(
        homeTeam: String,
        awayTeam: String,
        competition: String,
        venue: String,
        dateMillis: Long,
        kickoffMillis: Long,
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val newMatch = MatchEntity(
                homeTeam = homeTeam.trim(),
                awayTeam = awayTeam.trim(),
                competition = competition.trim(),
                venue = venue.trim(),
                matchDateMillis = dateMillis,
                kickoffTimeMillis = kickoffMillis,
                status = "SCHEDULED",
                currentPeriod = "NOT_STARTED"
            )
            val id = repository.insertMatch(newMatch)
            // Pre-seed default lineups if known Rwandan teams
            repository.populatePresetLineup(id, "HOME", homeTeam)
            repository.populatePresetLineup(id, "AWAY", awayTeam)
            calculateStorage()
            onCreated(id)
        }
    }

    fun deleteMatch(id: Long) {
        viewModelScope.launch {
            stopTimer()
            repository.deleteMatchById(id)
            if (_activeMatchId.value == id) {
                _activeMatchId.value = null
                _activeMatch.value = null
            }
            calculateStorage()
        }
    }

    fun updateMatchDetails(match: MatchEntity) {
        viewModelScope.launch {
            repository.updateMatch(match)
            _activeMatch.value = match
        }
    }

    // Lineups
    fun addPlayer(
        matchId: Long,
        teamSide: String,
        name: String,
        jersey: Int,
        position: String,
        isCaptain: Boolean,
        isStarter: Boolean
    ) {
        viewModelScope.launch {
            if (isCaptain) {
                repository.clearCaptainsForTeam(matchId, teamSide)
            }
            val entity = LineupEntity(
                matchId = matchId,
                team = teamSide,
                playerName = name.trim(),
                jerseyNumber = jersey,
                position = position,
                isCaptain = isCaptain,
                isStarter = isStarter
            )
            repository.insertLineup(entity)
        }
    }

    fun toggleCaptain(player: LineupEntity) {
        viewModelScope.launch {
            val newIsCaptain = !player.isCaptain
            if (newIsCaptain) {
                repository.clearCaptainsForTeam(player.matchId, player.team)
            }
            repository.updateLineup(player.copy(isCaptain = newIsCaptain))
        }
    }

    fun deletePlayer(lineupId: Long) {
        viewModelScope.launch {
            repository.deleteLineupById(lineupId)
        }
    }

    fun autoFillLineup(matchId: Long, teamSide: String, teamName: String) {
        viewModelScope.launch {
            repository.populatePresetLineup(matchId, teamSide, teamName)
        }
    }

    fun setPossession(side: String) {
        _currentPossession.value = side
    }

    // Pre-Match
    fun updateMatchReadiness(
        onPitch: Boolean? = null,
        warmingUp: Boolean? = null,
        aboutToStart: Boolean? = null
    ) {
        val current = _activeMatch.value ?: return
        val updated = current.copy(
            prePlayersOnPitch = onPitch ?: current.prePlayersOnPitch,
            prePlayersWarmingUp = warmingUp ?: current.prePlayersWarmingUp,
            preAboutToStart = aboutToStart ?: current.preAboutToStart
        )
        updateMatchDetails(updated)
    }

    fun updatePreMatchConditions(
        weather: String? = null,
        weatherIntensity: Int? = null,
        fog: Boolean? = null,
        wind: String? = null,
        pitchCondition: String? = null,
        homeColor: String? = null,
        awayColor: String? = null
    ) {
        val current = _activeMatch.value ?: return
        val updated = current.copy(
            weather = weather ?: current.weather,
            weatherIntensity = weatherIntensity ?: current.weatherIntensity,
            fog = fog ?: current.fog,
            wind = wind ?: current.wind,
            pitchCondition = pitchCondition ?: current.pitchCondition,
            homeColor = homeColor ?: current.homeColor,
            awayColor = awayColor ?: current.awayColor
        )
        updateMatchDetails(updated)
    }

    fun updatePreMatchInfo(
        weather: String,
        pitchCondition: String,
        homeColor: String,
        awayColor: String,
        onPitch: Boolean,
        warmingUp: Boolean,
        aboutToStart: Boolean
    ) {
        val current = _activeMatch.value ?: return
        val updated = current.copy(
            weather = weather,
            pitchCondition = pitchCondition,
            homeColor = homeColor,
            awayColor = awayColor,
            prePlayersOnPitch = onPitch,
            prePlayersWarmingUp = warmingUp,
            preAboutToStart = aboutToStart
        )
        updateMatchDetails(updated)
    }

    // Kickoff
    fun startKickoff(kickoffTeam: String, leftTeamSide: String) {
        val current = _activeMatch.value ?: return
        val now = System.currentTimeMillis()
        val updated = current.copy(
            status = "LIVE",
            currentPeriod = "FIRST_HALF",
            startedAtMillis = now,
            periodStartedAtMillis = now,
            accumulatedSeconds = 0,
            kickoffTeam = kickoffTeam,
            leftTeamSide = leftTeamSide
        )
        viewModelScope.launch {
            repository.updateMatch(updated)
            _activeMatch.value = updated
            _timerSeconds.value = 0
            _currentPossession.value = kickoffTeam.uppercase()

            // Insert KICKOFF event: matchTimeSeconds=0, matchTimeDisplay="00:00", period='FIRST_HALF'
            val kickoffTeamName = if (kickoffTeam.equals("HOME", ignoreCase = true)) updated.homeTeam else updated.awayTeam
            val kickoffEvent = EventEntity(
                matchId = updated.id,
                eventType = "KICKOFF",
                team = kickoffTeam,
                playerName = "$kickoffTeamName Kickoff",
                matchTimeSeconds = 0,
                matchTimeDisplay = "00:00",
                period = "FIRST_HALF"
            )
            repository.insertEvent(kickoffEvent)
        }
        startTimer()
    }

    // Timer and Periods
    fun startTimer() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true
        timerJob = viewModelScope.launch {
            while (_isTimerRunning.value) {
                delay(1000L)
                _timerSeconds.value += 1
                val current = _activeMatch.value
                if (current != null && _timerSeconds.value % 15 == 0) {
                    repository.updateMatch(current.copy(accumulatedSeconds = _timerSeconds.value))
                }
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        timerJob = null
        val current = _activeMatch.value ?: return
        viewModelScope.launch {
            repository.updateMatch(current.copy(accumulatedSeconds = _timerSeconds.value))
        }
    }

    fun stopTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        timerJob = null
    }

    fun setPeriod(period: String) {
        val current = _activeMatch.value ?: return
        val updated = current.copy(
            currentPeriod = period,
            accumulatedSeconds = _timerSeconds.value
        )
        updateMatchDetails(updated)
    }

    fun finishMatch() {
        pauseTimer()
        val current = _activeMatch.value ?: return
        val updated = current.copy(
            status = "FINISHED",
            currentPeriod = "FULL_TIME",
            accumulatedSeconds = _timerSeconds.value
        )
        updateMatchDetails(updated)
        generateSummary(current.id)
    }

    // Live Event Logging
    fun setAdvancedPlayerDataEnabled(enabled: Boolean) {
        _advancedPlayerDataEnabled.value = enabled
    }

    fun getExactMatchSeconds(): Int {
        val current = _activeMatch.value ?: return _timerSeconds.value
        val started = current.periodStartedAtMillis
        val delta = if (started != null && started > 0) ((System.currentTimeMillis() - started) / 1000).toInt().coerceAtLeast(0) else 0
        return maxOf(_timerSeconds.value, current.accumulatedSeconds + delta)
    }

    fun formatExactMatchTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    fun confirmGoalWorkflow(
        teamSide: String,
        howScored: String,
        isDoubtfulMinute: Boolean,
        isDoubtfulScorer: Boolean,
        isInsideBox: Boolean,
        isDoubtfulGoal: Boolean,
        scorerName: String,
        assistName: String?,
        ballX: Float?,
        ballY: Float?,
        onSuccess: (timeDisplay: String, newHomeScore: Int, newAwayScore: Int) -> Unit
    ) {
        val current = _activeMatch.value ?: return
        val exactSec = getExactMatchSeconds()
        val timeDisplay = formatExactMatchTime(exactSec)

        val newHomeScore = if (teamSide.equals("HOME", ignoreCase = true)) current.homeScore + 1 else current.homeScore
        val newAwayScore = if (teamSide.equals("AWAY", ignoreCase = true)) current.awayScore + 1 else current.awayScore

        // When a team scores, switch possession to the conceding team and restart at pitch center (0.5, 0.5)
        val nextPossession = if (teamSide.equals("HOME", ignoreCase = true)) "AWAY" else "HOME"
        setPossession(nextPossession)
        logBallTap(x = 0.5f, y = 0.5f, side = nextPossession, isDangerous = false)

        viewModelScope.launch {
            // Update Room Match
            val updatedMatch = current.copy(
                homeScore = newHomeScore,
                awayScore = newAwayScore,
                accumulatedSeconds = exactSec
            )
            repository.updateMatch(updatedMatch)
            _activeMatch.value = updatedMatch

            // Extra Data JSON with doubtfulGoal, howScored, insideBox, assist
            val extraJson = buildString {
                append("{")
                append("\"howScored\":\"$howScored\",")
                append("\"insideBox\":$isInsideBox,")
                append("\"doubtfulGoal\":$isDoubtfulGoal,")
                append("\"doubtfulMinute\":$isDoubtfulMinute,")
                if (!assistName.isNullOrBlank()) {
                    append("\"assist\":\"$assistName\",")
                }
                append("\"scorer\":\"$scorerName\"")
                append("}")
            }

            // Insert GOAL event into Room
            val goalEvent = EventEntity(
                matchId = current.id,
                eventType = "GOAL",
                team = teamSide.uppercase(),
                playerName = scorerName,
                matchTimeSeconds = exactSec,
                matchTimeDisplay = timeDisplay,
                period = current.currentPeriod,
                extraDataJson = extraJson,
                ballX = ballX,
                ballY = ballY,
                isDoubtful = isDoubtfulMinute || isDoubtfulGoal,
                isDoubtfulScorer = isDoubtfulScorer
            )
            repository.insertEvent(goalEvent)

            onSuccess(timeDisplay, newHomeScore, newAwayScore)
        }
    }

    fun logGoalCancelled(teamSide: String, reason: String) {
        val current = _activeMatch.value ?: return
        val exactSec = getExactMatchSeconds()
        val timeDisplay = formatExactMatchTime(exactSec)

        val event = EventEntity(
            matchId = current.id,
            eventType = "GOAL_CANCELLED",
            team = teamSide.uppercase(),
            playerName = "Goal Cancelled: $reason",
            matchTimeSeconds = exactSec,
            matchTimeDisplay = timeDisplay,
            period = current.currentPeriod,
            extraDataJson = "{\"reason\":\"$reason\"}"
        )
        viewModelScope.launch {
            repository.insertEvent(event)
        }
    }

    fun logAfterGoalKickoff() {
        val current = _activeMatch.value ?: return
        val exactSec = getExactMatchSeconds()
        val timeDisplay = formatExactMatchTime(exactSec)

        // Ensure ball tap is logged at exact center spot (0.5, 0.5)
        logBallTap(x = 0.5f, y = 0.5f, side = _currentPossession.value, isDangerous = false)

        val event = EventEntity(
            matchId = current.id,
            eventType = "AFTER_GOAL_KICKOFF",
            team = _currentPossession.value,
            playerName = "Kickoff After Goal",
            matchTimeSeconds = exactSec,
            matchTimeDisplay = timeDisplay,
            period = current.currentPeriod,
            ballX = 0.5f,
            ballY = 0.5f
        )
        viewModelScope.launch {
            repository.insertEvent(event)
        }
    }

    // Yellow Card Tracking per player
    fun getPlayerYellowCardCount(playerName: String): Int {
        val events = _activeEvents.value
        return events.count {
            it.playerName.equals(playerName, ignoreCase = true) &&
            (it.eventType == "YELLOW_CARD" ||
             (it.eventType == "CARD" && it.extraDataJson?.contains("Yellow", ignoreCase = true) == true &&
              !it.extraDataJson.contains("Yellow-Red", ignoreCase = true)))
        }
    }

    fun logCardEvent(
        teamSide: String,
        playerName: String,
        cardType: String, // "Yellow", "Yellow-Red", "Red"
        alsoAwardFreeKick: Boolean,
        ballX: Float?,
        ballY: Float?
    ) {
        val current = _activeMatch.value ?: return
        val exactSec = getExactMatchSeconds()
        val timeDisplay = formatExactMatchTime(exactSec)

        viewModelScope.launch {
            val cardEvent = EventEntity(
                matchId = current.id,
                eventType = "CARD",
                team = teamSide.uppercase(),
                playerName = playerName,
                matchTimeSeconds = exactSec,
                matchTimeDisplay = timeDisplay,
                period = current.currentPeriod,
                ballX = ballX,
                ballY = ballY,
                extraDataJson = "{\"cardType\":\"$cardType\"}"
            )
            repository.insertEvent(cardEvent)

            if (alsoAwardFreeKick) {
                val opposingTeam = if (teamSide.equals("HOME", ignoreCase = true)) "AWAY" else "HOME"
                val fkEvent = EventEntity(
                    matchId = current.id,
                    eventType = "FREE_KICK",
                    team = opposingTeam,
                    playerName = "$opposingTeam Free Kick",
                    matchTimeSeconds = exactSec,
                    matchTimeDisplay = timeDisplay,
                    period = current.currentPeriod,
                    ballX = ballX,
                    ballY = ballY
                )
                repository.insertEvent(fkEvent)
            }
        }
    }

    fun logCornerEvent(
        teamSide: String,
        takerName: String?,
        ballX: Float?,
        ballY: Float?
    ) {
        val current = _activeMatch.value ?: return
        val exactSec = getExactMatchSeconds()
        val timeDisplay = formatExactMatchTime(exactSec)

        val player = takerName?.ifBlank { null } ?: "$teamSide Corner"
        viewModelScope.launch {
            val event = EventEntity(
                matchId = current.id,
                eventType = "CORNER",
                team = teamSide.uppercase(),
                playerName = player,
                matchTimeSeconds = exactSec,
                matchTimeDisplay = timeDisplay,
                period = current.currentPeriod,
                ballX = ballX,
                ballY = ballY
            )
            repository.insertEvent(event)
        }
    }

    fun logPenaltyEvent(
        teamSide: String,
        cause: String,
        booking: String,
        playerInvolved: String?,
        ballX: Float?,
        ballY: Float?
    ) {
        val current = _activeMatch.value ?: return
        val exactSec = getExactMatchSeconds()
        val timeDisplay = formatExactMatchTime(exactSec)

        viewModelScope.launch {
            val penaltyEvent = EventEntity(
                matchId = current.id,
                eventType = "PENALTY",
                team = teamSide.uppercase(),
                playerName = "$teamSide Penalty ($cause)",
                matchTimeSeconds = exactSec,
                matchTimeDisplay = timeDisplay,
                period = current.currentPeriod,
                ballX = ballX,
                ballY = ballY,
                extraDataJson = "{\"cause\":\"$cause\",\"booking\":\"$booking\"}"
            )
            repository.insertEvent(penaltyEvent)

            if (booking != "No card" && booking.isNotBlank()) {
                val cardType = when (booking) {
                    "Yellow-red" -> "Yellow-Red"
                    "Red" -> "Red"
                    else -> "Yellow"
                }
                val fouledTeam = if (teamSide.equals("HOME", ignoreCase = true)) "AWAY" else "HOME"
                val cardEvent = EventEntity(
                    matchId = current.id,
                    eventType = "CARD",
                    team = fouledTeam,
                    playerName = playerInvolved?.ifBlank { null } ?: "$fouledTeam Defender",
                    matchTimeSeconds = exactSec,
                    matchTimeDisplay = timeDisplay,
                    period = current.currentPeriod,
                    ballX = ballX,
                    ballY = ballY,
                    extraDataJson = "{\"cardType\":\"$cardType\",\"incident\":\"Penalty $cause\"}"
                )
                repository.insertEvent(cardEvent)
            }
        }
    }

    fun logPenaltyResolution(
        teamSide: String,
        outcome: String,
        ballX: Float?,
        ballY: Float?
    ) {
        val current = _activeMatch.value ?: return
        val exactSec = getExactMatchSeconds()
        val timeDisplay = formatExactMatchTime(exactSec)

        viewModelScope.launch {
            val event = EventEntity(
                matchId = current.id,
                eventType = "PENALTY_RESOLUTION",
                team = teamSide.uppercase(),
                playerName = "Penalty: $outcome",
                matchTimeSeconds = exactSec,
                matchTimeDisplay = timeDisplay,
                period = current.currentPeriod,
                ballX = ballX,
                ballY = ballY,
                extraDataJson = "{\"outcome\":\"$outcome\"}"
            )
            repository.insertEvent(event)
        }
    }

    fun logFreeKickEvent(
        teamSide: String,
        cause: String,
        isDangerous: Boolean,
        booking: String?,
        ballX: Float?,
        ballY: Float?
    ) {
        val current = _activeMatch.value ?: return
        val exactSec = getExactMatchSeconds()
        val timeDisplay = formatExactMatchTime(exactSec)

        val eventType = if (isDangerous) "DANGEROUS_FREE_KICK" else "FREE_KICK"
        viewModelScope.launch {
            val fkEvent = EventEntity(
                matchId = current.id,
                eventType = eventType,
                team = teamSide.uppercase(),
                playerName = "$teamSide ${if (isDangerous) "Dangerous Free Kick" else "Free Kick"} ($cause)",
                matchTimeSeconds = exactSec,
                matchTimeDisplay = timeDisplay,
                period = current.currentPeriod,
                ballX = ballX,
                ballY = ballY,
                extraDataJson = "{\"cause\":\"$cause\",\"isDangerous\":$isDangerous}"
            )
            repository.insertEvent(fkEvent)

            if (!booking.isNullOrBlank() && booking != "No card") {
                val cardType = when (booking) {
                    "Yellow-red" -> "Yellow-Red"
                    "Red" -> "Red"
                    else -> "Yellow"
                }
                val opposingTeam = if (teamSide.equals("HOME", ignoreCase = true)) "AWAY" else "HOME"
                val cardEvent = EventEntity(
                    matchId = current.id,
                    eventType = "CARD",
                    team = opposingTeam,
                    playerName = "$opposingTeam Player",
                    matchTimeSeconds = exactSec,
                    matchTimeDisplay = timeDisplay,
                    period = current.currentPeriod,
                    ballX = ballX,
                    ballY = ballY,
                    extraDataJson = "{\"cardType\":\"$cardType\"}"
                )
                repository.insertEvent(cardEvent)
            }
        }
    }

    fun logDfkResolution(
        teamSide: String,
        outcome: String,
        isGkSave: Boolean,
        ballX: Float?,
        ballY: Float?
    ) {
        val current = _activeMatch.value ?: return
        val exactSec = getExactMatchSeconds()
        val timeDisplay = formatExactMatchTime(exactSec)

        viewModelScope.launch {
            val event = EventEntity(
                matchId = current.id,
                eventType = "DFK_RESOLUTION",
                team = teamSide.uppercase(),
                playerName = "DFK Resolution: $outcome",
                matchTimeSeconds = exactSec,
                matchTimeDisplay = timeDisplay,
                period = current.currentPeriod,
                ballX = ballX,
                ballY = ballY,
                extraDataJson = "{\"outcome\":\"$outcome\",\"gkSave\":$isGkSave}"
            )
            repository.insertEvent(event)
        }
    }

    fun logEvent(
        eventType: String,
        teamSide: String,
        playerName: String,
        ballX: Float? = null,
        ballY: Float? = null,
        extraInfo: String? = null
    ) {
        val current = _activeMatch.value ?: return
        val sec = _timerSeconds.value
        val min = sec / 60
        val displayTime = "${min}'"

        viewModelScope.launch {
            val event = EventEntity(
                matchId = current.id,
                eventType = eventType,
                team = teamSide,
                playerName = playerName,
                matchTimeSeconds = sec,
                matchTimeDisplay = displayTime,
                period = current.currentPeriod,
                ballX = ballX,
                ballY = ballY,
                extraDataJson = extraInfo
            )
            repository.insertEvent(event)

            // Update scores automatically on goals
            if (eventType == "GOAL") {
                val newHomeScore = if (teamSide == "HOME") current.homeScore + 1 else current.homeScore
                val newAwayScore = if (teamSide == "AWAY") current.awayScore + 1 else current.awayScore
                val updatedMatch = current.copy(
                    homeScore = newHomeScore,
                    awayScore = newAwayScore,
                    accumulatedSeconds = sec
                )
                repository.updateMatch(updatedMatch)
                _activeMatch.value = updatedMatch

                // When goal scored, switch possession to the other team and restart at center (0.5, 0.5)
                val nextPossession = if (teamSide.equals("HOME", ignoreCase = true)) "AWAY" else "HOME"
                setPossession(nextPossession)
                logBallTap(x = 0.5f, y = 0.5f, side = nextPossession, isDangerous = false)
            } else if (eventType == "OFFSIDE") {
                // If flagged offside, change possession to the defending team directly
                val defendingTeam = if (teamSide.equals("AWAY", ignoreCase = true)) "HOME" else "AWAY"
                setPossession(defendingTeam)
            } else if (eventType == "GOAL_KICK") {
                // Goal kick switches possession
                val gkTeam = if (teamSide.isNotBlank()) teamSide.uppercase() else (if (_currentPossession.value.equals("HOME", ignoreCase = true)) "AWAY" else "HOME")
                setPossession(gkTeam)
            }
        }
    }

    fun deleteEvent(event: EventEntity) {
        val current = _activeMatch.value ?: return
        viewModelScope.launch {
            repository.deleteEvent(event)
            if (event.eventType == "GOAL") {
                val newHomeScore = if (event.team == "HOME") (current.homeScore - 1).coerceAtLeast(0) else current.homeScore
                val newAwayScore = if (event.team == "AWAY") (current.awayScore - 1).coerceAtLeast(0) else current.awayScore
                val updatedMatch = current.copy(homeScore = newHomeScore, awayScore = newAwayScore)
                repository.updateMatch(updatedMatch)
                _activeMatch.value = updatedMatch
            }
        }
    }

    // VAR Review Workflow Logic
    fun applyVarReviewOutcome(
        teamSide: String,
        reviewReason: String,
        outcome: String,
        ballX: Float? = null,
        ballY: Float? = null,
        onOutcomeApplied: (String) -> Unit
    ) {
        val current = _activeMatch.value ?: return
        val exactSec = getExactMatchSeconds()
        val timeDisplay = formatExactMatchTime(exactSec)

        viewModelScope.launch {
            val extraJson = "{\"reason\":\"$reviewReason\",\"outcome\":\"$outcome\",\"team\":\"$teamSide\"}"
            val varEvent = EventEntity(
                matchId = current.id,
                eventType = "VAR_REVIEW",
                team = teamSide.uppercase(),
                playerName = "VAR Review: $reviewReason -> $outcome",
                matchTimeSeconds = exactSec,
                matchTimeDisplay = timeDisplay,
                period = current.currentPeriod,
                extraDataJson = extraJson,
                ballX = ballX,
                ballY = ballY
            )
            repository.insertEvent(varEvent)

            when (outcome) {
                "No Goal" -> {
                    // Delete latest GOAL event + decrement score
                    val allEvents = repository.getEvents(current.id)
                    val lastGoal = allEvents.findLast { it.eventType == "GOAL" && (teamSide == "UNKNOWN" || it.team.equals(teamSide, ignoreCase = true)) }
                        ?: allEvents.findLast { it.eventType == "GOAL" }

                    if (lastGoal != null) {
                        repository.deleteEvent(lastGoal)
                        val decTeam = lastGoal.team
                        val newHomeScore = if (decTeam.equals("HOME", ignoreCase = true)) (current.homeScore - 1).coerceAtLeast(0) else current.homeScore
                        val newAwayScore = if (decTeam.equals("AWAY", ignoreCase = true)) (current.awayScore - 1).coerceAtLeast(0) else current.awayScore
                        val updatedMatch = current.copy(homeScore = newHomeScore, awayScore = newAwayScore)
                        repository.updateMatch(updatedMatch)
                        _activeMatch.value = updatedMatch
                        onOutcomeApplied("Goal overturned! Score is now ${updatedMatch.homeScore}-${updatedMatch.awayScore}")
                    } else {
                        onOutcomeApplied("Goal overturned by VAR")
                    }
                }
                "No Penalty" -> {
                    val cancelEvent = EventEntity(
                        matchId = current.id,
                        eventType = "PENALTY_CANCELLED",
                        team = teamSide.uppercase(),
                        playerName = "VAR: Penalty Overturned",
                        matchTimeSeconds = exactSec,
                        matchTimeDisplay = timeDisplay,
                        period = current.currentPeriod,
                        ballX = ballX,
                        ballY = ballY
                    )
                    repository.insertEvent(cancelEvent)
                    onOutcomeApplied("Penalty cancelled by VAR")
                }
                "No Red card" -> {
                    val cancelEvent = EventEntity(
                        matchId = current.id,
                        eventType = "CARD_CANCELLED",
                        team = teamSide.uppercase(),
                        playerName = "VAR: Red Card Rescinded",
                        matchTimeSeconds = exactSec,
                        matchTimeDisplay = timeDisplay,
                        period = current.currentPeriod,
                        ballX = ballX,
                        ballY = ballY
                    )
                    repository.insertEvent(cancelEvent)
                    onOutcomeApplied("Red card rescinded by VAR")
                }
                "Mistaken identity" -> {
                    val miEvent = EventEntity(
                        matchId = current.id,
                        eventType = "MISTAKEN_IDENTITY",
                        team = teamSide.uppercase(),
                        playerName = "VAR: Mistaken Identity Confirmed",
                        matchTimeSeconds = exactSec,
                        matchTimeDisplay = timeDisplay,
                        period = current.currentPeriod,
                        ballX = ballX,
                        ballY = ballY
                    )
                    repository.insertEvent(miEvent)
                    onOutcomeApplied("Correct via Cards menu")
                }
                "No Corner" -> {
                    val defendingTeam = if (teamSide.equals("HOME", ignoreCase = true)) "AWAY" else "HOME"
                    val gkEvent = EventEntity(
                        matchId = current.id,
                        eventType = "GOAL_KICK",
                        team = defendingTeam,
                        playerName = "Goal Kick (Overturned Corner)",
                        matchTimeSeconds = exactSec,
                        matchTimeDisplay = timeDisplay,
                        period = current.currentPeriod,
                        ballX = ballX,
                        ballY = ballY
                    )
                    repository.insertEvent(gkEvent)
                    onOutcomeApplied("Corner overturned to Goal Kick")
                }
                else -> {
                    onOutcomeApplied("VAR: $outcome confirmed")
                }
            }
        }
    }

    // In-Play Action Helpers
    fun logThrowIn(teamSide: String, ballX: Float? = null, ballY: Float? = null) {
        val current = _activeMatch.value ?: return
        val exactSec = getExactMatchSeconds()
        val timeDisplay = formatExactMatchTime(exactSec)

        viewModelScope.launch {
            val event = EventEntity(
                matchId = current.id,
                eventType = "THROW_IN",
                team = teamSide.uppercase(),
                playerName = "$teamSide Throw-in",
                matchTimeSeconds = exactSec,
                matchTimeDisplay = timeDisplay,
                period = current.currentPeriod,
                ballX = ballX,
                ballY = ballY
            )
            repository.insertEvent(event)
        }
    }

    fun logGoalKick(teamSide: String, ballX: Float? = null, ballY: Float? = null) {
        val current = _activeMatch.value ?: return
        val exactSec = getExactMatchSeconds()
        val timeDisplay = formatExactMatchTime(exactSec)

        // Goal kick switches possession and sets ball position to goal kick area
        val isHomeOnLeft = (current.leftTeamSide ?: "HOME").equals("HOME", ignoreCase = true)
        val gkTeam = if (teamSide.isNotBlank()) teamSide.uppercase() else (if (_currentPossession.value.equals("HOME", ignoreCase = true)) "AWAY" else "HOME")
        setPossession(gkTeam)
        val gkX = ballX ?: (if (isHomeOnLeft) (if (gkTeam == "HOME") 0.08f else 0.92f) else (if (gkTeam == "HOME") 0.92f else 0.08f))
        val gkY = ballY ?: 0.5f
        logBallTap(x = gkX, y = gkY, side = gkTeam, isDangerous = false)

        viewModelScope.launch {
            val event = EventEntity(
                matchId = current.id,
                eventType = "GOAL_KICK",
                team = gkTeam,
                playerName = "$gkTeam Goal Kick",
                matchTimeSeconds = exactSec,
                matchTimeDisplay = timeDisplay,
                period = current.currentPeriod,
                ballX = gkX,
                ballY = gkY
            )
            repository.insertEvent(event)
        }
    }

    fun logOffside(teamSide: String, ballX: Float? = null, ballY: Float? = null) {
        val current = _activeMatch.value ?: return
        val exactSec = getExactMatchSeconds()
        val timeDisplay = formatExactMatchTime(exactSec)

        // If away team flagged offside, change possession to the home team directly!
        // If home team flagged offside, change possession to away team directly!
        val defendingTeam = if (teamSide.equals("AWAY", ignoreCase = true)) "HOME" else "AWAY"
        setPossession(defendingTeam)
        val isHomeOnLeft = (current.leftTeamSide ?: "HOME").equals("HOME", ignoreCase = true)
        val offsideX = ballX ?: (if (isHomeOnLeft) (if (teamSide.equals("AWAY", ignoreCase = true)) 0.35f else 0.65f) else (if (teamSide.equals("AWAY", ignoreCase = true)) 0.65f else 0.35f))
        val offsideY = ballY ?: 0.5f
        logBallTap(x = offsideX, y = offsideY, side = defendingTeam, isDangerous = false)

        viewModelScope.launch {
            val event = EventEntity(
                matchId = current.id,
                eventType = "OFFSIDE",
                team = teamSide.uppercase(),
                playerName = "$teamSide Offside",
                matchTimeSeconds = exactSec,
                matchTimeDisplay = timeDisplay,
                period = current.currentPeriod,
                ballX = offsideX,
                ballY = offsideY
            )
            repository.insertEvent(event)
        }
    }

    fun logShotEvent(
        teamSide: String,
        shotCategory: String,
        isHeader: Boolean,
        isPost: Boolean,
        isBar: Boolean,
        isInsideBox: Boolean,
        outcome: String?,
        ballX: Float? = null,
        ballY: Float? = null,
        onGoalScored: (() -> Unit)? = null
    ) {
        val current = _activeMatch.value ?: return
        val exactSec = getExactMatchSeconds()
        val timeDisplay = formatExactMatchTime(exactSec)

        val extraJson = buildString {
            append("{")
            append("\"shotCategory\":\"$shotCategory\",")
            append("\"isHeader\":$isHeader,")
            append("\"isPost\":$isPost,")
            append("\"isBar\":$isBar,")
            append("\"isInsideBox\":$isInsideBox")
            if (!outcome.isNullOrBlank()) {
                append(",\"outcome\":\"$outcome\"")
            }
            append("}")
        }

        viewModelScope.launch {
            val event = EventEntity(
                matchId = current.id,
                eventType = "SHOT",
                team = teamSide.uppercase(),
                playerName = "$teamSide Shot: $shotCategory${if (outcome != null) " ($outcome)" else ""}",
                matchTimeSeconds = exactSec,
                matchTimeDisplay = timeDisplay,
                period = current.currentPeriod,
                extraDataJson = extraJson,
                ballX = ballX,
                ballY = ballY
            )
            repository.insertEvent(event)

            if (outcome == "Goal") {
                val newHome = if (teamSide.equals("HOME", ignoreCase = true)) current.homeScore + 1 else current.homeScore
                val newAway = if (teamSide.equals("AWAY", ignoreCase = true)) current.awayScore + 1 else current.awayScore
                val updated = current.copy(homeScore = newHome, awayScore = newAway)
                repository.updateMatch(updated)
                _activeMatch.value = updated

                // If team scores a goal, change possession to the opposite team and restart in center
                val nextPossession = if (teamSide.equals("HOME", ignoreCase = true)) "AWAY" else "HOME"
                setPossession(nextPossession)
                logBallTap(x = 0.5f, y = 0.5f, side = nextPossession, isDangerous = false)

                val goalEvent = EventEntity(
                    matchId = current.id,
                    eventType = "GOAL",
                    team = teamSide.uppercase(),
                    playerName = "$teamSide Goal (Shot)",
                    matchTimeSeconds = exactSec,
                    matchTimeDisplay = timeDisplay,
                    period = current.currentPeriod,
                    extraDataJson = extraJson,
                    ballX = ballX,
                    ballY = ballY
                )
                repository.insertEvent(goalEvent)
                onGoalScored?.invoke()
            } else if (outcome?.contains("GK save", ignoreCase = true) == true) {
                // Goalkeeper save -> defending team gets possession
                val defendingTeam = if (teamSide.equals("HOME", ignoreCase = true)) "AWAY" else "HOME"
                setPossession(defendingTeam)
                val restartX = if (defendingTeam == "HOME") 0.08f else 0.92f
                logBallTap(x = restartX, y = 0.5f, side = defendingTeam, isDangerous = false)
            } else if (outcome?.contains("Goal kick", ignoreCase = true) == true) {
                // Goal kick -> defending team gets possession
                val defendingTeam = if (teamSide.equals("HOME", ignoreCase = true)) "AWAY" else "HOME"
                setPossession(defendingTeam)
                val restartX = if (defendingTeam == "HOME") 0.08f else 0.92f
                logBallTap(x = restartX, y = 0.5f, side = defendingTeam, isDangerous = false)
            } else if (outcome?.contains("Corner", ignoreCase = true) == true) {
                // Corner kick -> attacking team maintains possession
                setPossession(teamSide.uppercase())
            } else if (outcome?.contains("Another shot", ignoreCase = true) == true) {
                // Rebound shot -> attacking team maintains possession
                setPossession(teamSide.uppercase())
            }
        }
    }

    // Ball Taps
    fun logBallTap(x: Float, y: Float, side: String, isDangerous: Boolean) {
        val current = _activeMatch.value ?: return
        viewModelScope.launch {
            val tap = BallTapEntity(
                matchId = current.id,
                x = x,
                y = y,
                side = side,
                isDangerous = isDangerous
            )
            repository.insertBallTap(tap)
        }
    }

    // Summary
    fun generateAiSummary(
        matchId: Long,
        onError: (message: String) -> Unit = {},
        onSuccess: () -> Unit = {}
    ) {
        if (_isGeneratingSummary.value) return
        _isGeneratingSummary.value = true
        viewModelScope.launch {
            try {
                val match = repository.getMatch(matchId) ?: throw IllegalStateException("Match not found")
                val events = repository.getEvents(matchId)
                val taps = repository.getBallTaps(matchId)
                val context = getApplication<Application>()

                val aiText = GeminiSummaryService.generateMatchSummary(context, match, events, taps)
                val summaryEntity = SummaryEntity(
                    matchId = matchId,
                    summaryText = aiText,
                    generatedAtMillis = System.currentTimeMillis()
                )
                repository.insertSummary(summaryEntity)
                _activeSummary.value = summaryEntity
                onSuccess()
            } catch (e: NoInternetException) {
                onError("AI summary requires internet")
            } catch (e: Exception) {
                onError(e.message ?: "Failed to generate AI summary")
            } finally {
                _isGeneratingSummary.value = false
            }
        }
    }

    fun generateSummary(matchId: Long) {
        viewModelScope.launch {
            val match = repository.getMatch(matchId) ?: return@launch
            val events = repository.getEvents(matchId)
            val taps = repository.getBallTaps(matchId)
            val text = SummaryGenerator.generateScoutSummary(match, events, taps)
            val summaryEntity = SummaryEntity(
                matchId = matchId,
                summaryText = text
            )
            repository.insertSummary(summaryEntity)
            _activeSummary.value = summaryEntity
        }
    }

    fun seedSampleDerbyMatch(onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.createSampleDerbyMatch()
            calculateStorage()
            onComplete(id)
        }
    }

    fun clearAllMatches() {
        clearAllData()
    }

    fun deleteMatch(matchId: Long, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            if (_activeMatchId.value == matchId) {
                stopTimer()
                _activeMatchId.value = null
                _activeMatch.value = null
                _activeEvents.value = emptyList()
                _activeLineups.value = emptyList()
                _activeBallTaps.value = emptyList()
                _activeSummary.value = null
            }
            repository.deleteMatchById(matchId)
            calculateStorage()
            onComplete?.invoke()
        }
    }

    fun clearAllData(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            stopTimer()
            _activeMatchId.value = null
            _activeMatch.value = null
            _activeEvents.value = emptyList()
            _activeLineups.value = emptyList()
            _activeBallTaps.value = emptyList()
            _activeSummary.value = null
            repository.clearAllData()
            calculateStorage()
            onComplete?.invoke()
        }
    }

    suspend fun exportAllDataJson(): String = withContext(Dispatchers.IO) {
        val matches = repository.getAllMatchesList()
        val lineups = repository.getAllLineups()
        val events = repository.getAllEvents()
        val ballTaps = repository.getAllBallTaps()
        val summaries = repository.getAllSummaries()

        val root = JSONObject().apply {
            put("version", 1)
            put("exportedAt", System.currentTimeMillis())

            val matchesArr = JSONArray()
            matches.forEach { m ->
                matchesArr.put(JSONObject().apply {
                    put("id", m.id)
                    put("homeTeam", m.homeTeam)
                    put("awayTeam", m.awayTeam)
                    put("competition", m.competition)
                    put("venue", m.venue)
                    put("matchDateMillis", m.matchDateMillis)
                    put("kickoffTimeMillis", m.kickoffTimeMillis)
                    put("status", m.status)
                    put("currentPeriod", m.currentPeriod)
                    put("homeScore", m.homeScore)
                    put("awayScore", m.awayScore)
                    put("homePenScore", m.homePenScore)
                    put("awayPenScore", m.awayPenScore)
                    put("accumulatedSeconds", m.accumulatedSeconds)
                    if (m.attendance != null) put("attendance", m.attendance)
                    if (m.weather != null) put("weather", m.weather)
                })
            }
            put("matches", matchesArr)

            val lineupsArr = JSONArray()
            lineups.forEach { l ->
                lineupsArr.put(JSONObject().apply {
                    put("id", l.id)
                    put("matchId", l.matchId)
                    put("team", l.team)
                    put("playerName", l.playerName)
                    put("jerseyNumber", l.jerseyNumber)
                    put("position", l.position)
                    put("isStarter", l.isStarter)
                    put("isCaptain", l.isCaptain)
                })
            }
            put("lineups", lineupsArr)

            val eventsArr = JSONArray()
            events.forEach { e ->
                eventsArr.put(JSONObject().apply {
                    put("id", e.id)
                    put("matchId", e.matchId)
                    put("eventType", e.eventType)
                    put("team", e.team)
                    put("playerName", e.playerName)
                    put("matchTimeSeconds", e.matchTimeSeconds)
                    put("matchTimeDisplay", e.matchTimeDisplay)
                    put("period", e.period)
                    if (e.extraDataJson != null) put("extraDataJson", e.extraDataJson)
                    if (e.ballX != null) put("ballX", e.ballX)
                    if (e.ballY != null) put("ballY", e.ballY)
                })
            }
            put("events", eventsArr)

            val tapsArr = JSONArray()
            ballTaps.forEach { t ->
                tapsArr.put(JSONObject().apply {
                    put("id", t.id)
                    put("matchId", t.matchId)
                    put("x", t.x)
                    put("y", t.y)
                    put("side", t.side)
                    put("isDangerous", t.isDangerous)
                })
            }
            put("ballTaps", tapsArr)

            val sumArr = JSONArray()
            summaries.forEach { s ->
                sumArr.put(JSONObject().apply {
                    put("id", s.id)
                    put("matchId", s.matchId)
                    put("summaryText", s.summaryText)
                    put("generatedAtMillis", s.generatedAtMillis)
                })
            }
            put("summaries", sumArr)
        }
        root.toString(2)
    }

    fun importDataJson(jsonString: String, onComplete: (success: Boolean, message: String) -> Unit) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val root = JSONObject(jsonString)
                    val matchesArr = root.optJSONArray("matches") ?: JSONArray()
                    val lineupsArr = root.optJSONArray("lineups") ?: JSONArray()
                    val eventsArr = root.optJSONArray("events") ?: JSONArray()
                    val tapsArr = root.optJSONArray("ballTaps") ?: JSONArray()
                    val summariesArr = root.optJSONArray("summaries") ?: JSONArray()

                    val matches = mutableListOf<MatchEntity>()
                    for (i in 0 until matchesArr.length()) {
                        val m = matchesArr.getJSONObject(i)
                        matches.add(MatchEntity(
                            id = m.optLong("id", 0),
                            homeTeam = m.optString("homeTeam", "Home"),
                            awayTeam = m.optString("awayTeam", "Away"),
                            competition = m.optString("competition", "Rwanda Premier League"),
                            venue = m.optString("venue", "Kigali"),
                            matchDateMillis = m.optLong("matchDateMillis", System.currentTimeMillis()),
                            kickoffTimeMillis = m.optLong("kickoffTimeMillis", System.currentTimeMillis()),
                            status = m.optString("status", "SCHEDULED"),
                            currentPeriod = m.optString("currentPeriod", "NOT_STARTED"),
                            homeScore = m.optInt("homeScore", 0),
                            awayScore = m.optInt("awayScore", 0),
                            homePenScore = m.optInt("homePenScore", 0),
                            awayPenScore = m.optInt("awayPenScore", 0),
                            accumulatedSeconds = m.optInt("accumulatedSeconds", 0),
                            attendance = if (m.has("attendance")) m.optInt("attendance") else null,
                            weather = if (m.has("weather")) m.optString("weather") else null
                        ))
                    }
                    repository.insertMatches(matches)

                    val lineups = mutableListOf<LineupEntity>()
                    for (i in 0 until lineupsArr.length()) {
                        val l = lineupsArr.getJSONObject(i)
                        lineups.add(LineupEntity(
                            id = l.optLong("id", 0),
                            matchId = l.optLong("matchId"),
                            team = l.optString("team"),
                            playerName = l.optString("playerName"),
                            jerseyNumber = l.optInt("jerseyNumber"),
                            position = l.optString("position"),
                            isStarter = l.optBoolean("isStarter", true),
                            isCaptain = l.optBoolean("isCaptain", false)
                        ))
                    }
                    repository.insertLineups(lineups)

                    val events = mutableListOf<EventEntity>()
                    for (i in 0 until eventsArr.length()) {
                        val e = eventsArr.getJSONObject(i)
                        events.add(EventEntity(
                            id = e.optLong("id", 0),
                            matchId = e.optLong("matchId"),
                            eventType = e.optString("eventType"),
                            team = e.optString("team"),
                            playerName = e.optString("playerName"),
                            matchTimeSeconds = e.optInt("matchTimeSeconds"),
                            matchTimeDisplay = e.optString("matchTimeDisplay"),
                            period = e.optString("period"),
                            extraDataJson = if (e.has("extraDataJson")) e.optString("extraDataJson") else null,
                            ballX = if (e.has("ballX")) e.optDouble("ballX").toFloat() else null,
                            ballY = if (e.has("ballY")) e.optDouble("ballY").toFloat() else null
                        ))
                    }
                    repository.insertEvents(events)

                    val taps = mutableListOf<BallTapEntity>()
                    for (i in 0 until tapsArr.length()) {
                        val t = tapsArr.getJSONObject(i)
                        taps.add(BallTapEntity(
                            id = t.optLong("id", 0),
                            matchId = t.optLong("matchId"),
                            x = t.optDouble("x").toFloat(),
                            y = t.optDouble("y").toFloat(),
                            side = t.optString("side"),
                            isDangerous = t.optBoolean("isDangerous", false)
                        ))
                    }
                    repository.insertBallTaps(taps)

                    val summaries = mutableListOf<SummaryEntity>()
                    for (i in 0 until summariesArr.length()) {
                        val s = summariesArr.getJSONObject(i)
                        summaries.add(SummaryEntity(
                            id = s.optLong("id", 0),
                            matchId = s.optLong("matchId"),
                            summaryText = s.optString("summaryText"),
                            generatedAtMillis = s.optLong("generatedAtMillis", System.currentTimeMillis())
                        ))
                    }
                    repository.insertSummaries(summaries)

                    calculateStorage()
                    Pair(true, "Imported ${matches.size} matches successfully")
                } catch (e: Exception) {
                    Pair(false, "Import failed: ${e.message}")
                }
            }
            onComplete(result.first, result.second)
        }
    }

    // General Events Helper Functions
    fun updateMatchStatusPeriod(option: String, onApplied: (() -> Unit)? = null) {
        val current = _activeMatch.value ?: return
        val now = System.currentTimeMillis()
        val exactSec = getExactMatchSeconds()
        val timeDisplay = formatExactMatchTime(exactSec)

        var nextPeriod = current.currentPeriod
        var nextStatus = current.status
        var nextPeriodStartedAtMillis: Long? = current.periodStartedAtMillis
        var nextAccumulatedSeconds = current.accumulatedSeconds
        var shouldStartTimer = false
        var shouldPauseTimer = false
        var shouldStopTimer = false

        when (option) {
            "Halftime" -> {
                nextPeriod = "HALF_TIME"
                nextAccumulatedSeconds = exactSec
                shouldPauseTimer = true
            }
            "2nd Half" -> {
                nextPeriod = "SECOND_HALF"
                nextPeriodStartedAtMillis = now
                nextAccumulatedSeconds = exactSec
                shouldStartTimer = true
            }
            "Awaiting OT" -> {
                nextPeriod = "AWAITING_OT"
                nextAccumulatedSeconds = exactSec
                shouldPauseTimer = true
            }
            "1st Half OT" -> {
                nextPeriod = "FIRST_HALF_OT"
                nextPeriodStartedAtMillis = now
                nextAccumulatedSeconds = exactSec
                shouldStartTimer = true
            }
            "Halftime OT" -> {
                nextPeriod = "HALF_TIME_OT"
                nextAccumulatedSeconds = exactSec
                shouldPauseTimer = true
            }
            "2nd Half OT" -> {
                nextPeriod = "SECOND_HALF_OT"
                nextPeriodStartedAtMillis = now
                nextAccumulatedSeconds = exactSec
                shouldStartTimer = true
            }
            "Awaiting penalties" -> {
                nextPeriod = "AWAITING_PENALTIES"
                nextAccumulatedSeconds = exactSec
                shouldPauseTimer = true
            }
            "Penalty shooting" -> {
                nextPeriod = "PENALTIES"
                nextPeriodStartedAtMillis = now
                nextAccumulatedSeconds = exactSec
                shouldStartTimer = true
            }
            "Ended" -> {
                nextPeriod = "FULL_TIME"
                nextStatus = "FINISHED"
                nextAccumulatedSeconds = exactSec
                shouldStopTimer = true
            }
            else -> {
                nextPeriod = option
            }
        }

        val updatedMatch = current.copy(
            status = nextStatus,
            currentPeriod = nextPeriod,
            periodStartedAtMillis = nextPeriodStartedAtMillis,
            accumulatedSeconds = nextAccumulatedSeconds
        )

        viewModelScope.launch {
            repository.updateMatch(updatedMatch)
            _activeMatch.value = updatedMatch
            _timerSeconds.value = nextAccumulatedSeconds

            if (shouldPauseTimer) pauseTimer()
            if (shouldStopTimer) {
                stopTimer()
                generateSummary(current.id)
            }
            if (shouldStartTimer) startTimer()

            val event = EventEntity(
                matchId = current.id,
                eventType = "STATUS_CHANGE",
                team = "NEUTRAL",
                playerName = "Status: $option",
                matchTimeSeconds = exactSec,
                matchTimeDisplay = timeDisplay,
                period = nextPeriod,
                extraDataJson = "{\"status\":\"$option\",\"period\":\"$nextPeriod\"}"
            )
            repository.insertEvent(event)
            onApplied?.invoke()
        }
    }

    fun logSubstitution(teamSide: String, playerCount: Int, isDelayed: Boolean, onApplied: (() -> Unit)? = null) {
        val current = _activeMatch.value ?: return
        val exactSec = getExactMatchSeconds()
        val timeDisplay = formatExactMatchTime(exactSec)

        viewModelScope.launch {
            val event = EventEntity(
                matchId = current.id,
                eventType = "SUBSTITUTION",
                team = teamSide.uppercase(),
                playerName = "$teamSide Sub: $playerCount player${if (playerCount > 1) "s" else ""}${if (isDelayed) " [DELAYED]" else ""}",
                matchTimeSeconds = exactSec,
                matchTimeDisplay = timeDisplay,
                period = current.currentPeriod,
                extraDataJson = "{\"playerCount\":$playerCount,\"isDelayed\":$isDelayed}"
            )
            repository.insertEvent(event)
            onApplied?.invoke()
        }
    }

    fun logInjury(teamSide: String, onApplied: (() -> Unit)? = null) {
        val current = _activeMatch.value ?: return
        val exactSec = getExactMatchSeconds()
        val timeDisplay = formatExactMatchTime(exactSec)

        viewModelScope.launch {
            val event = EventEntity(
                matchId = current.id,
                eventType = "INJURY",
                team = teamSide.uppercase(),
                playerName = "$teamSide Player Injury",
                matchTimeSeconds = exactSec,
                matchTimeDisplay = timeDisplay,
                period = current.currentPeriod
            )
            repository.insertEvent(event)
            onApplied?.invoke()
        }
    }

    fun logInjuryReturn(teamSide: String, onApplied: (() -> Unit)? = null) {
        val current = _activeMatch.value ?: return
        val exactSec = getExactMatchSeconds()
        val timeDisplay = formatExactMatchTime(exactSec)

        viewModelScope.launch {
            val event = EventEntity(
                matchId = current.id,
                eventType = "INJURY_RETURN",
                team = teamSide.uppercase(),
                playerName = "$teamSide Player Returned from Injury",
                matchTimeSeconds = exactSec,
                matchTimeDisplay = timeDisplay,
                period = current.currentPeriod
            )
            repository.insertEvent(event)
            onApplied?.invoke()
        }
    }

    fun saveAddedTime(minutes: Int, isFirstHalf: Boolean? = null, onApplied: (() -> Unit)? = null) {
        val current = _activeMatch.value ?: return
        val exactSec = getExactMatchSeconds()
        val timeDisplay = formatExactMatchTime(exactSec)
        val targetIsFirst = isFirstHalf ?: (current.currentPeriod in listOf("FIRST_HALF", "HALF_TIME"))

        val updated = if (targetIsFirst) {
            current.copy(addedTime1st = minutes)
        } else {
            current.copy(addedTime2nd = minutes)
        }

        viewModelScope.launch {
            repository.updateMatch(updated)
            _activeMatch.value = updated

            val halfLabel = if (targetIsFirst) "1st Half" else "2nd Half"
            val event = EventEntity(
                matchId = current.id,
                eventType = "ADDED_TIME",
                team = "NEUTRAL",
                playerName = "+$minutes min Added Time ($halfLabel)",
                matchTimeSeconds = exactSec,
                matchTimeDisplay = timeDisplay,
                period = current.currentPeriod,
                extraDataJson = "{\"minutes\":$minutes,\"half\":\"$halfLabel\"}"
            )
            repository.insertEvent(event)
            onApplied?.invoke()
        }
    }

    fun logSnapshot(filePath: String, onApplied: (() -> Unit)? = null) {
        val current = _activeMatch.value ?: return
        val exactSec = getExactMatchSeconds()
        val timeDisplay = formatExactMatchTime(exactSec)

        viewModelScope.launch {
            val event = EventEntity(
                matchId = current.id,
                eventType = "SNAPSHOT",
                team = "NEUTRAL",
                playerName = "Pitch Snapshot",
                matchTimeSeconds = exactSec,
                matchTimeDisplay = timeDisplay,
                period = current.currentPeriod,
                extraDataJson = "{\"filePath\":\"$filePath\"}"
            )
            repository.insertEvent(event)
            onApplied?.invoke()
        }
    }

    fun saveAttendance(attendanceNumber: Int, onApplied: (() -> Unit)? = null) {
        val current = _activeMatch.value ?: return
        val exactSec = getExactMatchSeconds()
        val timeDisplay = formatExactMatchTime(exactSec)

        val updated = current.copy(attendance = attendanceNumber)
        viewModelScope.launch {
            repository.updateMatch(updated)
            _activeMatch.value = updated

            val event = EventEntity(
                matchId = current.id,
                eventType = "ATTENDANCE",
                team = "NEUTRAL",
                playerName = "Attendance: $attendanceNumber",
                matchTimeSeconds = exactSec,
                matchTimeDisplay = timeDisplay,
                period = current.currentPeriod,
                extraDataJson = "{\"attendance\":$attendanceNumber}"
            )
            repository.insertEvent(event)
            onApplied?.invoke()
        }
    }

    fun startInterruption(reason: String, onApplied: (() -> Unit)? = null) {
        val current = _activeMatch.value ?: return
        val exactSec = getExactMatchSeconds()
        val timeDisplay = formatExactMatchTime(exactSec)
        pauseTimer()

        viewModelScope.launch {
            val event = EventEntity(
                matchId = current.id,
                eventType = "INTERRUPTION",
                team = "NEUTRAL",
                playerName = "Match Interrupted: $reason",
                matchTimeSeconds = exactSec,
                matchTimeDisplay = timeDisplay,
                period = current.currentPeriod,
                extraDataJson = "{\"reason\":\"$reason\"}"
            )
            repository.insertEvent(event)
            onApplied?.invoke()
        }
    }

    fun endInterruption(onApplied: (() -> Unit)? = null) {
        val current = _activeMatch.value ?: return
        val exactSec = getExactMatchSeconds()
        val timeDisplay = formatExactMatchTime(exactSec)
        startTimer()

        viewModelScope.launch {
            val event = EventEntity(
                matchId = current.id,
                eventType = "INTERRUPTION_END",
                team = "NEUTRAL",
                playerName = "Match Resumed",
                matchTimeSeconds = exactSec,
                matchTimeDisplay = timeDisplay,
                period = current.currentPeriod
            )
            repository.insertEvent(event)
            onApplied?.invoke()
        }
    }

    fun deleteEvent(eventId: Long, onApplied: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.deleteEventById(eventId)
            onApplied?.invoke()
        }
    }

    fun updateTeamColors(homeColor: String, awayColor: String, onApplied: (() -> Unit)? = null) {
        val current = _activeMatch.value ?: return
        val updated = current.copy(homeColor = homeColor, awayColor = awayColor)
        viewModelScope.launch {
            repository.updateMatch(updated)
            _activeMatch.value = updated
            onApplied?.invoke()
        }
    }

    fun toggleSides(onApplied: ((newLeft: String) -> Unit)? = null) {
        val current = _activeMatch.value ?: return
        val currentLeft = current.leftTeamSide ?: "HOME"
        val newLeft = if (currentLeft.equals("HOME", ignoreCase = true)) "AWAY" else "HOME"
        val updated = current.copy(leftTeamSide = newLeft)
        viewModelScope.launch {
            repository.updateMatch(updated)
            _activeMatch.value = updated
            onApplied?.invoke(newLeft)
        }
    }

    fun logRequestAssistance(onApplied: (() -> Unit)? = null) {
        val current = _activeMatch.value ?: return
        val exactSec = getExactMatchSeconds()
        val timeDisplay = formatExactMatchTime(exactSec)

        viewModelScope.launch {
            val event = EventEntity(
                matchId = current.id,
                eventType = "REQUEST_ASSISTANCE",
                team = "NEUTRAL",
                playerName = "Request Assistance",
                matchTimeSeconds = exactSec,
                matchTimeDisplay = timeDisplay,
                period = current.currentPeriod,
                extraDataJson = "{\"status\":\"SAVED_LOCALLY\"}"
            )
            repository.insertEvent(event)
            onApplied?.invoke()
        }
    }

    fun updateConditions(
        weather: String?,
        intensity: Int?,
        fog: Boolean,
        wind: String?,
        pitch: String?,
        homeColor: String?,
        awayColor: String?,
        onApplied: (() -> Unit)? = null
    ) {
        val current = _activeMatch.value ?: return
        val updated = current.copy(
            weather = weather ?: current.weather,
            weatherIntensity = intensity ?: current.weatherIntensity,
            fog = fog,
            wind = wind ?: current.wind,
            pitchCondition = pitch ?: current.pitchCondition,
            homeColor = homeColor ?: current.homeColor,
            awayColor = awayColor ?: current.awayColor
        )
        viewModelScope.launch {
            repository.updateMatch(updated)
            _activeMatch.value = updated
            onApplied?.invoke()
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val app = application as ScoutApplication
                    return ScoutViewModel(application, app.repository) as T
                }
            }
    }
}
