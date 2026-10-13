package com.example.ui.components

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.model.EventEntity
import com.example.data.model.MatchEntity
import com.example.ui.screens.CalculatedMatchStats
import com.example.ui.theme.*
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import kotlin.math.cos
import kotlin.math.sin

/**
 * Rwandan 24-ray Golden Sun Emblem
 * Distinctive national symbol from the Rwandan Flag.
 */
@Composable
fun RwandaSunEmblem(
    modifier: Modifier = Modifier.size(24.dp),
    color: Color = RwandaSunGold
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val discRadius = w * 0.22f
        val rayInner = w * 0.28f
        val rayOuter = w * 0.48f

        // Central disc
        drawCircle(
            color = color,
            radius = discRadius,
            center = Offset(cx, cy)
        )

        // 24 radiating rays
        val raysCount = 24
        val strokeWidth = 1.6.dp.toPx()
        for (i in 0 until raysCount) {
            val angle = (i * (360.0 / raysCount)) * (Math.PI / 180.0)
            val startX = cx + (rayInner * cos(angle)).toFloat()
            val startY = cy + (rayInner * sin(angle)).toFloat()
            val endX = cx + (rayOuter * cos(angle)).toFloat()
            val endY = cy + (rayOuter * sin(angle)).toFloat()

            drawLine(
                color = color,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Rwandan National Flag Ribbon Banner
 * Proportional 3-stripe national flag:
 * Sky Blue (double width, represents happiness & peace) with Golden Sun
 * Sun Yellow (represents economic development)
 * Forest Green (represents prosperity & nature)
 */
@Composable
fun RwandaFlagRibbon(
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(10.dp)
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Sky Blue stripe (50% of height)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f)
                    .background(RwandaSkyBlue)
            )
            // Sun Yellow stripe (25% of height)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.25f)
                    .background(RwandaSunGold)
            )
            // Forest Green stripe (25% of height)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.25f)
                    .background(RwandaForestGreen)
            )
        }
    }
}

/**
 * Match Report Hero Banner with Rwandan Flag styling
 */
@Composable
fun RwandaMatchReportHero(
    match: MatchEntity,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("rwanda_report_hero_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = RwandaBroadcastNavy),
        border = BorderStroke(1.5.dp, RwandaBroadcastBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Rwanda Flag Ribbon
            RwandaFlagRibbon()

            // Header Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Tag: Competition + Sun Emblem
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = RwandaSkyBlue.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, RwandaSkyBlue.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = RwandaSunGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = (match.competition ?: "RWANDA PREMIER LEAGUE").uppercase(),
                                style = ScoutTypographyCaption.copy(
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color.White
                            )
                        }
                    }

                    // Sun Emblem badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RwandaSunEmblem(modifier = Modifier.size(20.dp))
                        Text(
                            text = "FERWAFA OFFICIAL",
                            style = ScoutTypographyCaption.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = RwandaSunGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scoreboard Row
                val homeColor = parseTeamColor(match.homeColor, isHome = true)
                val awayColor = parseTeamColor(match.awayColor, isHome = false)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Home Team
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TacticalJerseyBadge(color = homeColor, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = match.homeTeam,
                            style = ScoutTypographyTitleL.copy(
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "HOME",
                            style = ScoutTypographyCaption.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = RwandaSkyBlue
                        )
                    }

                    // Score Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.55f),
                        border = BorderStroke(1.5.dp, RwandaSunGold.copy(alpha = 0.8f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${match.homeScore}  :  ${match.awayScore}",
                                style = ScoutTypographyDisplay.copy(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = if (match.status.equals("FINISHED", ignoreCase = true)) "FULL TIME" else match.status,
                                style = ScoutTypographyCaption.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = RwandaSunGold
                            )
                        }
                    }

                    // Away Team
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TacticalJerseyBadge(color = awayColor, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = match.awayTeam,
                            style = ScoutTypographyTitleL.copy(
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "AWAY",
                            style = ScoutTypographyCaption.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = RwandaForestGreenLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = RwandaBroadcastBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Venue & Date Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = RwandaSkyBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = match.venue ?: "Amahoro National Stadium, Kigali",
                            style = ScoutTypographyCaption.copy(fontSize = 12.sp),
                            color = ScoutReportTextMuted
                        )
                    }

                    val formattedDate = DateFormat.format("d MMM yyyy", Date(match.matchDateMillis)).toString()
                    Text(
                        text = formattedDate,
                        style = ScoutTypographyCaption.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = ScoutReportTextMuted
                    )
                }
            }
        }
    }
}

/**
 * Eye-Catching Television Broadcast Match Report Card (In-App Preview)
 * Replicates the televised full-time graphic used by major African and European football broadcasters.
 */
@Composable
fun BroadcastMatchReportPreviewCard(
    match: MatchEntity,
    stats: CalculatedMatchStats,
    events: List<EventEntity>,
    onDownloadImage: () -> Unit,
    onShareImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("broadcast_report_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RwandaBroadcastNavy),
        border = BorderStroke(2.dp, RwandaSunGold.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Tricolor Ribbon Banner
            RwandaFlagRibbon(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Broadcast Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RwandaSunEmblem(modifier = Modifier.size(22.dp))
                        Column {
                            Text(
                                text = "MATCHDAY BROADCAST",
                                style = ScoutTypographyLabelL.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = RwandaSunGold
                            )
                            Text(
                                text = (match.competition ?: "RWANDA PREMIER LEAGUE").uppercase(),
                                style = ScoutTypographyCaption.copy(fontSize = 10.5.sp),
                                color = RwandaSkyBlue
                            )
                        }
                    }

                    // "FULL TIME" Television badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = RwandaForestGreen,
                        border = BorderStroke(1.dp, RwandaSunGold.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = RwandaSunGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "FULL TIME",
                                style = ScoutTypographyCaption.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // High-Impact Score Card
                val homeColor = parseTeamColor(match.homeColor, isHome = true)
                val awayColor = parseTeamColor(match.awayColor, isHome = false)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = RwandaBroadcastSurface,
                    border = BorderStroke(1.dp, RwandaBroadcastBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Home Team side
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            TacticalJerseyBadge(color = homeColor, modifier = Modifier.size(32.dp))
                            Column {
                                Text(
                                    text = match.homeTeam,
                                    style = ScoutTypographyTitleL.copy(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "HOME",
                                    style = ScoutTypographyCaption.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = RwandaSkyBlue
                                )
                            }
                        }

                        // Big Score
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.65f),
                            border = BorderStroke(1.dp, RwandaSunGold)
                        ) {
                            Text(
                                text = "${match.homeScore} - ${match.awayScore}",
                                style = ScoutTypographyDisplay.copy(
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }

                        // Away Team side
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = match.awayTeam,
                                    style = ScoutTypographyTitleL.copy(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "AWAY",
                                    style = ScoutTypographyCaption.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = RwandaForestGreenLight
                                )
                            }
                            TacticalJerseyBadge(color = awayColor, modifier = Modifier.size(32.dp))
                        }
                    }
                }

                // Scorers Summary if available
                val goalEvents = events.filter { it.eventType.equals("GOAL", ignoreCase = true) }
                if (goalEvents.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, RwandaBroadcastBorder.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            goalEvents.take(4).forEach { g ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SportsSoccer,
                                        contentDescription = null,
                                        tint = RwandaSunGold,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "${g.matchTimeDisplay}  ${g.playerName} (${g.team})",
                                        style = ScoutTypographyCaption.copy(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Comparison Section (Rwanda Flag styling)
                Text(
                    text = "MATCH STATS COMPARISON",
                    style = ScoutTypographyCaption.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = RwandaSunGold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BroadcastStatBar(
                        label = "Possession",
                        homeVal = "${stats.home.possessionPercent}%",
                        awayVal = "${stats.away.possessionPercent}%",
                        homeRatio = stats.home.possessionPercent / 100f
                    )
                    BroadcastStatBar(
                        label = "Shots on Target",
                        homeVal = "${stats.home.shotsOnTarget}",
                        awayVal = "${stats.away.shotsOnTarget}",
                        homeRatio = if (stats.home.shotsOnTarget + stats.away.shotsOnTarget > 0)
                            stats.home.shotsOnTarget.toFloat() / (stats.home.shotsOnTarget + stats.away.shotsOnTarget)
                        else 0.5f
                    )
                    BroadcastStatBar(
                        label = "Total Shots",
                        homeVal = "${stats.home.totalShots}",
                        awayVal = "${stats.away.totalShots}",
                        homeRatio = if (stats.home.totalShots + stats.away.totalShots > 0)
                            stats.home.totalShots.toFloat() / (stats.home.totalShots + stats.away.totalShots)
                        else 0.5f
                    )
                    BroadcastStatBar(
                        label = "Corners",
                        homeVal = "${stats.home.corners}",
                        awayVal = "${stats.away.corners}",
                        homeRatio = if (stats.home.corners + stats.away.corners > 0)
                            stats.home.corners.toFloat() / (stats.home.corners + stats.away.corners)
                        else 0.5f
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = RwandaBroadcastBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons: Download and Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Download PNG Button
                    ScoutButton(
                        text = "Download Card",
                        onClick = onDownloadImage,
                        style = ScoutButtonStyle.PRIMARY,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("download_broadcast_image_btn")
                    )

                    // Share PNG Button
                    ScoutButton(
                        text = "Share Graphic",
                        onClick = onShareImage,
                        style = ScoutButtonStyle.SECONDARY,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_broadcast_image_btn")
                    )
                }
            }
        }
    }
}

@Composable
private fun BroadcastStatBar(
    label: String,
    homeVal: String,
    awayVal: String,
    homeRatio: Float
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = homeVal,
                style = ScoutTypographyCaption.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                color = RwandaSkyBlue
            )
            Text(
                text = label,
                style = ScoutTypographyCaption.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Medium),
                color = ScoutReportTextMuted
            )
            Text(
                text = awayVal,
                style = ScoutTypographyCaption.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                color = RwandaForestGreenLight
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Progress bar: Sky Blue on left, Forest Green on right
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(RwandaBroadcastBorder)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                val clampedHomeRatio = homeRatio.coerceIn(0.05f, 0.95f)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(clampedHomeRatio)
                        .background(RwandaSkyBlue)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f - clampedHomeRatio)
                        .background(RwandaForestGreenLight)
                )
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// HIGH-RESOLUTION BROADCAST IMAGE GENERATOR (PNG BITMAP)
// ═════════════════════════════════════════════════════════════════════════════

/**
 * Generates an eye-catching 1080x1350 broadcast card bitmap matching professional televised graphics.
 */
fun generateBroadcastReportBitmap(
    context: Context,
    match: MatchEntity,
    stats: CalculatedMatchStats,
    events: List<EventEntity>
): Bitmap {
    val width = 1080
    val height = 1350
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // 1. Background Gradient (Deep Rwanda Navy)
    val bgPaint = Paint().apply {
        shader = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            intArrayOf(
                android.graphics.Color.parseColor("#0A1324"),
                android.graphics.Color.parseColor("#12233C"),
                android.graphics.Color.parseColor("#0A1324")
            ),
            null,
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    // 2. Subtle pitch line watermarks in background
    val watermarkPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#1A2D48")
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }
    canvas.drawCircle(width / 2f, height / 2f, 220f, watermarkPaint)
    canvas.drawLine(0f, height / 2f, width.toFloat(), height / 2f, watermarkPaint)
    canvas.drawRect(80f, 120f, width - 80f, height - 120f, watermarkPaint)

    // 3. Rwanda National Flag Top Banner (Sky Blue 28px, Sun Gold 14px, Forest Green 14px)
    val skyBluePaint = Paint().apply { color = android.graphics.Color.parseColor("#00A1DE") }
    val sunGoldPaint = Paint().apply { color = android.graphics.Color.parseColor("#FAD201"); isAntiAlias = true }
    val forestGreenPaint = Paint().apply { color = android.graphics.Color.parseColor("#20603D") }

    canvas.drawRect(0f, 0f, width.toFloat(), 34f, skyBluePaint)
    canvas.drawRect(0f, 34f, width.toFloat(), 50f, sunGoldPaint)
    canvas.drawRect(0f, 50f, width.toFloat(), 66f, forestGreenPaint)

    // 4. Header Bar: Sun Emblem + Competition Name
    var y = 130f
    drawSunEmblemToCanvas(canvas, 100f, y - 8f, 36f, sunGoldPaint)

    val headerTextPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#FAD201")
        textSize = 34f
        isFakeBoldText = true
        isAntiAlias = true
    }
    canvas.drawText("RWANDA FOOTBALL LEAGUE", 160f, y, headerTextPaint)

    val subtitlePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#00A1DE")
        textSize = 24f
        isFakeBoldText = true
        isAntiAlias = true
    }
    y += 34f
    canvas.drawText((match.competition ?: "OFFICIAL MATCHDAY BROADCAST REPORT").uppercase(), 160f, y, subtitlePaint)

    // Full-Time Badge on right
    val badgePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#20603D")
        isAntiAlias = true
    }
    val badgeBorderPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#FAD201")
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }
    val badgeRect = RectF(width - 320f, 100f, width - 80f, 160f)
    canvas.drawRoundRect(badgeRect, 16f, 16f, badgePaint)
    canvas.drawRoundRect(badgeRect, 16f, 16f, badgeBorderPaint)

    val badgeTextPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 28f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    canvas.drawText("FULL TIME", badgeRect.centerX(), badgeRect.centerY() + 10f, badgeTextPaint)

    // 5. Team Card Box with Scores
    y = 230f
    val cardBox = RectF(60f, y, width - 60f, y + 260f)
    val cardBgPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#132238")
        isAntiAlias = true
    }
    val cardStrokePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#1E3A60")
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }
    canvas.drawRoundRect(cardBox, 24f, 24f, cardBgPaint)
    canvas.drawRoundRect(cardBox, 24f, 24f, cardStrokePaint)

    // Home Team Name
    val teamNamePaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 42f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    val sideLabelPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#00A1DE")
        textSize = 22f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    canvas.drawText(match.homeTeam, cardBox.left + 230f, cardBox.centerY() - 10f, teamNamePaint)
    canvas.drawText("HOME", cardBox.left + 230f, cardBox.centerY() + 30f, sideLabelPaint)

    // Away Team Name
    val awaySideLabelPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#2E8B57")
        textSize = 22f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    canvas.drawText(match.awayTeam, cardBox.right - 230f, cardBox.centerY() - 10f, teamNamePaint)
    canvas.drawText("AWAY", cardBox.right - 230f, cardBox.centerY() + 30f, awaySideLabelPaint)

    // Central Score Box
    val scoreBox = RectF(cardBox.centerX() - 140f, cardBox.centerY() - 70f, cardBox.centerX() + 140f, cardBox.centerY() + 70f)
    val scoreBgPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#070E1A")
        isAntiAlias = true
    }
    val scoreBorderPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#FAD201")
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }
    canvas.drawRoundRect(scoreBox, 20f, 20f, scoreBgPaint)
    canvas.drawRoundRect(scoreBox, 20f, 20f, scoreBorderPaint)

    val scoreTextPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 72f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    canvas.drawText("${match.homeScore} - ${match.awayScore}", scoreBox.centerX(), scoreBox.centerY() + 24f, scoreTextPaint)

    // 6. Venue & Date Info under score
    y = cardBox.bottom + 45f
    val infoPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#94A3B8")
        textSize = 22f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    val venueText = "${match.venue ?: "Amahoro Stadium, Kigali"}  •  ${DateFormat.format("EEEE, d MMMM yyyy", Date(match.matchDateMillis))}"
    canvas.drawText(venueText, width / 2f, y, infoPaint)

    // 7. Goal Scorers List
    y += 50f
    val goalEvents = events.filter { it.eventType.equals("GOAL", ignoreCase = true) }
    if (goalEvents.isNotEmpty()) {
        val scorerBox = RectF(80f, y, width - 80f, y + (goalEvents.size.coerceAtMost(3) * 36f) + 30f)
        val scorerBg = Paint().apply {
            color = android.graphics.Color.parseColor("#0D192B")
            isAntiAlias = true
        }
        canvas.drawRoundRect(scorerBox, 16f, 16f, scorerBg)

        var sy = y + 36f
        val goalIconPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#FAD201")
            textSize = 22f
            isAntiAlias = true
        }
        val scorerTextPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 24f
            isAntiAlias = true
        }
        for (g in goalEvents.take(3)) {
            canvas.drawText("⚽", 110f, sy, goalIconPaint)
            canvas.drawText("${g.matchTimeDisplay}  ${g.playerName} (${g.team})", 160f, sy, scorerTextPaint)
            sy += 36f
        }
        y = scorerBox.bottom + 45f
    } else {
        y += 20f
    }

    // 8. Stats Comparison Title
    val statsTitlePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#FAD201")
        textSize = 28f
        isFakeBoldText = true
        letterSpacing = 0.08f
        isAntiAlias = true
    }
    canvas.drawText("MATCH STATS COMPARISON", 80f, y, statsTitlePaint)
    y += 35f

    // 9. Stats Comparison Rows
    val statsList = listOf(
        Triple("Possession", "${stats.home.possessionPercent}%", "${stats.away.possessionPercent}%"),
        Triple("Shots on Target", "${stats.home.shotsOnTarget}", "${stats.away.shotsOnTarget}"),
        Triple("Total Shots", "${stats.home.totalShots}", "${stats.away.totalShots}"),
        Triple("Corners", "${stats.home.corners}", "${stats.away.corners}"),
        Triple("Fouls", "${stats.home.fouls}", "${stats.away.fouls}")
    )

    val statLabelPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#94A3B8")
        textSize = 24f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    val statValHomePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#00A1DE")
        textSize = 28f
        isFakeBoldText = true
        isAntiAlias = true
    }
    val statValAwayPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#2E8B57")
        textSize = 28f
        isFakeBoldText = true
        textAlign = Paint.Align.RIGHT
        isAntiAlias = true
    }
    val barTrackPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#1E3557")
        isAntiAlias = true
    }
    val barHomePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#00A1DE")
        isAntiAlias = true
    }
    val barAwayPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#2E8B57")
        isAntiAlias = true
    }

    val barWidth = width - 160f
    for ((label, homeVal, awayVal) in statsList) {
        canvas.drawText(homeVal, 80f, y + 22f, statValHomePaint)
        canvas.drawText(label, width / 2f, y + 20f, statLabelPaint)
        canvas.drawText(awayVal, width - 80f, y + 22f, statValAwayPaint)

        y += 34f
        val barRect = RectF(80f, y, 80f + barWidth, y + 14f)
        canvas.drawRoundRect(barRect, 7f, 7f, barTrackPaint)

        val homeRatio = when (label) {
            "Possession" -> stats.home.possessionPercent / 100f
            "Shots on Target" -> if (stats.home.shotsOnTarget + stats.away.shotsOnTarget > 0)
                stats.home.shotsOnTarget.toFloat() / (stats.home.shotsOnTarget + stats.away.shotsOnTarget) else 0.5f
            "Total Shots" -> if (stats.home.totalShots + stats.away.totalShots > 0)
                stats.home.totalShots.toFloat() / (stats.home.totalShots + stats.away.totalShots) else 0.5f
            "Corners" -> if (stats.home.corners + stats.away.corners > 0)
                stats.home.corners.toFloat() / (stats.home.corners + stats.away.corners) else 0.5f
            else -> 0.5f
        }.coerceIn(0.08f, 0.92f)

        val splitX = 80f + (barWidth * homeRatio)
        val homeBar = RectF(80f, y, splitX - 2f, y + 14f)
        val awayBar = RectF(splitX + 2f, y, 80f + barWidth, y + 14f)
        canvas.drawRoundRect(homeBar, 7f, 7f, barHomePaint)
        canvas.drawRoundRect(awayBar, 7f, 7f, barAwayPaint)

        y += 40f
    }

    // 10. Bottom Verification Footer
    val bottomY = height - 70f
    val footerPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#FAD201")
        textSize = 22f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
        letterSpacing = 0.05f
        isAntiAlias = true
    }
    canvas.drawText("★  OFFICIAL FERWAFA MATCH REPORT  •  RWANDA FOOTBALL SCOUT  ★", width / 2f, bottomY, footerPaint)

    // Bottom Rwanda Flag Ribbon
    canvas.drawRect(0f, height - 16f, width.toFloat(), height - 12f, skyBluePaint)
    canvas.drawRect(0f, height - 12f, width.toFloat(), height - 8f, sunGoldPaint)
    canvas.drawRect(0f, height - 8f, width.toFloat(), height.toFloat(), forestGreenPaint)

    return bitmap
}

private fun drawSunEmblemToCanvas(
    canvas: Canvas,
    cx: Float,
    cy: Float,
    radius: Float,
    paint: Paint
) {
    val discRadius = radius * 0.45f
    canvas.drawCircle(cx, cy, discRadius, paint)

    val rayPaint = Paint(paint).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        strokeCap = Paint.Cap.ROUND
    }
    val raysCount = 24
    val inner = radius * 0.55f
    val outer = radius
    for (i in 0 until raysCount) {
        val angle = (i * (360.0 / raysCount)) * (Math.PI / 180.0)
        val sx = cx + (inner * cos(angle)).toFloat()
        val sy = cy + (inner * sin(angle)).toFloat()
        val ex = cx + (outer * cos(angle)).toFloat()
        val ey = cy + (outer * sin(angle)).toFloat()
        canvas.drawLine(sx, sy, ex, ey, rayPaint)
    }
}

/**
 * Downloads or shares the generated broadcast report PNG image
 */
fun exportAndShareBroadcastImage(
    context: Context,
    match: MatchEntity,
    stats: CalculatedMatchStats,
    events: List<EventEntity>,
    isShareOnly: Boolean
) {
    try {
        val bitmap = generateBroadcastReportBitmap(context, match, stats, events)
        val fileName = "Rwanda_Match_Broadcast_${match.id}_${System.currentTimeMillis()}.png"

        // Save to cache for sharing
        val cacheFile = File(context.cacheDir, fileName)
        val out = FileOutputStream(cacheFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        out.flush()
        out.close()

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            cacheFile
        )

        if (isShareOnly) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Rwanda Football Scout Match Report: ${match.homeTeam} ${match.homeScore} - ${match.awayScore} ${match.awayTeam}"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Match Broadcast Report"))
        } else {
            // Also attempt to save to MediaStore (Pictures/RwandaScout) for permanent storage on device
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/RwandaScout")
                }
                val resolver = context.contentResolver
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    resolver.openOutputStream(imageUri)?.use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    }
                }
            }

            // Launch view intent so user sees the saved image
            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "image/png")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            try {
                context.startActivity(Intent.createChooser(viewIntent, "Open Broadcast Report"))
                Toast.makeText(context, "Broadcast report card saved (${cacheFile.name})", Toast.LENGTH_LONG).show()
            } catch (_: Exception) {
                Toast.makeText(context, "Broadcast report card downloaded to app storage", Toast.LENGTH_LONG).show()
            }
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error saving broadcast report: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
