package com.cryotrac.paranormal.ui.screens

import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.cryotrac.paranormal.BuildConfig
import com.cryotrac.paranormal.R
import com.cryotrac.paranormal.ui.theme.*

@Composable
fun HomeScreen(onEnter: () -> Unit) {
    val context = LocalContext.current

    // Background music via ExoPlayer
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            val uri = Uri.parse("android.resource://${context.packageName}/${R.raw.the_midnight_perimeter}")
            setMediaItem(MediaItem.fromUri(uri))
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
            volume = 0.7f
            prepare()
            playWhenReady = true
        }
    }
    DisposableEffect(Unit) { onDispose { player.release() } }

    // Animations
    val infinite = rememberInfiniteTransition(label = "home")
    val logoAlpha by infinite.animateFloat(
        initialValue = 0.82f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "logoAlpha"
    )
    val borderAlpha by infinite.animateFloat(
        initialValue = 0.35f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "borderAlpha"
    )

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "GHOSTRAC",
                fontFamily    = FontFamily.Monospace,
                fontSize      = 36.sp,
                letterSpacing = 10.sp,
                color         = CryotracGreen,
                modifier      = Modifier.alpha(logoAlpha)
            )

            Spacer(Modifier.height(8.dp))

            Image(
                painter = painterResource(R.drawable.logofinal),
                contentDescription = "Ghostrac",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(240.dp).alpha(logoAlpha)
            )

            Spacer(Modifier.height(36.dp))

            Button(
                onClick = onEnter,
                modifier = Modifier
                    .height(52.dp)
                    .widthIn(min = 180.dp)
                    .border(1.dp, CryotracGreen.copy(alpha = borderAlpha), RoundedCornerShape(4.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor   = CryotracGreen
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "▶   ENTER",
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 22.sp,
                    letterSpacing = 6.sp,
                    color = CryotracGreen
                )
            }
        }

        // Website + Disclaimer stacked together
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp, start = 16.dp, end = 16.dp)
        ) {
            // Gemini art — yellow tint on dark background
            Box(
                modifier = Modifier
                    .background(Color.Black)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.gemini_art),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.height(144.dp)
                )
            }

            Text(
                text = "GHOSTRAC.COM",
                fontFamily    = FontFamily.Monospace,
                fontSize      = 11.sp,
                color         = CryotracDim,
                textAlign     = TextAlign.Center,
                letterSpacing = 3.sp
            )
            Text(
                text = "FOR ENTERTAINMENT PURPOSES ONLY\n" +
                       "EMF & TOUCH USE REAL DEVICE SENSORS\n" +
                       "WORD ENGINE IS ALGORITHMICALLY GENERATED",
                fontFamily    = FontFamily.Monospace,
                fontSize      = 10.sp,
                color         = CryotracDim,
                textAlign     = TextAlign.Center,
                letterSpacing = 1.sp,
                lineHeight    = 17.sp
            )
        }

        // ── Version stamp (bottom-right corner) ──────────────────────────────
        Text(
            text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            fontFamily    = FontFamily.Monospace,
            fontSize      = 10.sp,
            color         = CryotracDim,
            letterSpacing = 1.sp,
            modifier      = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 10.dp, end = 12.dp)
        )
    }
}
