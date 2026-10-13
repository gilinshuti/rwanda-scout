package com.example

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.LineupEntity
import com.example.data.model.MatchEntity
import com.example.data.repository.ScoutRepository
import com.example.ui.viewmodel.ScoutViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var database: AppDatabase
  private lateinit var repository: ScoutRepository

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    repository = ScoutRepository(database)
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Rwanda Football Scout", appName)
  }

  @Test
  fun `test full match lifecycle - create, lineups, pre-match, and kickoff`() = runBlocking {
    val now = System.currentTimeMillis()

    // 1. Create Match
    val match = MatchEntity(
      homeTeam = "APR FC",
      awayTeam = "Rayon Sports FC",
      competition = "Rwanda Premier League",
      venue = "Amahoro National Stadium",
      matchDateMillis = now,
      kickoffTimeMillis = now,
      status = "SCHEDULED"
    )
    val matchId = repository.insertMatch(match)
    assertTrue("Match ID must be positive", matchId > 0)

    val fetchedMatch = repository.getMatch(matchId)
    assertNotNull(fetchedMatch)
    assertEquals("APR FC", fetchedMatch?.homeTeam)
    assertEquals("Rayon Sports FC", fetchedMatch?.awayTeam)
    assertEquals("SCHEDULED", fetchedMatch?.status)

    // 2. Add Lineups
    val player1 = LineupEntity(
      matchId = matchId,
      team = "HOME",
      playerName = "Fitina Omborenga",
      jerseyNumber = 2,
      position = "DEF",
      isCaptain = true,
      isStarter = true
    )
    val player1Id = repository.insertLineup(player1)
    assertTrue(player1Id > 0)

    // Add second player as captain -> captain rule: only one captain per team
    repository.clearCaptainsForTeam(matchId, "HOME")
    val player2 = LineupEntity(
      matchId = matchId,
      team = "HOME",
      playerName = "Prince Buregeya",
      jerseyNumber = 4,
      position = "DEF",
      isCaptain = true,
      isStarter = true
    )
    repository.insertLineup(player2)

    val homePlayers = repository.getLineups(matchId)
    val captains = homePlayers.filter { it.team == "HOME" && it.isCaptain }
    assertEquals("Only one player can be captain", 1, captains.size)
    assertEquals("Prince Buregeya", captains.first().playerName)

    // 3. Pre-Match Setup (auto-save readiness and conditions)
    val updatedWithPreMatch = fetchedMatch!!.copy(
      prePlayersOnPitch = true,
      prePlayersWarmingUp = true,
      preAboutToStart = true,
      weather = "Rain",
      weatherIntensity = 75,
      pitchCondition = "Muddy",
      homeColor = "Black",
      awayColor = "Blue"
    )
    repository.updateMatch(updatedWithPreMatch)

    val preMatchFetched = repository.getMatch(matchId)
    assertNotNull(preMatchFetched)
    assertTrue(preMatchFetched!!.prePlayersOnPitch)
    assertTrue(preMatchFetched.prePlayersWarmingUp)
    assertTrue(preMatchFetched.preAboutToStart)
    assertEquals("Rain", preMatchFetched.weather)
    assertEquals(75, preMatchFetched.weatherIntensity)
    assertEquals("Muddy", preMatchFetched.pitchCondition)

    // 4. Kickoff & Confirm
    val liveMatch = preMatchFetched.copy(
      status = "LIVE",
      currentPeriod = "FIRST_HALF",
      startedAtMillis = now,
      periodStartedAtMillis = now,
      accumulatedSeconds = 0,
      kickoffTeam = "HOME",
      leftTeamSide = "HOME"
    )
    repository.updateMatch(liveMatch)

    // Insert KICKOFF Event
    val kickoffEvent = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "KICKOFF",
      team = "HOME",
      playerName = "APR FC Kickoff",
      matchTimeSeconds = 0,
      matchTimeDisplay = "00:00",
      period = "FIRST_HALF"
    )
    val eventId = repository.insertEvent(kickoffEvent)
    assertTrue(eventId > 0)

    // Verify Room Rows Updated and Event Stored
    val verifiedMatch = repository.getMatch(matchId)
    assertNotNull(verifiedMatch)
    assertEquals("LIVE", verifiedMatch?.status)
    assertEquals("FIRST_HALF", verifiedMatch?.currentPeriod)
    assertEquals("HOME", verifiedMatch?.kickoffTeam)

    val events = repository.getEvents(matchId)
    assertEquals(1, events.size)
    val firstEvent = events.first()
    assertEquals("KICKOFF", firstEvent.eventType)
    assertEquals("00:00", firstEvent.matchTimeDisplay)
    assertEquals(0, firstEvent.matchTimeSeconds)
    assertEquals("FIRST_HALF", firstEvent.period)
    assertEquals("APR FC Kickoff", firstEvent.playerName)
  }

  @Test
  fun `test live match ball taps, dangerous attack detection, and event logging`() = runBlocking {
    val now = System.currentTimeMillis()
    val match = MatchEntity(
      homeTeam = "Rayon Sports FC",
      awayTeam = "Police FC",
      competition = "Rwanda Cup",
      venue = "Kigali Pele Stadium",
      matchDateMillis = now,
      kickoffTimeMillis = now,
      status = "LIVE",
      currentPeriod = "FIRST_HALF"
    )
    val matchId = repository.insertMatch(match)

    // 1. Log Ball Taps: normal tap vs dangerous zone tap
    // Normal tap in middle: x = 0.5f, y = 0.5f
    val normalTap = com.example.data.model.BallTapEntity(
      matchId = matchId,
      x = 0.5f,
      y = 0.5f,
      side = "HOME",
      isDangerous = false
    )
    val tap1Id = repository.insertBallTap(normalTap)
    assertTrue(tap1Id > 0)

    // Dangerous tap inside attacking third (< 0.25f or > 0.75f)
    val dangerousTap = com.example.data.model.BallTapEntity(
      matchId = matchId,
      x = 0.85f,
      y = 0.45f,
      side = "HOME",
      isDangerous = true
    )
    val tap2Id = repository.insertBallTap(dangerousTap)
    assertTrue(tap2Id > 0)

    val taps = repository.getBallTaps(matchId)
    assertEquals(2, taps.size)
    assertFalse(taps[0].isDangerous)
    assertTrue(taps[1].isDangerous)

    // 2. Log Goal Event & update score
    val goalEvent = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "GOAL",
      team = "HOME",
      playerName = "Muhire Kevin",
      matchTimeSeconds = 1420,
      matchTimeDisplay = "24'",
      period = "FIRST_HALF"
    )
    val goalId = repository.insertEvent(goalEvent)
    assertTrue(goalId > 0)

    // Update match score to 1-0
    repository.updateMatch(match.copy(id = matchId, homeScore = 1, accumulatedSeconds = 1420))
    val updatedMatch = repository.getMatch(matchId)
    assertNotNull(updatedMatch)
    assertEquals(1, updatedMatch?.homeScore)
    assertEquals(0, updatedMatch?.awayScore)

    // 3. Log Yellow and Red Cards
    val yellowCard = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "YELLOW_CARD",
      team = "AWAY",
      playerName = "Police Defender",
      matchTimeSeconds = 1900,
      matchTimeDisplay = "31'",
      period = "FIRST_HALF"
    )
    repository.insertEvent(yellowCard)

    val events = repository.getEvents(matchId)
    assertEquals(2, events.size)
    assertEquals("GOAL", events[0].eventType)
    assertEquals("YELLOW_CARD", events[1].eventType)
    assertEquals("AWAY", events[1].team)
  }

  @Test
  fun `test goal workflow - team selection, score update, cancel, and after-goal kickoff`() = runBlocking {
    val now = System.currentTimeMillis()
    val match = MatchEntity(
      homeTeam = "APR FC",
      awayTeam = "Rayon Sports FC",
      competition = "Rwanda Premier League",
      venue = "Amahoro National Stadium",
      matchDateMillis = now,
      kickoffTimeMillis = now,
      status = "LIVE",
      currentPeriod = "FIRST_HALF",
      homeScore = 0,
      awayScore = 0,
      accumulatedSeconds = 1200
    )
    val matchId = repository.insertMatch(match)

    // 1. Goal Workflow - HOME team scores
    val homeScoredMatch = match.copy(id = matchId, homeScore = 1, accumulatedSeconds = 1427)
    repository.updateMatch(homeScoredMatch)

    val goalEvent = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "GOAL",
      team = "HOME",
      playerName = "Fitina Omborenga",
      matchTimeSeconds = 1427,
      matchTimeDisplay = "23:47",
      period = "FIRST_HALF",
      extraDataJson = "{\"howScored\":\"Shot on goal\",\"insideBox\":true,\"doubtfulGoal\":false,\"scorer\":\"Fitina Omborenga\"}",
      ballX = 0.90f,
      ballY = 0.50f,
      isDoubtful = false,
      isDoubtfulScorer = false
    )
    val goalEventId = repository.insertEvent(goalEvent)
    assertTrue("Goal event should be saved", goalEventId > 0)

    // Verify Match Score Updated
    val updatedMatch = repository.getMatch(matchId)
    assertNotNull(updatedMatch)
    assertEquals(1, updatedMatch?.homeScore)
    assertEquals(0, updatedMatch?.awayScore)

    // 2. After-Goal Kickoff
    val afterGoalKickoffEvent = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "AFTER_GOAL_KICKOFF",
      team = "AWAY",
      playerName = "Play Restarted After Goal",
      matchTimeSeconds = 1480,
      matchTimeDisplay = "24:40",
      period = "FIRST_HALF"
    )
    val afterGoalKoId = repository.insertEvent(afterGoalKickoffEvent)
    assertTrue("After-goal kickoff event should be saved", afterGoalKoId > 0)

    // 3. Goal Cancelled Event (Offside test)
    val goalCancelledEvent = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "GOAL_CANCELLED",
      team = "HOME",
      playerName = "Goal Cancelled: Offside",
      matchTimeSeconds = 1520,
      matchTimeDisplay = "25:20",
      period = "FIRST_HALF",
      extraDataJson = "{\"reason\":\"Offside\"}"
    )
    val cancelId = repository.insertEvent(goalCancelledEvent)
    assertTrue("Goal cancelled event should be saved", cancelId > 0)

    val allEvents = repository.getEvents(matchId)
    assertEquals(3, allEvents.size)
    assertEquals("GOAL", allEvents[0].eventType)
    assertEquals("23:47", allEvents[0].matchTimeDisplay)
    assertEquals("AFTER_GOAL_KICKOFF", allEvents[1].eventType)
    assertEquals("GOAL_CANCELLED", allEvents[2].eventType)
  }

  @Test
  fun `test cards, corners, penalty, and dfk workflows`() = runBlocking {
    val now = System.currentTimeMillis()
    val match = MatchEntity(
      homeTeam = "Police FC",
      awayTeam = "AS Kigali",
      competition = "Rwanda Premier League",
      venue = "Kigali Pele Stadium",
      matchDateMillis = now,
      kickoffTimeMillis = now,
      status = "LIVE",
      currentPeriod = "FIRST_HALF"
    )
    val matchId = repository.insertMatch(match)

    // 1. CARDS: First Yellow, then Second Yellow (Yellow-Red) + award free kick
    val cardEvent1 = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "CARD",
      team = "HOME",
      playerName = "Hakizimana Muhadjiri",
      matchTimeSeconds = 600,
      matchTimeDisplay = "10:00",
      period = "FIRST_HALF",
      extraDataJson = "{\"cardType\":\"Yellow\"}"
    )
    repository.insertEvent(cardEvent1)

    // Also award opposing free kick
    val fkOpposing = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "FREE_KICK",
      team = "AWAY",
      playerName = "AWAY Free Kick",
      matchTimeSeconds = 600,
      matchTimeDisplay = "10:00",
      period = "FIRST_HALF"
    )
    repository.insertEvent(fkOpposing)

    val cardEvent2 = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "CARD",
      team = "HOME",
      playerName = "Hakizimana Muhadjiri",
      matchTimeSeconds = 1800,
      matchTimeDisplay = "30:00",
      period = "FIRST_HALF",
      extraDataJson = "{\"cardType\":\"Yellow-Red\"}"
    )
    repository.insertEvent(cardEvent2)

    // 2. CORNER: Corner awarded with corner taker
    val cornerEvent = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "CORNER",
      team = "AWAY",
      playerName = "Kigali Winger",
      matchTimeSeconds = 2100,
      matchTimeDisplay = "35:00",
      period = "FIRST_HALF"
    )
    repository.insertEvent(cornerEvent)

    // 3. PENALTY: Penalty caused by Foul with Yellow card booking + Resolution (GK Save)
    val penaltyEvent = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "PENALTY",
      team = "AWAY",
      playerName = "AWAY Penalty (Foul)",
      matchTimeSeconds = 2400,
      matchTimeDisplay = "40:00",
      period = "FIRST_HALF",
      extraDataJson = "{\"cause\":\"Foul\",\"booking\":\"Yellow\"}"
    )
    repository.insertEvent(penaltyEvent)

    val penaltyResEvent = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "PENALTY_RESOLUTION",
      team = "AWAY",
      playerName = "Penalty: GK Save",
      matchTimeSeconds = 2460,
      matchTimeDisplay = "41:00",
      period = "FIRST_HALF",
      extraDataJson = "{\"outcome\":\"GK Save\"}"
    )
    repository.insertEvent(penaltyResEvent)

    // 4. DANGEROUS FREE KICK: Inside dangerous zone + Resolution
    val dfkEvent = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "DANGEROUS_FREE_KICK",
      team = "HOME",
      playerName = "HOME Dangerous Free Kick (Handball)",
      matchTimeSeconds = 2600,
      matchTimeDisplay = "43:20",
      period = "FIRST_HALF",
      extraDataJson = "{\"cause\":\"Handball\",\"isDangerous\":true}"
    )
    repository.insertEvent(dfkEvent)

    val dfkResEvent = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "DFK_RESOLUTION",
      team = "HOME",
      playerName = "DFK Resolution: Corner",
      matchTimeSeconds = 2650,
      matchTimeDisplay = "44:10",
      period = "FIRST_HALF",
      extraDataJson = "{\"outcome\":\"Corner\",\"gkSave\":true}"
    )
    repository.insertEvent(dfkResEvent)

    val events = repository.getEvents(matchId)
    assertEquals(8, events.size)

    val cards = events.filter { it.eventType == "CARD" }
    assertEquals(2, cards.size)
    assertTrue(cards[0].extraDataJson!!.contains("Yellow"))
    assertTrue(cards[1].extraDataJson!!.contains("Yellow-Red"))

    val corners = events.filter { it.eventType == "CORNER" }
    assertEquals(1, corners.size)

    val penalties = events.filter { it.eventType == "PENALTY" }
    assertEquals(1, penalties.size)

    val penaltyRes = events.filter { it.eventType == "PENALTY_RESOLUTION" }
    assertEquals(1, penaltyRes.size)
    assertTrue(penaltyRes[0].extraDataJson!!.contains("GK Save"))

    val dfks = events.filter { it.eventType == "DANGEROUS_FREE_KICK" }
    assertEquals(1, dfks.size)

    val dfkRes = events.filter { it.eventType == "DFK_RESOLUTION" }
    assertEquals(1, dfkRes.size)
    assertTrue(dfkRes[0].extraDataJson!!.contains("gkSave\":true"))
  }

  @Test
  fun `test VAR review workflow overturns goal and penalties`() = runBlocking {
    val now = System.currentTimeMillis()
    val match = com.example.data.model.MatchEntity(
      homeTeam = "Rayon Sports",
      awayTeam = "APR FC",
      competition = "Rwanda Premier League",
      venue = "Amahoro Stadium",
      matchDateMillis = now,
      kickoffTimeMillis = now,
      status = "LIVE",
      homeScore = 1,
      awayScore = 0,
      currentPeriod = "FIRST_HALF"
    )
    val matchId = repository.insertMatch(match)

    // 1. Initial Goal logged
    val goalEvent = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "GOAL",
      team = "HOME",
      playerName = "Muhire Kevin",
      matchTimeSeconds = 1200,
      matchTimeDisplay = "20:00",
      period = "FIRST_HALF"
    )
    val goalId = repository.insertEvent(goalEvent)
    assertTrue(goalId > 0)

    // 2. VAR Review: "Goal" -> "No Goal"
    val varReviewGoal = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "VAR_REVIEW",
      team = "HOME",
      playerName = "VAR Review: Goal -> No Goal",
      matchTimeSeconds = 1260,
      matchTimeDisplay = "21:00",
      period = "FIRST_HALF",
      extraDataJson = "{\"reason\":\"Goal\",\"outcome\":\"No Goal\",\"team\":\"HOME\"}"
    )
    repository.insertEvent(varReviewGoal)

    // Apply "No Goal" outcome: delete goal and decrement score
    val eventsBefore = repository.getEvents(matchId)
    val goalToDelete = eventsBefore.findLast { it.eventType == "GOAL" && it.team == "HOME" }
    assertNotNull(goalToDelete)
    repository.deleteEvent(goalToDelete!!)

    repository.updateMatch(match.copy(id = matchId, homeScore = 0))
    val matchAfterVar = repository.getMatch(matchId)
    assertEquals(0, matchAfterVar?.homeScore)

    // 3. VAR Review: "Penalty kick" -> "No Penalty"
    val varReviewPenalty = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "VAR_REVIEW",
      team = "AWAY",
      playerName = "VAR Review: Penalty kick -> No Penalty",
      matchTimeSeconds = 1500,
      matchTimeDisplay = "25:00",
      period = "FIRST_HALF",
      extraDataJson = "{\"reason\":\"Penalty kick\",\"outcome\":\"No Penalty\",\"team\":\"AWAY\"}"
    )
    repository.insertEvent(varReviewPenalty)

    val penaltyCancel = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "PENALTY_CANCELLED",
      team = "AWAY",
      playerName = "VAR: Penalty Overturned",
      matchTimeSeconds = 1500,
      matchTimeDisplay = "25:00",
      period = "FIRST_HALF"
    )
    repository.insertEvent(penaltyCancel)

    // 4. VAR Review: "Red card" -> "No Red card"
    val redCardCancel = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "CARD_CANCELLED",
      team = "HOME",
      playerName = "VAR: Red Card Rescinded",
      matchTimeSeconds = 1800,
      matchTimeDisplay = "30:00",
      period = "FIRST_HALF"
    )
    repository.insertEvent(redCardCancel)

    // 5. VAR Review: "Corner kick" -> "No Corner" (overturned to Goal Kick)
    val overturnedGk = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "GOAL_KICK",
      team = "AWAY",
      playerName = "Goal Kick (Overturned Corner)",
      matchTimeSeconds = 2100,
      matchTimeDisplay = "35:00",
      period = "FIRST_HALF"
    )
    repository.insertEvent(overturnedGk)

    val allEvents = repository.getEvents(matchId)
    val varReviews = allEvents.filter { it.eventType == "VAR_REVIEW" }
    assertEquals(2, varReviews.size)

    val goals = allEvents.filter { it.eventType == "GOAL" }
    assertEquals(0, goals.size) // Overturned goal successfully removed

    val cancelledPens = allEvents.filter { it.eventType == "PENALTY_CANCELLED" }
    assertEquals(1, cancelledPens.size)

    val cancelledCards = allEvents.filter { it.eventType == "CARD_CANCELLED" }
    assertEquals(1, cancelledCards.size)

    val goalKicks = allEvents.filter { it.eventType == "GOAL_KICK" }
    assertEquals(1, goalKicks.size)
  }

  @Test
  fun `test In-Play actions log throw-in, free kick, shot, goal kick, offside`() = runBlocking {
    val now = System.currentTimeMillis()
    val match = com.example.data.model.MatchEntity(
      homeTeam = "Rayon Sports",
      awayTeam = "Kiyovu Sports",
      competition = "Rwanda Cup",
      venue = "Kigali Pele Stadium",
      matchDateMillis = now,
      kickoffTimeMillis = now,
      status = "LIVE",
      homeScore = 0,
      awayScore = 0,
      currentPeriod = "FIRST_HALF"
    )
    val matchId = repository.insertMatch(match)

    // 1. Throw-in
    val throwIn = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "THROW_IN",
      team = "HOME",
      playerName = "HOME Throw-in",
      matchTimeSeconds = 120,
      matchTimeDisplay = "02:00",
      period = "FIRST_HALF"
    )
    repository.insertEvent(throwIn)

    // 2. Free kick with cause
    val freeKick = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "FREE_KICK",
      team = "AWAY",
      playerName = "AWAY Free Kick (Foul)",
      matchTimeSeconds = 340,
      matchTimeDisplay = "05:40",
      period = "FIRST_HALF",
      extraDataJson = "{\"cause\":\"Foul\"}"
    )
    repository.insertEvent(freeKick)

    // 3. Shot: on goal + resolved as Goal
    val shot = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "SHOT",
      team = "HOME",
      playerName = "HOME Shot: Shot on goal (Goal)",
      matchTimeSeconds = 600,
      matchTimeDisplay = "10:00",
      period = "FIRST_HALF",
      extraDataJson = "{\"shotCategory\":\"Shot on goal\",\"isHeader\":false,\"isInsideBox\":true,\"outcome\":\"Goal\"}"
    )
    repository.insertEvent(shot)

    val goalFromShot = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "GOAL",
      team = "HOME",
      playerName = "HOME Goal (Shot)",
      matchTimeSeconds = 600,
      matchTimeDisplay = "10:00",
      period = "FIRST_HALF"
    )
    repository.insertEvent(goalFromShot)
    repository.updateMatch(match.copy(id = matchId, homeScore = 1))

    // 4. Goal Kick
    val goalKick = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "GOAL_KICK",
      team = "AWAY",
      playerName = "AWAY Goal Kick",
      matchTimeSeconds = 900,
      matchTimeDisplay = "15:00",
      period = "FIRST_HALF"
    )
    repository.insertEvent(goalKick)

    // 5. Offside
    val offside = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "OFFSIDE",
      team = "HOME",
      playerName = "HOME Offside",
      matchTimeSeconds = 1100,
      matchTimeDisplay = "18:20",
      period = "FIRST_HALF"
    )
    repository.insertEvent(offside)

    val events = repository.getEvents(matchId)
    assertEquals(6, events.size)

    assertEquals(1, events.count { it.eventType == "THROW_IN" })
    assertEquals(1, events.count { it.eventType == "FREE_KICK" })
    assertEquals(1, events.count { it.eventType == "SHOT" })
    assertEquals(1, events.count { it.eventType == "GOAL" })
    assertEquals(1, events.count { it.eventType == "GOAL_KICK" })
    assertEquals(1, events.count { it.eventType == "OFFSIDE" })

    val updatedMatch = repository.getMatch(matchId)
    assertEquals(1, updatedMatch?.homeScore)
    assertEquals(0, updatedMatch?.awayScore)
  }

  @Test
  fun `test General Events menu items - all 12 actions`() = runBlocking {
    val now = System.currentTimeMillis()
    val match = MatchEntity(
      homeTeam = "Rayon Sports FC",
      awayTeam = "APR FC",
      competition = "Rwanda Cup",
      venue = "Huye Stadium",
      matchDateMillis = now,
      kickoffTimeMillis = now,
      status = "LIVE",
      currentPeriod = "FIRST_HALF",
      homeScore = 0,
      awayScore = 0,
      leftTeamSide = "HOME"
    )
    val matchId = repository.insertMatch(match)

    // 1. MATCH STATUS transitions & STATUS_CHANGE event
    val halftimeEvent = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "STATUS_CHANGE",
      team = "NEUTRAL",
      playerName = "Status: Halftime",
      matchTimeSeconds = 2700,
      matchTimeDisplay = "45:00",
      period = "HALF_TIME",
      extraDataJson = "{\"status\":\"Halftime\",\"period\":\"HALF_TIME\"}"
    )
    repository.insertEvent(halftimeEvent)
    repository.updateMatch(match.copy(id = matchId, currentPeriod = "HALF_TIME", accumulatedSeconds = 2700))

    var currentMatch = repository.getMatch(matchId)
    assertEquals("HALF_TIME", currentMatch?.currentPeriod)

    // 2. SUBSTITUTION (Home team, 2 players, Delayed)
    val subEvent = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "SUBSTITUTION",
      team = "HOME",
      playerName = "HOME Sub: 2 players [DELAYED]",
      matchTimeSeconds = 2700,
      matchTimeDisplay = "45:00",
      period = "HALF_TIME",
      extraDataJson = "{\"playerCount\":2,\"isDelayed\":true}"
    )
    repository.insertEvent(subEvent)

    // 3. INJURY & RETURN
    val injuryEvent = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "INJURY",
      team = "AWAY",
      playerName = "AWAY Player Injury",
      matchTimeSeconds = 3000,
      matchTimeDisplay = "50:00",
      period = "SECOND_HALF"
    )
    repository.insertEvent(injuryEvent)

    val injuryReturnEvent = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "INJURY_RETURN",
      team = "AWAY",
      playerName = "AWAY Player Returned from Injury",
      matchTimeSeconds = 3120,
      matchTimeDisplay = "52:00",
      period = "SECOND_HALF"
    )
    repository.insertEvent(injuryReturnEvent)

    // 4. ADDED TIME
    val addedTimeEvent = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "ADDED_TIME",
      team = "NEUTRAL",
      playerName = "+4 min Added Time (2nd Half)",
      matchTimeSeconds = 5400,
      matchTimeDisplay = "90:00",
      period = "SECOND_HALF"
    )
    repository.insertEvent(addedTimeEvent)
    repository.updateMatch(currentMatch!!.copy(addedTime1st = 2, addedTime2nd = 4))

    currentMatch = repository.getMatch(matchId)
    assertEquals(2, currentMatch?.addedTime1st)
    assertEquals(4, currentMatch?.addedTime2nd)

    // 5. SNAPSHOT
    val snapshotEvent = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "SNAPSHOT",
      team = "NEUTRAL",
      playerName = "Pitch Snapshot",
      matchTimeSeconds = 3200,
      matchTimeDisplay = "53:20",
      period = "SECOND_HALF",
      extraDataJson = "{\"filePath\":\"/app/snapshots/pitch_snapshot_1.jpg\"}"
    )
    repository.insertEvent(snapshotEvent)

    // 6. CONDITIONS
    repository.updateMatch(
      currentMatch!!.copy(
        weather = "Rain",
        weatherIntensity = 80,
        fog = true,
        wind = "Strong",
        pitchCondition = "Wet",
        homeColor = "Blue",
        awayColor = "White"
      )
    )
    currentMatch = repository.getMatch(matchId)
    assertEquals("Rain", currentMatch?.weather)
    assertEquals(80, currentMatch?.weatherIntensity)
    assertTrue(currentMatch?.fog == true)
    assertEquals("Strong", currentMatch?.wind)
    assertEquals("Wet", currentMatch?.pitchCondition)

    // 7. ATTENDANCE (Save and lock)
    repository.updateMatch(currentMatch!!.copy(attendance = 28500))
    val attendanceEvent = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "ATTENDANCE",
      team = "NEUTRAL",
      playerName = "Attendance: 28500",
      matchTimeSeconds = 3300,
      matchTimeDisplay = "55:00",
      period = "SECOND_HALF",
      extraDataJson = "{\"attendance\":28500}"
    )
    repository.insertEvent(attendanceEvent)

    currentMatch = repository.getMatch(matchId)
    assertEquals(28500, currentMatch?.attendance)

    // 8. TEMPORARY INTERRUPTION & RESUME
    val interruptionEvent = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "INTERRUPTION",
      team = "NEUTRAL",
      playerName = "Match Interrupted: Weather / Severe Storm",
      matchTimeSeconds = 3600,
      matchTimeDisplay = "60:00",
      period = "SECOND_HALF",
      extraDataJson = "{\"reason\":\"Weather / Severe Storm\"}"
    )
    repository.insertEvent(interruptionEvent)

    val resumeEvent = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "INTERRUPTION_END",
      team = "NEUTRAL",
      playerName = "Match Resumed",
      matchTimeSeconds = 3600,
      matchTimeDisplay = "60:00",
      period = "SECOND_HALF"
    )
    repository.insertEvent(resumeEvent)

    // 9. UNDO EVENT: insert a reversible event and delete it
    val foulEvent = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "FOUL",
      team = "HOME",
      playerName = "Accidental Tap",
      matchTimeSeconds = 3700,
      matchTimeDisplay = "61:40",
      period = "SECOND_HALF"
    )
    val foulId = repository.insertEvent(foulEvent)
    var eventsList = repository.getEvents(matchId)
    assertTrue(eventsList.any { it.id == foulId })

    repository.deleteEventById(foulId)
    eventsList = repository.getEvents(matchId)
    assertFalse("Deleted foul event must not be present in database", eventsList.any { it.id == foulId })

    // 10. TEAM COLORS
    repository.updateMatch(currentMatch!!.copy(homeColor = "Navy", awayColor = "Emerald"))
    currentMatch = repository.getMatch(matchId)
    assertEquals("Navy", currentMatch?.homeColor)
    assertEquals("Emerald", currentMatch?.awayColor)

    // 11. CHANGE SIDES (Toggle leftTeamSide)
    val currentLeft = currentMatch?.leftTeamSide ?: "HOME"
    val toggledSide = if (currentLeft == "HOME") "AWAY" else "HOME"
    repository.updateMatch(currentMatch!!.copy(leftTeamSide = toggledSide))

    currentMatch = repository.getMatch(matchId)
    assertEquals("AWAY", currentMatch?.leftTeamSide)

    // 12. REQUEST ASSISTANCE
    val assistanceEvent = com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "REQUEST_ASSISTANCE",
      team = "NEUTRAL",
      playerName = "Request Assistance",
      matchTimeSeconds = 4000,
      matchTimeDisplay = "66:40",
      period = "SECOND_HALF",
      extraDataJson = "{\"status\":\"SAVED_LOCALLY\"}"
    )
    repository.insertEvent(assistanceEvent)

    val finalEvents = repository.getEvents(matchId)
    assertTrue(finalEvents.any { it.eventType == "STATUS_CHANGE" })
    assertTrue(finalEvents.any { it.eventType == "SUBSTITUTION" })
    assertTrue(finalEvents.any { it.eventType == "INJURY" })
    assertTrue(finalEvents.any { it.eventType == "INJURY_RETURN" })
    assertTrue(finalEvents.any { it.eventType == "ADDED_TIME" })
    assertTrue(finalEvents.any { it.eventType == "SNAPSHOT" })
    assertTrue(finalEvents.any { it.eventType == "ATTENDANCE" })
    assertTrue(finalEvents.any { it.eventType == "INTERRUPTION" })
    assertTrue(finalEvents.any { it.eventType == "INTERRUPTION_END" })
    assertTrue(finalEvents.any { it.eventType == "REQUEST_ASSISTANCE" })
  }

  @Test
  fun `test match report room calculations and stat derivations`() = runBlocking {
    val now = System.currentTimeMillis()
    val match = MatchEntity(
      homeTeam = "Rayon Sports",
      awayTeam = "Kiyovu Sports",
      competition = "Rwanda Premier League",
      venue = "Kigali Pelé Stadium",
      matchDateMillis = now,
      kickoffTimeMillis = now,
      status = "FINISHED",
      homeScore = 2,
      awayScore = 1,
      homePenScore = 4,
      awayPenScore = 3
    )
    val matchId = repository.insertMatch(match)

    // 1. Ball Taps: 6 HOME, 4 AWAY -> 60% and 40% possession
    repeat(6) { i ->
      repository.insertBallTap(
        com.example.data.model.BallTapEntity(
          matchId = matchId,
          x = 0.3f,
          y = 0.4f,
          side = "HOME"
        )
      )
    }
    repeat(4) { i ->
      repository.insertBallTap(
        com.example.data.model.BallTapEntity(
          matchId = matchId,
          x = 0.7f,
          y = 0.4f,
          side = "AWAY"
        )
      )
    }

    // 2. SHOTS:
    // HOME: 3 shots (1 On Target, 1 Goal, 1 Wide [off target])
    repository.insertEvent(com.example.data.model.EventEntity(
      matchId = matchId, eventType = "SHOT", team = "HOME", playerName = "Striker A",
      matchTimeSeconds = 300, matchTimeDisplay = "05:00", period = "FIRST_HALF",
      extraDataJson = "{\"outcome\":\"On Target\"}"
    ))
    repository.insertEvent(com.example.data.model.EventEntity(
      matchId = matchId, eventType = "SHOT", team = "HOME", playerName = "Striker B",
      matchTimeSeconds = 600, matchTimeDisplay = "10:00", period = "FIRST_HALF",
      extraDataJson = "{\"outcome\":\"Goal\"}"
    ))
    repository.insertEvent(com.example.data.model.EventEntity(
      matchId = matchId, eventType = "SHOT", team = "HOME", playerName = "Winger A",
      matchTimeSeconds = 900, matchTimeDisplay = "15:00", period = "FIRST_HALF",
      extraDataJson = "{\"outcome\":\"Wide\"}"
    ))

    // AWAY: 2 shots (1 Saved [on target], 1 Bar [off target])
    repository.insertEvent(com.example.data.model.EventEntity(
      matchId = matchId, eventType = "SHOT", team = "AWAY", playerName = "Away Forward",
      matchTimeSeconds = 1200, matchTimeDisplay = "20:00", period = "FIRST_HALF",
      extraDataJson = "{\"outcome\":\"Saved\"}"
    ))
    repository.insertEvent(com.example.data.model.EventEntity(
      matchId = matchId, eventType = "SHOT", team = "AWAY", playerName = "Away Midfielder",
      matchTimeSeconds = 1500, matchTimeDisplay = "25:00", period = "FIRST_HALF",
      extraDataJson = "{\"outcome\":\"Bar\"}"
    ))

    // 3. CORNERS: 4 HOME, 2 AWAY
    repeat(4) { i ->
      repository.insertEvent(com.example.data.model.EventEntity(
        matchId = matchId, eventType = "CORNER", team = "HOME", playerName = "Corner Taker",
        matchTimeSeconds = 1800 + i * 100, matchTimeDisplay = "30:00", period = "FIRST_HALF"
      ))
    }
    repeat(2) { i ->
      repository.insertEvent(com.example.data.model.EventEntity(
        matchId = matchId, eventType = "CORNER", team = "AWAY", playerName = "Away Corner",
        matchTimeSeconds = 2200 + i * 100, matchTimeDisplay = "36:00", period = "FIRST_HALF"
      ))
    }

    // 4. OFFSIDES: 2 HOME, 1 AWAY
    repeat(2) { i ->
      repository.insertEvent(com.example.data.model.EventEntity(
        matchId = matchId, eventType = "OFFSIDE", team = "HOME", playerName = "Offside Player",
        matchTimeSeconds = 2500 + i * 100, matchTimeDisplay = "41:00", period = "FIRST_HALF"
      ))
    }
    repository.insertEvent(com.example.data.model.EventEntity(
      matchId = matchId, eventType = "OFFSIDE", team = "AWAY", playerName = "Away Offside",
      matchTimeSeconds = 2700, matchTimeDisplay = "45:00", period = "FIRST_HALF"
    ))

    // 5. FOULS: eventType in FREE_KICK, DANGEROUS_FREE_KICK
    // HOME: 2 FREE_KICK, 1 DANGEROUS_FREE_KICK = 3
    repository.insertEvent(com.example.data.model.EventEntity(
      matchId = matchId, eventType = "FREE_KICK", team = "HOME", playerName = "Home FK 1",
      matchTimeSeconds = 2800, matchTimeDisplay = "46:00", period = "SECOND_HALF"
    ))
    repository.insertEvent(com.example.data.model.EventEntity(
      matchId = matchId, eventType = "FREE_KICK", team = "HOME", playerName = "Home FK 2",
      matchTimeSeconds = 2900, matchTimeDisplay = "48:00", period = "SECOND_HALF"
    ))
    repository.insertEvent(com.example.data.model.EventEntity(
      matchId = matchId, eventType = "DANGEROUS_FREE_KICK", team = "HOME", playerName = "Home DFK",
      matchTimeSeconds = 3000, matchTimeDisplay = "50:00", period = "SECOND_HALF"
    ))
    // AWAY: 2 FREE_KICK = 2
    repository.insertEvent(com.example.data.model.EventEntity(
      matchId = matchId, eventType = "FREE_KICK", team = "AWAY", playerName = "Away FK 1",
      matchTimeSeconds = 3100, matchTimeDisplay = "51:00", period = "SECOND_HALF"
    ))
    repository.insertEvent(com.example.data.model.EventEntity(
      matchId = matchId, eventType = "FREE_KICK", team = "AWAY", playerName = "Away FK 2",
      matchTimeSeconds = 3200, matchTimeDisplay = "53:00", period = "SECOND_HALF"
    ))

    // 6. CARDS:
    // HOME: 2 Yellow
    repository.insertEvent(com.example.data.model.EventEntity(
      matchId = matchId, eventType = "CARD", team = "HOME", playerName = "Home Defender 1",
      matchTimeSeconds = 3300, matchTimeDisplay = "55:00", period = "SECOND_HALF",
      extraDataJson = "{\"cardType\":\"Yellow\"}"
    ))
    repository.insertEvent(com.example.data.model.EventEntity(
      matchId = matchId, eventType = "CARD", team = "HOME", playerName = "Home Midfielder",
      matchTimeSeconds = 3400, matchTimeDisplay = "56:00", period = "SECOND_HALF",
      extraDataJson = "{\"cardType\":\"Yellow\"}"
    ))

    // AWAY: 1 Yellow, 1 Red
    repository.insertEvent(com.example.data.model.EventEntity(
      matchId = matchId, eventType = "CARD", team = "AWAY", playerName = "Away Defender",
      matchTimeSeconds = 3500, matchTimeDisplay = "58:00", period = "SECOND_HALF",
      extraDataJson = "{\"cardType\":\"Yellow\"}"
    ))
    repository.insertEvent(com.example.data.model.EventEntity(
      matchId = matchId, eventType = "CARD", team = "AWAY", playerName = "Away Aggressor",
      matchTimeSeconds = 3600, matchTimeDisplay = "60:00", period = "SECOND_HALF",
      extraDataJson = "{\"cardType\":\"Red\"}"
    ))

    val allEvents = repository.getEvents(matchId)
    val allTaps = repository.getBallTaps(matchId)

    // Verification of calculations according to Room rules:
    // 1. Possession:
    val homeTapsCount = allTaps.count { it.side.equals("HOME", true) }
    val awayTapsCount = allTaps.count { it.side.equals("AWAY", true) }
    val homePossession = Math.round((homeTapsCount.toDouble() / (homeTapsCount + awayTapsCount)) * 100).toInt()
    val awayPossession = 100 - homePossession
    assertEquals(60, homePossession)
    assertEquals(40, awayPossession)

    // 2. Total shots:
    val homeTotalShots = allEvents.count { it.eventType == "SHOT" && it.team.equals("HOME", true) }
    val awayTotalShots = allEvents.count { it.eventType == "SHOT" && it.team.equals("AWAY", true) }
    assertEquals(3, homeTotalShots)
    assertEquals(2, awayTotalShots)

    // 3. Shots on target: SHOT + outcome IN ('On Target','Goal','Saved')
    val homeShotsOnTarget = allEvents.count { ev ->
      ev.eventType == "SHOT" && ev.team.equals("HOME", true) &&
        (ev.extraDataJson?.contains("\"On Target\"") == true || ev.extraDataJson?.contains("\"Goal\"") == true || ev.extraDataJson?.contains("\"Saved\"") == true)
    }
    val awayShotsOnTarget = allEvents.count { ev ->
      ev.eventType == "SHOT" && ev.team.equals("AWAY", true) &&
        (ev.extraDataJson?.contains("\"On Target\"") == true || ev.extraDataJson?.contains("\"Goal\"") == true || ev.extraDataJson?.contains("\"Saved\"") == true)
    }
    assertEquals(2, homeShotsOnTarget)
    assertEquals(1, awayShotsOnTarget)

    // 4. Shots off target: SHOT + outcome IN ('Off Target','Wide','Bar','Post')
    val homeShotsOffTarget = allEvents.count { ev ->
      ev.eventType == "SHOT" && ev.team.equals("HOME", true) &&
        (ev.extraDataJson?.contains("\"Off Target\"") == true || ev.extraDataJson?.contains("\"Wide\"") == true || ev.extraDataJson?.contains("\"Bar\"") == true || ev.extraDataJson?.contains("\"Post\"") == true)
    }
    val awayShotsOffTarget = allEvents.count { ev ->
      ev.eventType == "SHOT" && ev.team.equals("AWAY", true) &&
        (ev.extraDataJson?.contains("\"Off Target\"") == true || ev.extraDataJson?.contains("\"Wide\"") == true || ev.extraDataJson?.contains("\"Bar\"") == true || ev.extraDataJson?.contains("\"Post\"") == true)
    }
    assertEquals(1, homeShotsOffTarget)
    assertEquals(1, awayShotsOffTarget)

    // 5. Corners:
    val homeCorners = allEvents.count { it.eventType == "CORNER" && it.team.equals("HOME", true) }
    val awayCorners = allEvents.count { it.eventType == "CORNER" && it.team.equals("AWAY", true) }
    assertEquals(4, homeCorners)
    assertEquals(2, awayCorners)

    // 6. Offsides:
    val homeOffsides = allEvents.count { it.eventType == "OFFSIDE" && it.team.equals("HOME", true) }
    val awayOffsides = allEvents.count { it.eventType == "OFFSIDE" && it.team.equals("AWAY", true) }
    assertEquals(2, homeOffsides)
    assertEquals(1, awayOffsides)

    // 7. Fouls: eventType IN ('FREE_KICK','DANGEROUS_FREE_KICK')
    val homeFouls = allEvents.count { (it.eventType in listOf("FREE_KICK", "DANGEROUS_FREE_KICK")) && it.team.equals("HOME", true) }
    val awayFouls = allEvents.count { (it.eventType in listOf("FREE_KICK", "DANGEROUS_FREE_KICK")) && it.team.equals("AWAY", true) }
    assertEquals(3, homeFouls)
    assertEquals(2, awayFouls)

    // 8. Yellow: CARD + cardType='Yellow'
    val homeYellows = allEvents.count { it.eventType == "CARD" && it.team.equals("HOME", true) && it.extraDataJson?.contains("\"cardType\":\"Yellow\"") == true }
    val awayYellows = allEvents.count { it.eventType == "CARD" && it.team.equals("AWAY", true) && it.extraDataJson?.contains("\"cardType\":\"Yellow\"") == true }
    assertEquals(2, homeYellows)
    assertEquals(1, awayYellows)

    // 9. Red: CARD + cardType IN ('Red','Yellow-Red')
    val homeReds = allEvents.count { it.eventType == "CARD" && it.team.equals("HOME", true) && (it.extraDataJson?.contains("\"cardType\":\"Red\"") == true || it.extraDataJson?.contains("\"cardType\":\"Yellow-Red\"") == true) }
    val awayReds = allEvents.count { it.eventType == "CARD" && it.team.equals("AWAY", true) && (it.extraDataJson?.contains("\"cardType\":\"Red\"") == true || it.extraDataJson?.contains("\"cardType\":\"Yellow-Red\"") == true) }
    assertEquals(0, homeReds)
    assertEquals(1, awayReds)

    // Penalties condition:
    val loadedMatch = repository.getMatch(matchId)
    assertNotNull(loadedMatch)
    assertEquals(4, loadedMatch?.homePenScore)
    assertEquals(3, loadedMatch?.awayPenScore)
  }

  @Test
  fun `test ai summary room storage and retrieval`() = runBlocking {
    val now = System.currentTimeMillis()
    val match = MatchEntity(
      homeTeam = "Police FC",
      awayTeam = "Mukura Victory",
      competition = "Rwanda Premier League",
      venue = "Kigali Pelé Stadium",
      matchDateMillis = now,
      kickoffTimeMillis = now,
      status = "FINISHED",
      homeScore = 3,
      awayScore = 0
    )
    val matchId = repository.insertMatch(match)

    val aiSummaryText = """
      Police FC delivered a dominant attacking performance to defeat Mukura Victory 3-0 at Kigali Pelé Stadium.
      Striker Muhadjiri Hakizimana opened the scoring in the 24th minute with a composed finish inside the penalty box, followed by two quick second-half goals in the 56th and 72nd minutes.
      The law enforcers controlled 62% possession and generated 14 total shots with exceptional tactical organization, while Mukura struggled to penetrate the defensive block.
    """.trimIndent()

    val summaryEntity = com.example.data.model.SummaryEntity(
      matchId = matchId,
      summaryText = aiSummaryText,
      generatedAtMillis = now
    )
    repository.insertSummary(summaryEntity)

    val loadedSummary = repository.getSummary(matchId)
    assertNotNull(loadedSummary)
    assertEquals(matchId, loadedSummary?.matchId)
    assertTrue(loadedSummary?.summaryText?.contains("Police FC delivered a dominant") == true)
    assertTrue(loadedSummary?.summaryText?.contains("Muhadjiri Hakizimana") == true)
  }

  @Test
  fun `test no internet exception message`() {
    val exception = com.example.data.ai.NoInternetException()
    assertEquals("AI summary requires internet", exception.message)
  }

  @Test
  fun `test accessibility switches and datastore preferences`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = com.example.data.preferences.ScoutPreferences(context)
    
    // Default values
    val defaultMotion = prefs.reduceMotionFlow.first()
    val defaultContrast = prefs.highContrastFlow.first()
    val defaultLargerText = prefs.largerTextFlow.first()
    val defaultAiSummary = prefs.aiSummaryFlow.first()
    val defaultScoutName = prefs.scoutNameFlow.first()

    assertEquals(false, defaultMotion)
    assertEquals(false, defaultContrast)
    assertEquals(false, defaultLargerText)
    assertEquals(false, defaultAiSummary)
    assertEquals("", defaultScoutName)

    // Toggle each accessibility switch and scout name
    prefs.setReduceMotion(true)
    prefs.setHighContrast(true)
    prefs.setLargerText(true)
    prefs.setAiSummary(true)
    prefs.setScoutName("Jean Paul Gasore")

    assertEquals(true, prefs.reduceMotionFlow.first())
    assertEquals(true, prefs.highContrastFlow.first())
    assertEquals(true, prefs.largerTextFlow.first())
    assertEquals(true, prefs.aiSummaryFlow.first())
    assertEquals("Jean Paul Gasore", prefs.scoutNameFlow.first())
  }

  @Test
  fun `test delete match cascade removes all related rows`() = runBlocking {
    val now = System.currentTimeMillis()
    val match = MatchEntity(
      homeTeam = "AS Kigali",
      awayTeam = "Gorilla FC",
      competition = "Rwanda Premier League",
      venue = "Kigali Pelé Stadium",
      matchDateMillis = now,
      kickoffTimeMillis = now,
      status = "LIVE",
      homeScore = 1,
      awayScore = 0
    )
    val matchId = repository.insertMatch(match)

    // Insert events, lineups, ball taps, summary
    repository.insertEvent(com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "GOAL",
      team = "HOME",
      playerName = "Striker X",
      matchTimeSeconds = 120,
      matchTimeDisplay = "02:00",
      period = "FIRST_HALF"
    ))
    repository.insertLineup(com.example.data.model.LineupEntity(
      matchId = matchId,
      team = "HOME",
      playerName = "Striker X",
      jerseyNumber = 9,
      position = "CF",
      isStarter = true
    ))
    repository.insertBallTap(com.example.data.model.BallTapEntity(
      matchId = matchId,
      x = 0.5f,
      y = 0.5f,
      side = "HOME"
    ))
    repository.insertSummary(com.example.data.model.SummaryEntity(
      matchId = matchId,
      summaryText = "Sample match summary"
    ))

    // Verify entities exist
    assertNotNull(repository.getMatch(matchId))
    assertEquals(1, repository.getEvents(matchId).size)
    assertEquals(1, repository.getLineups(matchId).size)
    assertEquals(1, repository.getBallTaps(matchId).size)
    assertNotNull(repository.getSummary(matchId))

    // Delete match with cascade
    repository.deleteMatchById(matchId)

    // Verify all rows cascade removed
    assertNull(repository.getMatch(matchId))
    assertEquals(0, repository.getEvents(matchId).size)
    assertEquals(0, repository.getLineups(matchId).size)
    assertEquals(0, repository.getBallTaps(matchId).size)
    assertNull(repository.getSummary(matchId))
  }

  @Test
  fun `test export and import all data json`() = runBlocking {
    val now = System.currentTimeMillis()
    val match = MatchEntity(
      homeTeam = "Marines FC",
      awayTeam = "Etincelles FC",
      competition = "Rwanda Premier League",
      venue = "Umuganda Stadium (Rubavu)",
      matchDateMillis = now,
      kickoffTimeMillis = now,
      status = "FINISHED",
      homeScore = 2,
      awayScore = 2
    )
    val matchId = repository.insertMatch(match)

    repository.insertEvent(com.example.data.model.EventEntity(
      matchId = matchId,
      eventType = "GOAL",
      team = "HOME",
      playerName = "Rubavu Star",
      matchTimeSeconds = 240,
      matchTimeDisplay = "04:00",
      period = "FIRST_HALF"
    ))

    // Total events count
    val totalEvents = repository.getTotalEventCount()
    assertTrue(totalEvents >= 1)

    // Export JSON
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = ScoutViewModel(
      app,
      repository
    )
    val exportedJson = viewModel.exportAllDataJson()
    assertTrue(exportedJson.contains("\"version\": 1"))
    assertTrue(exportedJson.contains("Marines FC"))
    assertTrue(exportedJson.contains("Rubavu Star"))

    // Clear all data
    repository.clearAllData()
    assertEquals(0, repository.getAllMatchesList().size)
    assertEquals(0, repository.getAllEvents().size)

    // Import the JSON back
    var importSuccess = false
    viewModel.importDataJson(exportedJson) { success: Boolean, _: String ->
      importSuccess = success
    }
    for (i in 0 until 30) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (importSuccess) break
      kotlinx.coroutines.delay(100)
    }
    assertTrue("Data import should report success", importSuccess)

    val reloadedMatches = repository.getAllMatchesList()
    assertTrue(reloadedMatches.any { it.homeTeam == "Marines FC" })
    val reloadedEvents = repository.getAllEvents()
    assertTrue(reloadedEvents.any { it.playerName == "Rubavu Star" })
  }

  @Test
  fun `test storage indicator updates and clear all data via Room clearAllTables`() = runBlocking {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = ScoutViewModel(app, repository)

    // Initially storageSizeMb is calculated
    viewModel.calculateStorage()
    val storageText = viewModel.storageSizeMb.value
    assertTrue("Storage string should contain MB", storageText.contains("MB"))

    // Insert sample derby match
    val matchId = repository.createSampleDerbyMatch()
    assertTrue(matchId > 0)
    assertTrue(repository.getAllMatchesList().isNotEmpty())
    assertTrue(repository.getAllEvents().isNotEmpty())

    // Clear all data using clearAllTables
    var cleared = false
    viewModel.clearAllData {
      cleared = true
    }

    for (i in 0 until 20) {
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      if (cleared) break
      kotlinx.coroutines.delay(50)
    }
    assertTrue(cleared)
    assertEquals(0, repository.getAllMatchesList().size)
    assertEquals(0, repository.getAllEvents().size)
    assertEquals(0, repository.getAllLineups().size)
    assertEquals(0, repository.getAllBallTaps().size)
    assertEquals(0, repository.getAllSummaries().size)
  }
}
