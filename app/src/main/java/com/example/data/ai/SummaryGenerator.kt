package com.example.data.ai

import com.example.data.model.BallTapEntity
import com.example.data.model.EventEntity
import com.example.data.model.MatchEntity

object SummaryGenerator {

    fun generateScoutSummary(
        match: MatchEntity,
        events: List<EventEntity>,
        ballTaps: List<BallTapEntity>
    ): String {
        val goals = events.filter { it.eventType == "GOAL" }
        val cards = events.filter { it.eventType == "YELLOW_CARD" || it.eventType == "RED_CARD" }
        val dangerousTaps = ballTaps.filter { it.isDangerous }

        val winnerText = when {
            match.homeScore > match.awayScore -> "${match.homeTeam} secured a decisive victory"
            match.awayScore > match.homeScore -> "${match.awayTeam} triumphed on the road"
            else -> "A fiercely contested draw between ${match.homeTeam} and ${match.awayTeam}"
        }

        val goalRecap = if (goals.isEmpty()) {
            "Neither side managed to find the back of the net in this tactical encounter."
        } else {
            val listStr = goals.joinToString("; ") { "${it.playerName} (${it.team}, ${it.matchTimeDisplay})" }
            "Goalscorers: $listStr."
        }

        val disciplineRecap = if (cards.isEmpty()) {
            "Match played with exceptional discipline; no bookings recorded."
        } else {
            val reds = cards.filter { it.eventType == "RED_CARD" }.size
            val yellows = cards.filter { it.eventType == "YELLOW_CARD" }.size
            "Disciplinary summary: $yellows caution(s) and $reds dismissal(s) issued."
        }

        val territoryRecap = if (ballTaps.isNotEmpty()) {
            val homeTaps = ballTaps.filter { it.side == "HOME" }.size
            val awayTaps = ballTaps.filter { it.side == "AWAY" }.size
            "Attacking volume: ${match.homeTeam} registered $homeTaps recorded attacking touches ($dangerousTaps dangerous box actions) vs $awayTaps for ${match.awayTeam}."
        } else {
            "Attacking territory remained evenly distributed across both halves."
        }

        val weatherNote = match.weather?.let { "Played under $it conditions at ${match.venue}." } ?: "Held at ${match.venue}."

        return buildString {
            appendLine("RWANDA FOOTBALL SCOUT — TACTICAL MATCH REPORT")
            appendLine("Competition: ${match.competition}")
            appendLine("Fixture: ${match.homeTeam} ${match.homeScore} - ${match.awayScore} ${match.awayTeam}")
            appendLine("Status: ${match.status} (${match.currentPeriod.replace("_", " ")})")
            appendLine("Venue: $weatherNote")
            appendLine()
            appendLine("1. EXECUTIVE SUMMARY:")
            appendLine("$winnerText with a final scoreline of ${match.homeScore}-${match.awayScore}. $goalRecap")
            appendLine()
            appendLine("2. DISCIPLINE & TEMPO:")
            appendLine(disciplineRecap)
            appendLine()
            appendLine("3. TERRITORIAL & CHANCE THREAT:")
            appendLine(territoryRecap)
            appendLine()
            appendLine("4. SCOUTING VERDICT:")
            if (goals.isNotEmpty()) {
                val standout = goals.first().playerName
                appendLine("Key impact player: $standout demonstrated sharp positional awareness in the final third.")
            } else {
                appendLine("Both defensive units maintained compact low blocks, stifling interior penetration.")
            }
            appendLine("Generated offline by Rwanda Football Scout.")
        }
    }
}
