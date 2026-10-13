package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

enum class ScoutButtonStyle {
    PRIMARY,
    SECONDARY,
    DANGER,
    SUCCESS
}

@Composable
fun ScoutButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ScoutButtonStyle = ScoutButtonStyle.PRIMARY,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = 48.dp,
    testTag: String = "scout_button"
) {
    val buttonModifier = modifier
        .fillMaxWidth()
        .height(height)
        .testTag(testTag)
        .semantics { contentDescription = text }

    val cornerShape = RoundedCornerShape(10.dp)

    when (style) {
        ScoutButtonStyle.PRIMARY -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled,
                shape = cornerShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ScoutPrimaryAction,
                    contentColor = Color.White,
                    disabledContainerColor = ScoutDivider,
                    disabledContentColor = ScoutTextHint
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp, pressedElevation = 0.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                ButtonContent(icon = icon, text = text, textColor = Color.White)
            }
        }
        ScoutButtonStyle.SECONDARY -> {
            OutlinedButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled,
                shape = cornerShape,
                border = BorderStroke(1.5.dp, if (enabled) ScoutPrimaryAction else ScoutDivider),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = ScoutSurface,
                    contentColor = ScoutPrimaryAction,
                    disabledContentColor = ScoutTextHint
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                ButtonContent(icon = icon, text = text, textColor = if (enabled) ScoutPrimaryAction else ScoutTextHint)
            }
        }
        ScoutButtonStyle.DANGER -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled,
                shape = cornerShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ScoutErrorDanger,
                    contentColor = Color.White,
                    disabledContainerColor = ScoutDivider,
                    disabledContentColor = ScoutTextHint
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp, pressedElevation = 0.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                ButtonContent(icon = icon, text = text, textColor = Color.White)
            }
        }
        ScoutButtonStyle.SUCCESS -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled,
                shape = cornerShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ScoutSuccessLive,
                    contentColor = Color.White,
                    disabledContainerColor = ScoutDivider,
                    disabledContentColor = ScoutTextHint
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp, pressedElevation = 0.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                ButtonContent(icon = icon, text = text, textColor = Color.White)
            }
        }
    }
}

@Composable
private fun ButtonContent(
    icon: ImageVector?,
    text: String,
    textColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = textColor
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = ScoutTypographyLabelL.copy(
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1
        )
    }
}

@Composable
fun ScoutStatusPill(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, iconVector, statusLabel) = when (status.uppercase()) {
        "LIVE" -> Triple(ScoutSuccessLive, Icons.Default.FiberManualRecord, "● LIVE")
        "FINISHED" -> Triple(ScoutPrimaryAction, Icons.Default.Check, "✓ FINISHED")
        else -> Triple(ScoutTextSecondary, Icons.Default.CalendarToday, "SCHEDULED")
    }

    Surface(
        modifier = modifier
            .semantics { contentDescription = "Status: $statusLabel" },
        shape = CircleShape,
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = status.uppercase(),
                style = ScoutTypographyLabelM.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}

@Composable
fun ScoutTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    testTag: String = "scout_text_field"
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = ScoutTypographyLabelM.copy(
                color = ScoutTextSecondary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            ),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
            placeholder = {
                if (placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        style = ScoutTypographyBodyM.copy(color = ScoutTextHint)
                    )
                }
            },
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = ScoutPrimaryAction,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            textStyle = ScoutTypographyBodyL.copy(fontSize = 14.5.sp),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ScoutPrimaryAction,
                unfocusedBorderColor = ScoutDivider,
                focusedContainerColor = ScoutSurface,
                unfocusedContainerColor = ScoutSurface
            ),
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )
    }
}

@Composable
fun ScoutCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    } else {
        modifier.fillMaxWidth()
    }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ScoutSurface),
        border = BorderStroke(1.dp, ScoutDivider),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun ScoutSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = ScoutTypographyTitleL,
            color = ScoutTextPrimary
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = ScoutTypographyCaption,
                color = ScoutTextSecondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoutTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    style = ScoutTypographyTitleL.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = ScoutTextPrimary,
                    maxLines = 1
                )
            },
            navigationIcon = {
                if (onBackClick != null) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                            tint = ScoutPrimaryAction,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            },
            actions = { actions() },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = ScoutBackground,
                titleContentColor = ScoutTextPrimary
            )
        )
        HorizontalDivider(color = ScoutDivider.copy(alpha = 0.6f), thickness = 1.dp)
    }
}

// Color-blind safe event badge
@Composable
fun ScoutEventBadge(
    eventType: String,
    modifier: Modifier = Modifier
) {
    val (bg, fg, label, icon) = when (eventType.uppercase()) {
        "GOAL" -> Quadruple(ScoutSuccessLive, Color.White, "GOAL", Icons.Default.SportsSoccer)
        "YELLOW_CARD" -> Quadruple(ScoutWarning, Color.White, "YELLOW", Icons.Default.Warning)
        "RED_CARD" -> Quadruple(ScoutErrorDanger, Color.White, "RED", Icons.Default.Warning)
        "SUBSTITUTION" -> Quadruple(ScoutInfo, Color.White, "SUB", Icons.Default.Check)
        "FOUL" -> Quadruple(ScoutTextSecondary, Color.White, "FOUL", Icons.Default.Warning)
        "CORNER" -> Quadruple(ScoutPrimaryAction, Color.White, "CORNER", Icons.Default.SportsSoccer)
        else -> Quadruple(ScoutTextSecondary, Color.White, eventType.replace("_", " "), Icons.Default.FiberManualRecord)
    }

    Row(
        modifier = modifier
            .background(bg, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .semantics { contentDescription = "Event: $label" },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = ScoutTypographyLabelM.copy(
                color = fg,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        )
    }
}

/**
 * Realistic Football Jersey / Kit Badge with collar, sleeves, and optional number.
 */
@Composable
fun TacticalJerseyBadge(
    color: Color,
    modifier: Modifier = Modifier.size(28.dp),
    number: Int? = null,
    accentColor: Color = Color.White
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            // Main shirt torso
            val bodyPath = Path().apply {
                moveTo(w * 0.28f, h * 0.18f)
                // Left shoulder
                lineTo(w * 0.08f, h * 0.32f)
                // Left sleeve bottom
                lineTo(w * 0.18f, h * 0.46f)
                // Left armpit
                lineTo(w * 0.28f, h * 0.38f)
                // Left side
                lineTo(w * 0.28f, h * 0.88f)
                // Bottom hem
                lineTo(w * 0.72f, h * 0.88f)
                // Right side
                lineTo(w * 0.72f, h * 0.38f)
                // Right armpit
                lineTo(w * 0.82f, h * 0.46f)
                // Right sleeve bottom
                lineTo(w * 0.92f, h * 0.32f)
                // Right shoulder
                lineTo(w * 0.72f, h * 0.18f)
                // Collar cut
                quadraticTo(w * 0.5f, h * 0.32f, w * 0.28f, h * 0.18f)
                close()
            }
            drawPath(bodyPath, color = color)
            drawPath(bodyPath, color = accentColor.copy(alpha = 0.5f), style = Stroke(width = 1.2.dp.toPx()))

            // V-neck collar detail
            val collarPath = Path().apply {
                moveTo(w * 0.38f, h * 0.18f)
                lineTo(w * 0.5f, h * 0.30f)
                lineTo(w * 0.62f, h * 0.18f)
            }
            drawPath(collarPath, color = accentColor, style = Stroke(width = 1.5.dp.toPx()))
        }

        if (number != null) {
            Text(
                text = number.toString(),
                style = ScoutTypographyCaption.copy(
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Realistic Vector Soccer Ball Icon with pentagons and seams.
 */
@Composable
fun SoccerBallBadge(
    modifier: Modifier = Modifier.size(24.dp),
    ballColor: Color = Color.White,
    patternColor: Color = Color(0xFF1E293B)
) {
    Canvas(modifier = modifier) {
        val r = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Ball base
        drawCircle(color = ballColor, radius = r, center = center)
        drawCircle(color = patternColor, radius = r, center = center, style = Stroke(width = 1.5.dp.toPx()))

        // Center pentagon
        val pentR = r * 0.36f
        val pentPath = Path()
        for (i in 0 until 5) {
            val angle = Math.toRadians((i * 72 - 90).toDouble())
            val px = center.x + (pentR * Math.cos(angle)).toFloat()
            val py = center.y + (pentR * Math.sin(angle)).toFloat()
            if (i == 0) pentPath.moveTo(px, py) else pentPath.lineTo(px, py)
        }
        pentPath.close()
        drawPath(pentPath, color = patternColor)

        // Seams extending outwards to edges
        for (i in 0 until 5) {
            val angle = Math.toRadians((i * 72 - 90).toDouble())
            val p1x = center.x + (pentR * Math.cos(angle)).toFloat()
            val p1y = center.y + (pentR * Math.sin(angle)).toFloat()
            val p2x = center.x + (r * Math.cos(angle)).toFloat()
            val p2y = center.y + (r * Math.sin(angle)).toFloat()
            drawLine(
                color = patternColor,
                start = Offset(p1x, p1y),
                end = Offset(p2x, p2y),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
