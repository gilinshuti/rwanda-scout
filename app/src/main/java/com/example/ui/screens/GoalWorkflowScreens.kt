package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Goal Details Confirmation Screen — Exact Sportradar Scout UI matching IMG_8742.jpeg
 */
@Composable
fun GoalDetailsScreen(
    homeTeamName: String,
    awayTeamName: String,
    selectedTeam: String,
    currentHomeScore: Int,
    currentAwayScore: Int,
    selectedHowScored: String,
    onSelectHowScored: (String) -> Unit,
    isDoubtfulMinute: Boolean,
    onToggleDoubtfulMinute: (Boolean) -> Unit,
    isDoubtfulScorer: Boolean,
    onToggleDoubtfulScorer: (Boolean) -> Unit,
    isInsideBox: Boolean,
    onToggleInsideBox: (Boolean) -> Unit,
    isDoubtfulGoal: Boolean,
    onToggleDoubtfulGoal: (Boolean) -> Unit,
    onCancelGoal: () -> Unit,
    onVideoReferee: () -> Unit,
    onConfirmGoal: () -> Unit
) {
    var countdownSeconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        if (countdownSeconds > 0) {
            while (countdownSeconds > 0) {
                delay(1000L)
                countdownSeconds--
            }
        }
    }

    val futureHomeScore = if (selectedTeam.equals("HOME", ignoreCase = true)) currentHomeScore + 1 else currentHomeScore
    val futureAwayScore = if (selectedTeam.equals("AWAY", ignoreCase = true)) currentAwayScore + 1 else currentAwayScore
    val scoringTeamName = if (selectedTeam.equals("HOME", ignoreCase = true)) homeTeamName else awayTeamName

    // Header gradient matches team (Blue for Away as in IMG_8742, Red for Home)
    val headerGradient = if (selectedTeam.equals("AWAY", ignoreCase = true)) {
        Brush.verticalGradient(listOf(Color(0xFF1E5BB0), Color(0xFF0D3470)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFB01E1E), Color(0xFF6B0E0E)))
    }

    val howScoredOptions = listOf(
        "Shot on goal",
        "Penalty kick",
        "Header",
        "Free kick",
        "Own goal"
    )

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("goal_details_screen"),
        color = Color(0xFF000000)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ═══════════════════════════════════════════════════════════════════════
            // 1. TOP HEADER BAR (Exact IMG_8742 design)
            // ═══════════════════════════════════════════════════════════════════════
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(headerGradient)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                // Left: Team name pill
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.35f),
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Text(
                        text = scoringTeamName.uppercase(),
                        style = ScoutTypographyLabelM.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        maxLines = 1
                    )
                }

                // Center: "GOAL"
                Text(
                    text = "GOAL",
                    style = ScoutTypographyLabelL.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )

                // Right: Headset comms pill & soccer ball icon
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RefereeCommsBadge()
                    Spacer(modifier = Modifier.width(10.dp))
                    GoalSoccerBallIcon(modifier = Modifier.size(20.dp))
                }
            }

            // ═══════════════════════════════════════════════════════════════════════
            // 2. MAIN CONTENT (Centered Titles, 5 Action Tiles, 4 Checkboxes)
            // ═══════════════════════════════════════════════════════════════════════
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Centered Score Announcement
                Text(
                    text = "Score will be $futureHomeScore:$futureAwayScore",
                    style = ScoutTypographyDisplay.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.testTag("future_score_heading"),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "How was the goal scored?",
                    style = ScoutTypographyTitleM.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Center area: Tiles on left, Checkboxes on right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: 5 Action Tiles Row
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        howScoredOptions.forEach { optionName ->
                            val isSelected = selectedHowScored.equals(optionName, ignoreCase = true)
                            GoalHowScoredTile(
                                name = optionName,
                                isSelected = isSelected,
                                onClick = { onSelectHowScored(optionName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Right: 4 Checkboxes Column
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        listOf(
                            Triple("Doubtful minute", isDoubtfulMinute, onToggleDoubtfulMinute),
                            Triple("Doubtful scorer", isDoubtfulScorer, onToggleDoubtfulScorer),
                            Triple("Inside the box", isInsideBox, onToggleInsideBox),
                            Triple("Doubtful goal", isDoubtfulGoal, onToggleDoubtfulGoal)
                        ).forEach { (label, checked, onToggle) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { onToggle(!checked) }
                                    .padding(vertical = 2.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(22.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (checked) Color(0xFF3B82F6) else Color(0xFF262626),
                                    border = BorderStroke(1.5.dp, if (checked) Color(0xFF60A5FA) else Color(0xFF555555))
                                ) {
                                    if (checked) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = label,
                                    style = ScoutTypographyBodyM.copy(
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // ═══════════════════════════════════════════════════════════════════════
            // 3. BOTTOM ACTION BAR (Exact IMG_8742 design)
            // ═══════════════════════════════════════════════════════════════════════
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                color = Color(0xFF1E1E1E),
                border = BorderStroke(0.5.dp, Color(0xFF333333))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: < CANCEL GOAL
                    Surface(
                        onClick = onCancelGoal,
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("cancel_goal_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "‹  CANCEL GOAL",
                                style = ScoutTypographyLabelL.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    // Center: VIDEO REFEREE button with red whistle badge
                    Surface(
                        onClick = onVideoReferee,
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("video_referee_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            // Red outlined badge with whistle/screen icon
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF3B1212),
                                border = BorderStroke(1.5.dp, Color(0xFFEF4444)),
                                modifier = Modifier.size(width = 38.dp, height = 28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    GoalWhistleRaysIcon(
                                        modifier = Modifier.size(18.dp),
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = "VIDEO REFEREE",
                                style = ScoutTypographyLabelL.copy(
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    // Right: CONFIRM GOAL >
                    Surface(
                        onClick = onConfirmGoal,
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("confirm_goal_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "CONFIRM GOAL  ›",
                                style = ScoutTypographyLabelL.copy(
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Single square Action Tile for "How was the goal scored?"
 */
@Composable
private fun GoalHowScoredTile(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier
                .size(72.dp)
                .border(
                    border = if (isSelected) BorderStroke(2.5.dp, Color.White) else BorderStroke(1.dp, Color(0xFF383838)),
                    shape = RoundedCornerShape(12.dp)
                ),
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) Color(0xFF383838) else Color(0xFF222222),
            shadowElevation = if (isSelected) 3.dp else 0.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when (name) {
                    "Shot on goal" -> GoalShotOnGoalIcon(modifier = Modifier.size(36.dp))
                    "Penalty kick" -> GoalPenaltyKickIcon(modifier = Modifier.size(36.dp))
                    "Header" -> GoalHeaderIcon(modifier = Modifier.size(36.dp))
                    "Free kick" -> GoalFreeKickWallIcon(modifier = Modifier.size(36.dp))
                    "Own goal" -> GoalOwnGoalIcon(modifier = Modifier.size(36.dp))
                    else -> GoalSoccerBallIcon(modifier = Modifier.size(32.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = name,
            style = ScoutTypographyCaption.copy(
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = Color.White
            ),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Goal Cancellation Reason Screen — Exact Sportradar Scout UI matching IMG_8743.jpeg
 */
@Composable
fun GoalCancelReasonsScreen(
    teamName: String = "Team",
    onSelectReason: (String) -> Unit,
    onBack: () -> Unit
) {
    var selectedReason by remember { mutableStateOf("Offside") }

    val redHeaderGradient = Brush.verticalGradient(
        listOf(Color(0xFFB91C1C), Color(0xFF7F1D1D))
    )

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("cancel_goal_reasons_screen"),
        color = Color(0xFF000000)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ═══════════════════════════════════════════════════════════════════════
            // 1. TOP HEADER BAR (Exact IMG_8743 design: Red gradient with Team Name & Goal Icon)
            // ═══════════════════════════════════════════════════════════════════════
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(redHeaderGradient)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                // Left: Team name
                Text(
                    text = teamName.uppercase(),
                    style = ScoutTypographyLabelM.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                // Right: "GOAL" + Soccer ball
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GOAL",
                        style = ScoutTypographyLabelM.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    GoalSoccerBallIcon(modifier = Modifier.size(20.dp))
                }
            }

            // ═══════════════════════════════════════════════════════════════════════
            // 2. MAIN CONTENT (Exact 3 Action Tiles: Offside, Offensive foul, Wrong entry)
            // ═══════════════════════════════════════════════════════════════════════
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Centered Title
                Text(
                    text = "Reason for cancelling?",
                    style = ScoutTypographyDisplay.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Row of 3 square action tiles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val reasons = listOf(
                        "Offside",
                        "Offensive foul",
                        "Wrong entry"
                    )

                    reasons.forEachIndexed { index, reason ->
                        val isSelected = selectedReason == reason
                        val testTag = "cancel_reason_${reason.replace(" ", "_")}"

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .testTag(testTag)
                        ) {
                            Surface(
                                onClick = { selectedReason = reason },
                                modifier = Modifier
                                    .size(80.dp)
                                    .border(
                                        border = if (isSelected) BorderStroke(2.5.dp, Color.White) else BorderStroke(1.dp, Color(0xFF383838)),
                                        shape = RoundedCornerShape(14.dp)
                                    ),
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) Color(0xFF383838) else Color(0xFF222222),
                                shadowElevation = if (isSelected) 3.dp else 0.dp
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when (reason) {
                                        "Offside" -> GoalOffsideFlagIcon(modifier = Modifier.size(42.dp))
                                        "Offensive foul" -> GoalOffensiveFoulIcon(modifier = Modifier.size(42.dp))
                                        "Wrong entry" -> GoalWrongEntryIcon(modifier = Modifier.size(40.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = reason,
                                style = ScoutTypographyCaption.copy(
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = Color.White
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // ═══════════════════════════════════════════════════════════════════════
            // 3. BOTTOM ACTION BAR (Exact IMG_8743 design: CONFIRM > button)
            // ═══════════════════════════════════════════════════════════════════════
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                color = Color(0xFF1E1E1E),
                border = BorderStroke(0.5.dp, Color(0xFF333333))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Back button
                    Surface(
                        onClick = onBack,
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Transparent,
                        modifier = Modifier.height(44.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "‹  BACK",
                                style = ScoutTypographyLabelL.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    // Right: CONFIRM >
                    Surface(
                        onClick = { onSelectReason(selectedReason) },
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Transparent,
                        modifier = Modifier.height(44.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "CONFIRM  ›",
                                style = ScoutTypographyLabelL.copy(
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 4. CUSTOM CANVAS ICONS (Matching Exact Sportradar Vector Specifications)
// ═════════════════════════════════════════════════════════════════════════════

/**
 * 1. Shot on goal Icon: Goal frame with crossbar, posts and ball inside
 */
@Composable
fun GoalShotOnGoalIcon(modifier: Modifier = Modifier, color: Color = Color.White) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Goal frame (posts and crossbar)
        val strokeW = 2.2.dp.toPx()
        val postLeft = w * 0.16f
        val postRight = w * 0.84f
        val crossbarY = h * 0.20f
        val groundY = h * 0.84f

        // Crossbar
        drawLine(
            color = color,
            start = Offset(postLeft, crossbarY),
            end = Offset(postRight, crossbarY),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
        // Left post
        drawLine(
            color = color,
            start = Offset(postLeft, crossbarY),
            end = Offset(postLeft, groundY),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
        // Right post
        drawLine(
            color = color,
            start = Offset(postRight, crossbarY),
            end = Offset(postRight, groundY),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )

        // Subtle net lines
        val netColor = color.copy(alpha = 0.35f)
        val netStroke = 1.dp.toPx()
        drawLine(color = netColor, start = Offset(w * 0.38f, crossbarY), end = Offset(w * 0.38f, groundY), strokeWidth = netStroke)
        drawLine(color = netColor, start = Offset(w * 0.62f, crossbarY), end = Offset(w * 0.62f, groundY), strokeWidth = netStroke)
        drawLine(color = netColor, start = Offset(postLeft, h * 0.42f), end = Offset(postRight, h * 0.42f), strokeWidth = netStroke)
        drawLine(color = netColor, start = Offset(postLeft, h * 0.64f), end = Offset(postRight, h * 0.64f), strokeWidth = netStroke)

        // Soccer ball in the lower portion of the goal
        val ballCenter = Offset(w * 0.45f, h * 0.65f)
        val ballR = 6.dp.toPx()
        drawCircle(color = Color.White, radius = ballR, center = ballCenter)
        drawCircle(color = Color.Black, radius = ballR, center = ballCenter, style = Stroke(width = 1.2.dp.toPx()))
        drawCircle(color = Color.Black, radius = 2.dp.toPx(), center = ballCenter)
    }
}

/**
 * 2. Penalty kick Icon: Whistle with angled sound/action rays emitting from top spout
 */
@Composable
fun GoalPenaltyKickIcon(modifier: Modifier = Modifier, color: Color = Color.White) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 3 Blast Rays above whistle
        val rayW = 2.dp.toPx()
        // Left ray
        drawLine(
            color = color,
            start = Offset(w * 0.35f, h * 0.26f),
            end = Offset(w * 0.22f, h * 0.12f),
            strokeWidth = rayW,
            cap = StrokeCap.Round
        )
        // Center ray
        drawLine(
            color = color,
            start = Offset(w * 0.48f, h * 0.22f),
            end = Offset(w * 0.48f, h * 0.08f),
            strokeWidth = rayW,
            cap = StrokeCap.Round
        )
        // Right ray
        drawLine(
            color = color,
            start = Offset(w * 0.61f, h * 0.26f),
            end = Offset(w * 0.74f, h * 0.12f),
            strokeWidth = rayW,
            cap = StrokeCap.Round
        )

        // Whistle path: Spout cylinder on left, bulb chamber on right
        val whistlePath = Path().apply {
            moveTo(w * 0.18f, h * 0.44f)
            lineTo(w * 0.46f, h * 0.44f)
            cubicTo(w * 0.50f, h * 0.36f, w * 0.68f, h * 0.36f, w * 0.74f, h * 0.48f)
            cubicTo(w * 0.86f, h * 0.54f, w * 0.86f, h * 0.78f, w * 0.70f, h * 0.84f)
            cubicTo(w * 0.56f, h * 0.86f, w * 0.46f, h * 0.74f, w * 0.46f, h * 0.64f)
            lineTo(w * 0.18f, h * 0.64f)
            close()
        }
        drawPath(whistlePath, color = color)
    }
}

/**
 * 3. Header Icon: Player silhouette / head contacting a soccer ball
 */
@Composable
fun GoalHeaderIcon(modifier: Modifier = Modifier, color: Color = Color.White) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Player Head
        drawCircle(
            color = color,
            radius = 6.dp.toPx(),
            center = Offset(w * 0.38f, h * 0.55f)
        )

        // Player Torso/Shoulders angled
        val bodyPath = Path().apply {
            moveTo(w * 0.38f, h * 0.66f)
            lineTo(w * 0.54f, h * 0.88f)
            lineTo(w * 0.18f, h * 0.88f)
            close()
        }
        drawPath(bodyPath, color = color)

        // Soccer ball positioned at forehead contact point
        val ballCenter = Offset(w * 0.66f, h * 0.32f)
        val ballR = 6.5.dp.toPx()
        drawCircle(color = Color.White, radius = ballR, center = ballCenter)
        drawCircle(color = Color.Black, radius = ballR, center = ballCenter, style = Stroke(width = 1.dp.toPx()))
        drawCircle(color = Color.Black, radius = 2.dp.toPx(), center = ballCenter)

        // Contact impulse ticks
        val tickStroke = 1.2.dp.toPx()
        drawLine(color = color, start = Offset(w * 0.48f, h * 0.40f), end = Offset(w * 0.52f, h * 0.44f), strokeWidth = tickStroke, cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(w * 0.52f, h * 0.34f), end = Offset(w * 0.58f, h * 0.38f), strokeWidth = tickStroke, cap = StrokeCap.Round)
    }
}

/**
 * 4. Free kick Icon: Defensive wall of 3 player silhouettes with a soccer ball
 */
@Composable
fun GoalFreeKickWallIcon(modifier: Modifier = Modifier, color: Color = Color.White) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 3 Player Heads in wall
        val rHead = 4.dp.toPx()
        drawCircle(color = color, radius = rHead, center = Offset(w * 0.32f, h * 0.30f))
        drawCircle(color = color, radius = rHead, center = Offset(w * 0.54f, h * 0.26f))
        drawCircle(color = color, radius = rHead, center = Offset(w * 0.76f, h * 0.30f))

        // 3 Bodies huddled together in wall
        val wallPath = Path().apply {
            moveTo(w * 0.22f, h * 0.78f)
            lineTo(w * 0.22f, h * 0.42f)
            lineTo(w * 0.42f, h * 0.40f)
            lineTo(w * 0.54f, h * 0.36f)
            lineTo(w * 0.66f, h * 0.40f)
            lineTo(w * 0.86f, h * 0.42f)
            lineTo(w * 0.86f, h * 0.78f)
            close()
        }
        drawPath(wallPath, color = color)

        // Arms crossed divider lines
        drawLine(color = Color.Black, start = Offset(w * 0.43f, h * 0.40f), end = Offset(w * 0.43f, h * 0.78f), strokeWidth = 1.dp.toPx())
        drawLine(color = Color.Black, start = Offset(w * 0.65f, h * 0.40f), end = Offset(w * 0.65f, h * 0.78f), strokeWidth = 1.dp.toPx())

        // Soccer ball in front
        val ballCenter = Offset(w * 0.22f, h * 0.80f)
        val ballR = 4.5.dp.toPx()
        drawCircle(color = Color.White, radius = ballR, center = ballCenter)
        drawCircle(color = Color.Black, radius = ballR, center = ballCenter, style = Stroke(width = 1.dp.toPx()))
    }
}

/**
 * 5. Own goal Icon: Red soccer ball (Exact IMG_8742 design)
 */
@Composable
fun GoalOwnGoalIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)
        val radius = 14.dp.toPx()

        // Red outer sphere
        val red = Color(0xFFD32F2F)
        drawCircle(color = red, radius = radius, center = center)

        // White soccer ball pentagon patches on red ball
        val centerPatch = Offset(center.x, center.y)
        drawCircle(color = Color.White, radius = radius * 0.28f, center = centerPatch)

        // Peripheral white pentagon spots
        val rSmall = radius * 0.22f
        drawCircle(color = Color.White, radius = rSmall, center = Offset(center.x - radius * 0.52f, center.y - radius * 0.40f))
        drawCircle(color = Color.White, radius = rSmall, center = Offset(center.x + radius * 0.52f, center.y - radius * 0.40f))
        drawCircle(color = Color.White, radius = rSmall, center = Offset(center.x - radius * 0.45f, center.y + radius * 0.45f))
        drawCircle(color = Color.White, radius = rSmall, center = Offset(center.x + radius * 0.45f, center.y + radius * 0.45f))

        // Subtle outer border
        drawCircle(color = Color.White.copy(alpha = 0.8f), radius = radius, center = center, style = Stroke(width = 1.5.dp.toPx()))
    }
}

/**
 * Offside Flag Icon (Exact IMG_8743 design: 4 checkered yellow/red quadrants on flagpole)
 */
@Composable
fun GoalOffsideFlagIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // White angled flagpole
        val poleStart = Offset(w * 0.32f, h * 0.86f)
        val poleEnd = Offset(w * 0.54f, h * 0.14f)
        drawLine(
            color = Color.White,
            start = poleStart,
            end = poleEnd,
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Flag body extending to the right of the pole
        val red = Color(0xFFD32F2F)
        val yellow = Color(0xFFFDD835)

        val flagW = w * 0.38f
        val flagH = h * 0.32f
        val flagTopLeftX = w * 0.48f
        val flagTopLeftY = h * 0.22f

        // Top-Left quadrant: Red
        drawRect(color = red, topLeft = Offset(flagTopLeftX, flagTopLeftY), size = Size(flagW / 2f, flagH / 2f))
        // Top-Right quadrant: Yellow
        drawRect(color = yellow, topLeft = Offset(flagTopLeftX + flagW / 2f, flagTopLeftY), size = Size(flagW / 2f, flagH / 2f))
        // Bottom-Left quadrant: Yellow
        drawRect(color = yellow, topLeft = Offset(flagTopLeftX, flagTopLeftY + flagH / 2f), size = Size(flagW / 2f, flagH / 2f))
        // Bottom-Right quadrant: Red
        drawRect(color = red, topLeft = Offset(flagTopLeftX + flagW / 2f, flagTopLeftY + flagH / 2f), size = Size(flagW / 2f, flagH / 2f))

        // Flag border
        drawRect(color = Color.White.copy(alpha = 0.6f), topLeft = Offset(flagTopLeftX, flagTopLeftY), size = Size(flagW, flagH), style = Stroke(width = 1.dp.toPx()))
    }
}

/**
 * Offensive Foul Icon (Exact IMG_8743 design: Red whistle with blast rays)
 */
@Composable
fun GoalOffensiveFoulIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val red = Color(0xFFD32F2F)

        // 3 White Rays above whistle
        val rayW = 2.dp.toPx()
        drawLine(color = Color.White, start = Offset(w * 0.35f, h * 0.28f), end = Offset(w * 0.22f, h * 0.12f), strokeWidth = rayW, cap = StrokeCap.Round)
        drawLine(color = Color.White, start = Offset(w * 0.48f, h * 0.24f), end = Offset(w * 0.48f, h * 0.08f), strokeWidth = rayW, cap = StrokeCap.Round)
        drawLine(color = Color.White, start = Offset(w * 0.61f, h * 0.28f), end = Offset(w * 0.74f, h * 0.12f), strokeWidth = rayW, cap = StrokeCap.Round)

        // Red whistle body
        val whistlePath = Path().apply {
            moveTo(w * 0.18f, h * 0.46f)
            lineTo(w * 0.48f, h * 0.46f)
            cubicTo(w * 0.52f, h * 0.38f, w * 0.68f, h * 0.38f, w * 0.74f, h * 0.50f)
            cubicTo(w * 0.86f, h * 0.56f, w * 0.86f, h * 0.80f, w * 0.70f, h * 0.84f)
            cubicTo(w * 0.56f, h * 0.86f, w * 0.46f, h * 0.74f, w * 0.46f, h * 0.64f)
            lineTo(w * 0.18f, h * 0.64f)
            close()
        }
        drawPath(whistlePath, color = red)
        drawPath(whistlePath, color = Color.White, style = Stroke(width = 1.2.dp.toPx()))
    }
}

/**
 * Wrong Entry Icon (Exact IMG_8743 design: Square box with 'X' cross drawn across it)
 */
@Composable
fun GoalWrongEntryIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Square box with rounded corners
        val boxMargin = w * 0.18f
        val boxSize = w * 0.64f
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(boxMargin, boxMargin),
            size = Size(boxSize, boxSize),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )

        // Bold 'X' crossing through box
        val strokeW = 3.dp.toPx()
        drawLine(
            color = Color.White,
            start = Offset(w * 0.26f, h * 0.26f),
            end = Offset(w * 0.74f, h * 0.74f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White,
            start = Offset(w * 0.74f, h * 0.26f),
            end = Offset(w * 0.26f, h * 0.74f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Classic Soccer Ball Icon
 */
@Composable
fun GoalSoccerBallIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)
        val r = (w.coerceAtMost(h) / 2f) - 1.dp.toPx()

        drawCircle(color = Color.White, radius = r, center = center)
        drawCircle(color = Color.Black, radius = r, center = center, style = Stroke(width = 1.2.dp.toPx()))
        drawCircle(color = Color.Black, radius = r * 0.32f, center = center)

        // Surrounding small patches
        val rSmall = r * 0.20f
        drawCircle(color = Color.Black, radius = rSmall, center = Offset(center.x - r * 0.55f, center.y - r * 0.40f))
        drawCircle(color = Color.Black, radius = rSmall, center = Offset(center.x + r * 0.55f, center.y - r * 0.40f))
        drawCircle(color = Color.Black, radius = rSmall, center = Offset(center.x - r * 0.45f, center.y + r * 0.45f))
        drawCircle(color = Color.Black, radius = rSmall, center = Offset(center.x + r * 0.45f, center.y + r * 0.45f))
    }
}

/**
 * Small Whistle Icon with 3 rays for the VIDEO REFEREE button
 */
@Composable
private fun GoalWhistleRaysIcon(modifier: Modifier = Modifier, color: Color = Color.White) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 3 rays
        drawLine(color = color, start = Offset(w * 0.35f, h * 0.28f), end = Offset(w * 0.22f, h * 0.12f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(w * 0.48f, h * 0.24f), end = Offset(w * 0.48f, h * 0.08f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(w * 0.61f, h * 0.28f), end = Offset(w * 0.74f, h * 0.12f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)

        // Whistle
        val whistlePath = Path().apply {
            moveTo(w * 0.20f, h * 0.46f)
            lineTo(w * 0.50f, h * 0.46f)
            cubicTo(w * 0.54f, h * 0.38f, w * 0.70f, h * 0.38f, w * 0.76f, h * 0.50f)
            cubicTo(w * 0.88f, h * 0.56f, w * 0.88f, h * 0.80f, w * 0.72f, h * 0.84f)
            cubicTo(w * 0.56f, h * 0.86f, w * 0.48f, h * 0.74f, w * 0.48f, h * 0.64f)
            lineTo(w * 0.20f, h * 0.64f)
            close()
        }
        drawPath(whistlePath, color = color)
    }
}

/**
 * White pill with red phone receiver badge (Operator Comms)
 */
@Composable
private fun RefereeCommsBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(width = 28.dp, height = 20.dp),
        shape = RoundedCornerShape(4.dp),
        color = Color.White
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(13.dp)) {
                val w = size.width
                val h = size.height
                val red = Color(0xFFC62828)

                // Phone receiver
                val phonePath = Path().apply {
                    moveTo(w * 0.25f, h * 0.70f)
                    cubicTo(w * 0.20f, h * 0.85f, w * 0.35f, h * 0.95f, w * 0.45f, h * 0.90f)
                    lineTo(w * 0.55f, h * 0.78f)
                    cubicTo(w * 0.45f, h * 0.60f, w * 0.55f, h * 0.45f, w * 0.68f, h * 0.40f)
                    lineTo(w * 0.80f, h * 0.50f)
                    cubicTo(w * 0.90f, h * 0.45f, w * 0.88f, h * 0.25f, w * 0.75f, h * 0.20f)
                    cubicTo(w * 0.40f, h * 0.25f, w * 0.20f, h * 0.50f, w * 0.25f, h * 0.70f)
                    close()
                }
                drawPath(phonePath, color = red)
            }
        }
    }
}
