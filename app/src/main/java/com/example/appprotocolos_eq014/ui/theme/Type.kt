package com.example.appprotocolos_eq014.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.unit.sp
import com.example.appprotocolos_eq014.R

private val IbmPlexSans = FontFamily(
    Font(R.font.ibm_plex_sans_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_sans_medium, FontWeight.Medium),
    Font(R.font.ibm_plex_sans_semibold, FontWeight.SemiBold),
    Font(R.font.ibm_plex_sans_bold, FontWeight.Bold)
)

private val TipografiaBase = Typography()

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = IbmPlexSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = IbmPlexSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = IbmPlexSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = IbmPlexSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = IbmPlexSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = IbmPlexSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp
    ),
    labelLarge = TextStyle(
        fontFamily = IbmPlexSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = IbmPlexSans,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = IbmPlexSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.sp
    ),
    displayLarge = TipografiaBase.displayLarge.copy(fontFamily = IbmPlexSans),
    displayMedium = TipografiaBase.displayMedium.copy(fontFamily = IbmPlexSans),
    displaySmall = TipografiaBase.displaySmall.copy(fontFamily = IbmPlexSans),
    headlineSmall = TipografiaBase.headlineSmall.copy(fontFamily = IbmPlexSans),
    titleSmall = TipografiaBase.titleSmall.copy(fontFamily = IbmPlexSans),
    bodySmall = TipografiaBase.bodySmall.copy(fontFamily = IbmPlexSans)
)
