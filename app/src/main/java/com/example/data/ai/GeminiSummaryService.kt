package com.example.data.ai

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.BuildConfig
import com.example.data.model.BallTapEntity
import com.example.data.model.EventEntity
import com.example.data.model.MatchEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class NoInternetException(message: String = "AI summary requires internet") : IOException(message)

object GeminiSummaryService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun generateMatchSummary(
        context: Context,
        match: MatchEntity,
        events: List<EventEntity>,
        ballTaps: List<BallTapEntity>
    ): String = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable(context)) {
            throw NoInternetException()
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) {
            throw IllegalStateException("Gemini API key is not configured in Secrets panel")
        }

        // Build match data JSON
        val matchDataJson = buildMatchDataJson(match, events, ballTaps)

        // Build prompt strictly matching user specification:
        // "You are a football match reporter for the Rwanda Premier League.
        //  Write a concise, professional match summary in 3-4 paragraphs.
        //  Include: result, key moments, goalscorers with minute, standout stats.
        //  Neutral, broadcast-like. Do NOT invent events.
        //  MATCH DATA: [paste JSON]"
        val prompt = buildString {
            appendLine("You are a football match reporter for the Rwanda Premier League.")
            appendLine("Write a concise, professional match summary in 3-4 paragraphs.")
            appendLine("Include: result, key moments, goalscorers with minute, standout stats.")
            appendLine("Neutral, broadcast-like. Do NOT invent events.")
            appendLine("MATCH DATA:")
            appendLine(matchDataJson)
        }

        // Try gemini-1.5-flash as specified, with graceful fallback to gemini-2.5-flash if 1.5 is deprecated
        try {
            executeGeminiRequest("gemini-1.5-flash", apiKey, prompt)
        } catch (e: Exception) {
            // Fallback to gemini-2.5-flash if gemini-1.5-flash is not available on this API key tier
            executeGeminiRequest("gemini-2.5-flash", apiKey, prompt)
        }
    }

    private fun executeGeminiRequest(model: String, apiKey: String, prompt: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val requestBodyJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        val partObj = JSONObject().apply {
                            put("text", prompt)
                        }
                        put(partObj)
                    }
                    put("parts", partsArray)
                }
                put(contentObj)
            }
            put("contents", contentsArray)

            // Optional generation config
            val genConfig = JSONObject().apply {
                put("temperature", 0.3)
                put("maxOutputTokens", 1024)
            }
            put("generationConfig", genConfig)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestBodyJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val errorMsg = try {
                val errorJson = JSONObject(responseBody)
                errorJson.optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
            } catch (_: Exception) {
                "HTTP ${response.code}: $responseBody"
            }
            throw IOException("Gemini API Error: $errorMsg")
        }

        val json = JSONObject(responseBody)
        val candidates = json.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
                val text = parts.getJSONObject(0).optString("text")
                if (text.isNotBlank()) {
                    return text.trim()
                }
            }
        }

        throw IOException("No summary text returned by Gemini API")
    }

    private fun buildMatchDataJson(
        match: MatchEntity,
        events: List<EventEntity>,
        ballTaps: List<BallTapEntity>
    ): String {
        val root = JSONObject().apply {
            put("homeTeam", match.homeTeam)
            put("awayTeam", match.awayTeam)
            put("score", "${match.homeScore} - ${match.awayScore}")
            if (match.homePenScore > 0 || match.awayPenScore > 0) {
                put("penalties", "${match.homePenScore} - ${match.awayPenScore}")
            }
            put("competition", match.competition)
            put("venue", match.venue)
            put("status", match.status)
            put("period", match.currentPeriod)
            if (match.attendance != null) {
                put("attendance", match.attendance)
            }
            if (match.weather != null) {
                put("weather", match.weather)
            }

            // Stats
            val homeTaps = ballTaps.count { it.side.equals("HOME", true) }
            val awayTaps = ballTaps.count { it.side.equals("AWAY", true) }
            val totalTaps = homeTaps + awayTaps
            val homePoss = if (totalTaps > 0) (homeTaps * 100 / totalTaps) else 50
            val awayPoss = 100 - homePoss

            val statsObj = JSONObject().apply {
                put("possession", "$homePoss% vs $awayPoss%")
                put("totalShotsHome", events.count { it.eventType == "SHOT" && it.team.equals("HOME", true) })
                put("totalShotsAway", events.count { it.eventType == "SHOT" && it.team.equals("AWAY", true) })
                put("cornersHome", events.count { it.eventType == "CORNER" && it.team.equals("HOME", true) })
                put("cornersAway", events.count { it.eventType == "CORNER" && it.team.equals("AWAY", true) })
                put("foulsHome", events.count { it.eventType in listOf("FREE_KICK", "DANGEROUS_FREE_KICK") && it.team.equals("HOME", true) })
                put("foulsAway", events.count { it.eventType in listOf("FREE_KICK", "DANGEROUS_FREE_KICK") && it.team.equals("AWAY", true) })
                put("yellowCardsHome", events.count { (it.eventType == "YELLOW_CARD" || (it.eventType == "CARD" && it.extraDataJson?.contains("Yellow") == true && it.extraDataJson?.contains("Yellow-Red") != true)) && it.team.equals("HOME", true) })
                put("yellowCardsAway", events.count { (it.eventType == "YELLOW_CARD" || (it.eventType == "CARD" && it.extraDataJson?.contains("Yellow") == true && it.extraDataJson?.contains("Yellow-Red") != true)) && it.team.equals("AWAY", true) })
                put("redCardsHome", events.count { (it.eventType == "RED_CARD" || (it.eventType == "CARD" && (it.extraDataJson?.contains("Red") == true || it.extraDataJson?.contains("Yellow-Red") == true))) && it.team.equals("HOME", true) })
                put("redCardsAway", events.count { (it.eventType == "RED_CARD" || (it.eventType == "CARD" && (it.extraDataJson?.contains("Red") == true || it.extraDataJson?.contains("Yellow-Red") == true))) && it.team.equals("AWAY", true) })
            }
            put("stats", statsObj)

            // Key Events
            val eventsArray = JSONArray()
            events.sortedBy { it.matchTimeSeconds }.forEach { ev ->
                val evObj = JSONObject().apply {
                    put("time", ev.matchTimeDisplay)
                    put("type", ev.eventType)
                    put("team", ev.team)
                    put("player", ev.playerName)
                    if (!ev.extraDataJson.isNullOrBlank()) {
                        put("extra", ev.extraDataJson)
                    }
                }
                eventsArray.put(evObj)
            }
            put("events", eventsArray)
        }
        return root.toString(2)
    }
}
