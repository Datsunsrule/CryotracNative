package com.cryotrac.paranormal.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// VT323 will be added as a bundled font asset in a follow-up.
// Using system monospace as a placeholder — same visual rhythm.
val CryotracFont = FontFamily.Monospace

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = CryotracFont,
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = CryotracFont,
        fontSize   = 14.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = CryotracFont,
        fontSize   = 12.sp,
    ),
)
