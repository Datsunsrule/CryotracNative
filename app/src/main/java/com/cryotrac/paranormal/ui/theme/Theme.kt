package com.cryotrac.paranormal.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CryotracColorScheme = darkColorScheme(
    primary      = CryotracGreen,
    onPrimary    = CryotracBg,
    background   = CryotracBg,
    onBackground = CryotracGreen,
    surface      = CryotracBg,
    onSurface    = CryotracGreen,
    secondary    = CryotracMid,
    onSecondary  = CryotracBg,
)

@Composable
fun CryotracTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CryotracColorScheme,
        typography  = Typography,
        content     = content
    )
}
