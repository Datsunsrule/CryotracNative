package com.cryotrac.paranormal.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cryotrac.paranormal.ui.theme.*
import com.cryotrac.paranormal.viewmodel.CryotracViewModel
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun GhostecScreen(vm: CryotracViewModel) {
    val running     by vm.ghostecRunning.collectAsState()
    val word        by vm.ghostecWord.collectAsState()
    val progress    by vm.ghostecProgress.collectAsState()
    val counter     by vm.ghostecCounter.collectAsState()
    val question    by vm.currentQuestion.collectAsState()
    val recentWords by vm.recentWords.collectAsState()

    // ── Waveform bar state ────────────────────────────────────────────────────
    val barCount = 28
    val barHeights = remember { mutableStateListOf<Float>().also { list -> repeat(barCount) { list.add(0.08f) } } }

    LaunchedEffect(running) {
        while (running) {
            for (i in 0 until barCount) {
                val base = 0.15f + Random.nextFloat() * 0.75f
                // shape into a rough bell curve so centre bars are taller
                val curve = 1f - (((i - barCount / 2f) / (barCount / 2f)) * 0.5f).let { it * it }
                barHeights[i] = (base * curve).coerceIn(0.08f, 1f)
            }
            delay(110)
        }
        // decay to flat when stopped
        for (i in 0 until barCount) barHeights[i] = 0.08f
    }

    // ── Scan-line animation value (0..1 cycling) ──────────────────────────────
    var scanPos by remember { mutableStateOf(0f) }
    LaunchedEffect(running) {
        var t = 0f
        while (running) {
            t += 0.04f
            if (t > 1f) t = 0f
            scanPos = t
            delay(30)
        }
        scanPos = 0f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 10.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        // ── Header ────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row {
                    Text("GHOSTEC", fontFamily = FontFamily.Monospace, fontSize = 15.sp,
                        color = CryotracGreen, letterSpacing = 2.sp)
                    Text("  WORD ENGINE", fontFamily = FontFamily.Monospace, fontSize = 15.sp,
                        color = CryotracMid, letterSpacing = 2.sp)
                }
                Text(
                    text = if (running) "● SCANNING" else "○ STANDBY",
                    fontFamily = FontFamily.Monospace, fontSize = 10.sp,
                    color = if (running) CryotracGreen else CryotracDim, letterSpacing = 2.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(counter, fontFamily = FontFamily.Monospace, fontSize = 13.sp,
                    color = CryotracMid, letterSpacing = 2.sp)
                Text("WORDS PROC.", fontFamily = FontFamily.Monospace, fontSize = 9.sp,
                    color = CryotracDim, letterSpacing = 1.sp)
            }
        }

        // ── Spectrum analyser ─────────────────────────────────────────────────
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(1.dp, CryotracDim, RoundedCornerShape(2.dp))
                .background(Color(0xFF020802), RoundedCornerShape(2.dp))
                .padding(4.dp)
        ) {
            val totalW = size.width
            val totalH = size.height
            val barW   = totalW / (barCount * 1.6f)
            val gap    = barW * 0.6f

            barHeights.forEachIndexed { i, h ->
                val barH  = h * totalH
                val x     = i * (barW + gap)
                val alpha = if (running) 1f else 0.3f
                val col   = when {
                    h > 0.72f -> CryotracGreen.copy(alpha = alpha)
                    h > 0.45f -> CryotracMid.copy(alpha = alpha)
                    else      -> CryotracDim.copy(alpha = alpha)
                }
                // main bar
                drawRect(color = col, topLeft = Offset(x, totalH - barH), size = Size(barW, barH))
                // top glow cap
                if (running && h > 0.2f) {
                    drawRect(color = CryotracGreen.copy(alpha = 0.9f * alpha),
                        topLeft = Offset(x, totalH - barH), size = Size(barW, 2.dp.toPx()))
                }
            }

            // scan line
            if (running) {
                val sx = scanPos * totalW
                drawLine(
                    color = CryotracGreen.copy(alpha = 0.25f),
                    start = Offset(sx, 0f), end = Offset(sx, totalH),
                    strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Butt
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // ── Word display box ──────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(
                    width = if (running) 1.5.dp else 1.dp,
                    color = if (running) CryotracGreen.copy(alpha = 0.8f) else CryotracDim,
                    shape = RoundedCornerShape(2.dp)
                )
                .background(
                    color = if (running) CryotracGreen.copy(alpha = 0.025f) else Color.Transparent,
                    shape = RoundedCornerShape(2.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                // Corner label
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text("DETECTED FREQUENCY", fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp, color = CryotracDim, letterSpacing = 1.sp,
                        modifier = Modifier.align(Alignment.TopStart).padding(top = 6.dp, start = 6.dp))
                }

                Spacer(Modifier.height(8.dp))

                // Main word
                Text(
                    text = word,
                    fontFamily    = FontFamily.Monospace,
                    fontSize      = 40.sp,
                    letterSpacing = 4.sp,
                    color         = if (running) CryotracGreen else CryotracMid,
                    textAlign     = TextAlign.Center
                )

                // Recent words trail — last 4, fading out
                if (recentWords.size > 1) {
                    Spacer(Modifier.height(12.dp))
                    val trail = recentWords.dropLast(1).reversed()
                    trail.forEachIndexed { i, w ->
                        val alphas = listOf(0.45f, 0.28f, 0.16f, 0.09f)
                        val a = alphas.getOrElse(i) { 0.06f }
                        Text(w, fontFamily = FontFamily.Monospace, fontSize = 13.sp,
                            color = CryotracMid.copy(alpha = a),
                            letterSpacing = 3.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        // ── Progress bar (thicker, segmented look) ────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color(0xFF020F02), RoundedCornerShape(2.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .background(
                        color = if (progress > 0.85f) CryotracYellow
                                else if (progress > 0.5f) CryotracMid
                                else CryotracGreen,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }

        Spacer(Modifier.height(6.dp))

        // ── Start / Stop ──────────────────────────────────────────────────────
        Button(
            onClick = { vm.toggleGhostec() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (running) CryotracGreen.copy(alpha = 0.12f) else Color.Transparent,
                contentColor   = if (running) CryotracGreen else CryotracDim
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp, if (running) CryotracGreen else CryotracDim
            )
        ) {
            Text(
                text = if (running) "■   STOP SEQUENCE" else "▶   INITIATE SEQUENCE",
                fontFamily = FontFamily.Monospace, fontSize = 18.sp, letterSpacing = 4.sp
            )
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = CryotracDim)
        Spacer(Modifier.height(6.dp))

        // ── Questions ─────────────────────────────────────────────────────────
        QuestionsPanel(
            question = question,
            onNext   = { vm.showNextQuestion() },
            onSpeak  = { vm.speakCurrentQuestion() },
            modifier = Modifier.height(195.dp)
        )

        Text(
            text = "FOR ENTERTAINMENT PURPOSES ONLY",
            fontFamily = FontFamily.Monospace, fontSize = 10.sp,
            color = CryotracDim, letterSpacing = 2.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
        )
    }
}
