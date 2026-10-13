package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.BallTapEntity
import com.example.data.model.EventEntity
import com.example.data.model.LineupEntity
import com.example.data.model.MatchEntity
import com.example.data.model.SummaryEntity
import kotlinx.coroutines.flow.Flow

class ScoutRepository(private val database: AppDatabase) {
    private val matchDao = database.matchDao()
    private val lineupDao = database.lineupDao()
    private val eventDao = database.eventDao()
    private val ballTapDao = database.ballTapDao()
    private val summaryDao = database.summaryDao()

    // Matches
    fun getAllMatches(): Flow<List<MatchEntity>> = matchDao.getAllMatchesFlow()
    fun getMatchFlow(id: Long): Flow<MatchEntity?> = matchDao.getMatchByIdFlow(id)
    suspend fun getMatch(id: Long): MatchEntity? = matchDao.getMatchById(id)
    suspend fun insertMatch(match: MatchEntity): Long = matchDao.insertMatch(match)
    suspend fun updateMatch(match: MatchEntity) = matchDao.updateMatch(match)
    suspend fun deleteMatch(match: MatchEntity) = deleteMatchById(match.id)
    suspend fun deleteMatchById(id: Long) {
        eventDao.deleteEventsForMatch(id)
        ballTapDao.deleteBallTapsForMatch(id)
        lineupDao.deleteLineupsForMatch(id)
        summaryDao.deleteSummaryForMatch(id)
        matchDao.deleteMatchById(id)
    }
    suspend fun getMatchCount(): Int = matchDao.getMatchCount()
    suspend fun getAllMatchesList(): List<MatchEntity> = matchDao.getAllMatchesList()
    suspend fun insertMatches(matches: List<MatchEntity>) = matchDao.insertMatches(matches)
    suspend fun clearAllMatches() = matchDao.clearAllMatches()
    suspend fun clearAllData() = database.clearAllTables()

    // Lineups
    fun getLineupsFlow(matchId: Long): Flow<List<LineupEntity>> = lineupDao.getLineupsForMatchFlow(matchId)
    suspend fun getLineups(matchId: Long): List<LineupEntity> = lineupDao.getLineupsForMatch(matchId)
    suspend fun getAllLineups(): List<LineupEntity> = lineupDao.getAllLineups()
    fun getTeamLineupFlow(matchId: Long, team: String): Flow<List<LineupEntity>> = lineupDao.getTeamLineupFlow(matchId, team)
    suspend fun insertLineup(lineup: LineupEntity): Long = lineupDao.insertLineup(lineup)
    suspend fun insertLineups(lineups: List<LineupEntity>) = lineupDao.insertLineups(lineups)
    suspend fun updateLineup(lineup: LineupEntity) = lineupDao.updateLineup(lineup)
    suspend fun clearCaptainsForTeam(matchId: Long, team: String) = lineupDao.clearCaptainsForTeam(matchId, team)
    suspend fun deleteLineup(lineup: LineupEntity) = lineupDao.deleteLineup(lineup)
    suspend fun deleteLineupById(id: Long) = lineupDao.deleteLineupById(id)
    suspend fun populatePresetLineup(matchId: Long, teamSide: String, teamName: String) {
        val preset = RwandaFootballData.generatePresetLineup(matchId, teamSide, teamName)
        lineupDao.insertLineups(preset)
    }

    // Events
    fun getEventsFlow(matchId: Long): Flow<List<EventEntity>> = eventDao.getEventsForMatchFlow(matchId)
    fun getTotalEventCountFlow(): Flow<Int> = eventDao.getTotalEventCountFlow()
    suspend fun getTotalEventCount(): Int = eventDao.getTotalEventCount()
    suspend fun getEvents(matchId: Long): List<EventEntity> = eventDao.getEventsForMatch(matchId)
    suspend fun getAllEvents(): List<EventEntity> = eventDao.getAllEvents()
    suspend fun insertEvent(event: EventEntity): Long = eventDao.insertEvent(event)
    suspend fun insertEvents(events: List<EventEntity>) = eventDao.insertEvents(events)
    suspend fun deleteEvent(event: EventEntity) = eventDao.deleteEvent(event)
    suspend fun deleteEventById(id: Long) = eventDao.deleteEventById(id)

    // Ball Taps
    fun getBallTapsFlow(matchId: Long): Flow<List<BallTapEntity>> = ballTapDao.getBallTapsForMatchFlow(matchId)
    suspend fun getBallTaps(matchId: Long): List<BallTapEntity> = ballTapDao.getBallTapsForMatch(matchId)
    suspend fun getAllBallTaps(): List<BallTapEntity> = ballTapDao.getAllBallTaps()
    suspend fun insertBallTap(tap: BallTapEntity): Long = ballTapDao.insertBallTap(tap)
    suspend fun insertBallTaps(taps: List<BallTapEntity>) = ballTapDao.insertBallTaps(taps)
    suspend fun deleteBallTap(tap: BallTapEntity) = ballTapDao.deleteBallTap(tap)

    // Summaries
    fun getSummaryFlow(matchId: Long): Flow<SummaryEntity?> = summaryDao.getSummaryForMatchFlow(matchId)
    suspend fun getSummary(matchId: Long): SummaryEntity? = summaryDao.getSummaryForMatch(matchId)
    suspend fun getAllSummaries(): List<SummaryEntity> = summaryDao.getAllSummaries()
    suspend fun insertSummary(summary: SummaryEntity): Long = summaryDao.insertSummary(summary)
    suspend fun insertSummaries(summaries: List<SummaryEntity>) = summaryDao.insertSummaries(summaries)

    // Quick seed demo sample match
    suspend fun createSampleDerbyMatch(): Long {
        val now = System.currentTimeMillis()
        val sampleMatch = MatchEntity(
            homeTeam = "APR FC",
            awayTeam = "Rayon Sports FC",
            competition = "Rwanda Premier League",
            venue = "Amahoro National Stadium (Kigali)",
            matchDateMillis = now,
            kickoffTimeMillis = now - 3600000L,
            weather = "Sunny",
            weatherIntensity = 1,
            pitchCondition = "Good Natural Grass",
            homeColor = "Black & White",
            awayColor = "Blue & White",
            status = "LIVE",
            currentPeriod = "SECOND_HALF",
            homeScore = 2,
            awayScore = 1,
            accumulatedSeconds = 3840,
            addedTime1st = 2,
            addedTime2nd = 0,
            attendance = 42500,
            prePlayersOnPitch = true,
            prePlayersWarmingUp = true,
            preAboutToStart = true,
            kickoffTeam = "HOME",
            leftTeamSide = "HOME"
        )
        val matchId = insertMatch(sampleMatch)
        populatePresetLineup(matchId, "HOME", "APR FC")
        populatePresetLineup(matchId, "AWAY", "Rayon Sports FC")

        // Seed some sample events
        insertEvent(
            EventEntity(
                matchId = matchId,
                eventType = "GOAL",
                team = "HOME",
                playerName = "Victor Mbaoma",
                matchTimeSeconds = 1140,
                matchTimeDisplay = "19'",
                period = "FIRST_HALF",
                ballX = 0.88f,
                ballY = 0.52f
            )
        )
        insertEvent(
            EventEntity(
                matchId = matchId,
                eventType = "YELLOW_CARD",
                team = "AWAY",
                playerName = "Abdul Rwatubyaye",
                matchTimeSeconds = 1920,
                matchTimeDisplay = "32'",
                period = "FIRST_HALF"
            )
        )
        insertEvent(
            EventEntity(
                matchId = matchId,
                eventType = "GOAL",
                team = "AWAY",
                playerName = "Charles Bbaale",
                matchTimeSeconds = 2580,
                matchTimeDisplay = "43'",
                period = "FIRST_HALF",
                ballX = 0.12f,
                ballY = 0.48f
            )
        )
        insertEvent(
            EventEntity(
                matchId = matchId,
                eventType = "GOAL",
                team = "HOME",
                playerName = "Ramadhan Niyibizi",
                matchTimeSeconds = 3480,
                matchTimeDisplay = "58'",
                period = "SECOND_HALF",
                ballX = 0.85f,
                ballY = 0.38f
            )
        )

        // Seed some ball taps
        insertBallTap(BallTapEntity(matchId = matchId, x = 0.82f, y = 0.45f, side = "HOME", isDangerous = true))
        insertBallTap(BallTapEntity(matchId = matchId, x = 0.70f, y = 0.30f, side = "HOME", isDangerous = false))
        insertBallTap(BallTapEntity(matchId = matchId, x = 0.25f, y = 0.60f, side = "AWAY", isDangerous = false))
        insertBallTap(BallTapEntity(matchId = matchId, x = 0.15f, y = 0.50f, side = "AWAY", isDangerous = true))

        return matchId
    }
}
