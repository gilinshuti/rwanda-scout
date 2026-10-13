package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.data.model.BallTapEntity
import com.example.ui.theme.*

@Composable
fun PitchView(
    modifier: Modifier = Modifier,
    ballTaps: List<BallTapEntity> = emptyList(),
    onPitchTap: ((x: Float, y: Float) -> Unit)? = null,
    isReadOnly: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.5f) // Realistic pitch ratio (length : width)
            .clip(RoundedCornerShape(16.dp))
            .background(ScoutPitchGrass)
            .padding(12.dp)
            .semantics {
                contentDescription = if (isReadOnly) {
                    "Tactical pitch view with ${ballTaps.size} recorded ball taps"
                } else {
                    "Interactive football pitch. Tap anywhere to record ball touch location."
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (!isReadOnly && onPitchTap != null) {
                        Modifier.pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val normalizedX = (offset.x / size.width).coerceIn(0f, 1f)
                                val normalizedY = (offset.y / size.height).coerceIn(0f, 1f)
                                onPitchTap(normalizedX, normalizedY)
                            }
                        }
                    } else Modifier
                )
        ) {
            val w = size.width
            val h = size.height
            val strokeW = 2.dp.toPx()
            val lineColor = ScoutPitchLines

            // 1. Alternating turf lawn mower stripes (10 vertical bands)
            val bandsCount = 10
            val bandWidth = w / bandsCount
            for (i in 0 until bandsCount) {
                val bandColor = if (i % 2 == 0) ScoutPitchGrass else ScoutPitchGrassStripe
                drawRect(
                    color = bandColor,
                    topLeft = Offset(i * bandWidth, 0f),
                    size = Size(bandWidth, h)
                )
            }

            // Outer boundary
            drawRect(
                color = lineColor,
                topLeft = Offset(0f, 0f),
                size = Size(w, h),
                style = Stroke(width = strokeW)
            )

            // Halfway line
            drawLine(
                color = lineColor,
                start = Offset(w / 2f, 0f),
                end = Offset(w / 2f, h),
                strokeWidth = strokeW
            )

            // Center circle
            val centerCircleRadius = h * 0.22f
            drawCircle(
                color = lineColor,
                radius = centerCircleRadius,
                center = Offset(w / 2f, h / 2f),
                style = Stroke(width = strokeW)
            )

            // Center spot
            drawCircle(
                color = lineColor,
                radius = 3.5.dp.toPx(),
                center = Offset(w / 2f, h / 2f)
            )

            // Left Penalty Box (18 yards)
            val boxW = w * 0.17f
            val boxH = h * 0.62f
            val boxTop = (h - boxH) / 2f
            drawRect(
                color = lineColor,
                topLeft = Offset(0f, boxTop),
                size = Size(boxW, boxH),
                style = Stroke(width = strokeW)
            )

            // Left Goal Box (6 yards)
            val goalBoxW = w * 0.065f
            val goalBoxH = h * 0.32f
            val goalBoxTop = (h - goalBoxH) / 2f
            drawRect(
                color = lineColor,
                topLeft = Offset(0f, goalBoxTop),
                size = Size(goalBoxW, goalBoxH),
                style = Stroke(width = strokeW)
            )

            // Left Penalty Spot (11m mark)
            val penSpotXLeft = w * 0.115f
            drawCircle(
                color = lineColor,
                radius = 3.dp.toPx(),
                center = Offset(penSpotXLeft, h / 2f)
            )

            // Left Penalty Arc ("D" arc outside 18-yard box)
            val penArcRadius = centerCircleRadius
            drawArc(
                color = lineColor,
                startAngle = -53f,
                sweepAngle = 106f,
                useCenter = false,
                topLeft = Offset(penSpotXLeft - penArcRadius, (h / 2f) - penArcRadius),
                size = Size(penArcRadius * 2, penArcRadius * 2),
                style = Stroke(width = strokeW)
            )

            // Right Penalty Box (18 yards)
            drawRect(
                color = lineColor,
                topLeft = Offset(w - boxW, boxTop),
                size = Size(boxW, boxH),
                style = Stroke(width = strokeW)
            )

            // Right Goal Box (6 yards)
            drawRect(
                color = lineColor,
                topLeft = Offset(w - goalBoxW, goalBoxTop),
                size = Size(goalBoxW, goalBoxH),
                style = Stroke(width = strokeW)
            )

            // Right Penalty Spot
            val penSpotXRight = w - penSpotXLeft
            drawCircle(
                color = lineColor,
                radius = 3.dp.toPx(),
                center = Offset(penSpotXRight, h / 2f)
            )

            // Right Penalty Arc ("D" arc outside 18-yard box)
            drawArc(
                color = lineColor,
                startAngle = 127f,
                sweepAngle = 106f,
                useCenter = false,
                topLeft = Offset(penSpotXRight - penArcRadius, (h / 2f) - penArcRadius),
                size = Size(penArcRadius * 2, penArcRadius * 2),
                style = Stroke(width = strokeW)
            )

            // Corner Arcs
            val arcR = 12.dp.toPx()
            // Top-left
            drawArc(
                color = lineColor,
                startAngle = 0f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(-arcR, -arcR),
                size = Size(arcR * 2, arcR * 2),
                style = Stroke(width = strokeW)
            )
            // Bottom-left
            drawArc(
                color = lineColor,
                startAngle = 270f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(-arcR, h - arcR),
                size = Size(arcR * 2, arcR * 2),
                style = Stroke(width = strokeW)
            )
            // Top-right
            drawArc(
                color = lineColor,
                startAngle = 90f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(w - arcR, -arcR),
                size = Size(arcR * 2, arcR * 2),
                style = Stroke(width = strokeW)
            )
            // Bottom-right
            drawArc(
                color = lineColor,
                startAngle = 180f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(w - arcR, h - arcR),
                size = Size(arcR * 2, arcR * 2),
                style = Stroke(width = strokeW)
            )

            // Realistic Goal Nets (left and right)
            val netDepth = w * 0.025f
            val goalWidth = goalBoxH * 0.6f
            val goalTop = (h - goalWidth) / 2f
            // Left goal net
            drawRect(
                color = Color.White.copy(alpha = 0.4f),
                topLeft = Offset(-netDepth, goalTop),
                size = Size(netDepth, goalWidth),
                style = Stroke(width = 1.dp.toPx())
            )
            // Right goal net
            drawRect(
                color = Color.White.copy(alpha = 0.4f),
                topLeft = Offset(w, goalTop),
                size = Size(netDepth, goalWidth),
                style = Stroke(width = 1.dp.toPx())
            )

            // Draw recorded Ball Taps
            ballTaps.forEach { tap ->
                val tapX = tap.x * w
                val tapY = tap.y * h

                if (tap.isDangerous) {
                    // Dangerous action: drawn as high-contrast red diamond / star with gold border
                    val diamondSize = 12.dp.toPx()
                    val path = Path().apply {
                        moveTo(tapX, tapY - diamondSize)
                        lineTo(tapX + diamondSize, tapY)
                        lineTo(tapX, tapY + diamondSize)
                        lineTo(tapX - diamondSize, tapY)
                        close()
                    }
                    drawPath(path, color = ScoutErrorDanger)
                    drawPath(path, color = ScoutReportAccents, style = Stroke(width = 2.dp.toPx()))
                } else {
                    // Regular tap: high-contrast gold circle with dark ring
                    drawCircle(
                        color = ScoutReportAccents,
                        radius = 8.dp.toPx(),
                        center = Offset(tapX, tapY)
                    )
                    drawCircle(
                        color = Color.Black,
                        radius = 8.dp.toPx(),
                        center = Offset(tapX, tapY),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }
    }
}
