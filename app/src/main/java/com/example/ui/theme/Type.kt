package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Professional Sports Scouting Typography System
val ScoutTypographyDisplay = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 34.sp,
    color = ScoutTextPrimary
)

val ScoutTypographyTitleXL = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    color = ScoutTextPrimary
)

val ScoutTypographyTitleL = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = 19.sp,
    lineHeight = 24.sp,
    color = ScoutTextPrimary
)

val ScoutTypographyTitleM = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    lineHeight = 22.sp,
    color = ScoutTextPrimary
)

val ScoutTypographyBodyL = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.2.sp,
    color = ScoutTextPrimary
)

val ScoutTypographyBodyM = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 13.5.sp,
    lineHeight = 19.sp,
    letterSpacing = 0.15.sp,
    color = ScoutTextPrimary
)

val ScoutTypographyLabelL = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.5.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp,
    color = ScoutTextPrimary
)

val ScoutTypographyLabelM = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.1.sp,
    color = ScoutTextPrimary
)

val ScoutTypographyCaption = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 11.5.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.2.sp,
    color = ScoutTextSecondary
)

val Typography = Typography(
    displayLarge = ScoutTypographyDisplay,
    displayMedium = ScoutTypographyDisplay,
    displaySmall = ScoutTypographyTitleXL,
    headlineLarge = ScoutTypographyTitleXL,
    headlineMedium = ScoutTypographyTitleL,
    headlineSmall = ScoutTypographyTitleM,
    titleLarge = ScoutTypographyTitleM,
    titleMedium = ScoutTypographyLabelL,
    titleSmall = ScoutTypographyLabelM,
    bodyLarge = ScoutTypographyBodyL,
    bodyMedium = ScoutTypographyBodyM,
    bodySmall = ScoutTypographyCaption,
    labelLarge = ScoutTypographyLabelL,
    labelMedium = ScoutTypographyLabelM,
    labelSmall = ScoutTypographyCaption
)
