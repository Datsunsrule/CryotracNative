package com.cryotrac.paranormal.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cryotrac.paranormal.ui.theme.*
import com.cryotrac.paranormal.viewmodel.CryotracViewModel
import kotlin.math.*

@Composable
fun ScannerScreen(vm: CryotracViewModel) {
    val ch01On      by vm.ch01On.collectAsState()
    val ch01Signal  by vm.ch01Signal.collectAsState()
    val touchCount  by vm.touchCount.collectAsState()
    val emfMag      by vm.emfMag.collectAsState()
    val emfX        by vm.emfX.collectAsState()
    val emfY        by vm.emfY.collectAsState()
    val emfZ        by vm.emfZ.collectAsState()
    val emfStatus   by vm.emfStatus.collectAsState()
    val emfOn       by vm.emfOn.collectAsState()
    val emfAnomalies by vm.emfAnomalyCount.collectAsState()
    val emfLive     by vm.emfLive.collectAsState()
    val question    by vm.currentQuestion.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 10.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        // ── CH-01 Touch Sensor ────────────────────────────────────────────────
        PanelHeader(ch = "CH-01", title = "TOUCH SENSOR") {
            Button(
                onClick = { vm.toggleCh01() },
                modifier = Modifier.height(36.dp).widthIn(min = 72.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (ch01On) CryotracGreen.copy(alpha = 0.15f) else Color.Transparent,
                    contentColor   = if (ch01On) CryotracGreen else CryotracDim
                ),
                border = androidx.compose.foundation.BorderStroke(
                    2.dp, if (ch01On) CryotracGreen else CryotracDim
                )
            ) {
                Text(if (ch01On) "OFF" else "ON", fontFamily = FontFamily.Monospace, fontSize = 16.sp, letterSpacing = 2.sp)
            }
        }

        // Analog needle (Canvas)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .pointerInput(ch01On) {
                    if (!ch01On) return@pointerInput
                    detectTapGestures(onPress = { offset ->
                        vm.incrementTouch()
                        val signal = ((offset.x / size.width) * 100f).coerceIn(0f, 100f)
                        vm.updateCh01Signal(signal)
                        tryAwaitRelease()
                        vm.updateCh01Signal(0f)
                    })
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawMeter(signal = ch01Signal)
            }
        }

        // Status row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = ch01Signal.toInt().toString().padStart(3, '0'),
                fontFamily = FontFamily.Monospace, fontSize = 32.sp,
                color = CryotracGreen, letterSpacing = 2.sp
            )
            val statusText = when {
                ch01Signal >= 85 -> "ANOMALY"
                ch01Signal >= 60 -> "ACTIVE"
                ch01Signal >= 30 -> "TRACE"
                ch01On           -> "CLEAR"
                else             -> "OFFLINE"
            }
            val statusColor = when {
                ch01Signal >= 85 -> CryotracRed
                ch01Signal >= 60 -> CryotracGreen
                ch01Signal >= 30 -> CryotracGreen
                else             -> CryotracDim
            }
            Text(
                text = statusText,
                fontFamily = FontFamily.Monospace, fontSize = 18.sp,
                color = statusColor, letterSpacing = 3.sp
            )
        }

        // Touch count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CryotracDim, RoundedCornerShape(2.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("TOUCHES THIS SESSION", fontFamily = FontFamily.Monospace,
                fontSize = 12.sp, color = CryotracMid, letterSpacing = 2.sp,
                modifier = Modifier.weight(1f))
            Text(touchCount.toString(), fontFamily = FontFamily.Monospace,
                fontSize = 22.sp, color = CryotracGreen)
        }

        Spacer(Modifier.height(4.dp))
        HorizontalDivider(color = CryotracDim)
        Spacer(Modifier.height(4.dp))

        // ── CH-02 EMF ─────────────────────────────────────────────────────────
        PanelHeader(ch = "CH-02", title = "EMF DETECTOR") {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "ΔEMF: $emfAnomalies",
                    fontFamily = FontFamily.Monospace, fontSize = 13.sp,
                    color = CryotracMid, letterSpacing = 2.sp
                )
                Button(
                    onClick = { vm.toggleEmf() },
                    modifier = Modifier.height(36.dp).widthIn(min = 72.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (emfOn) CryotracGreen.copy(alpha = 0.15f) else Color.Transparent,
                        contentColor   = if (emfOn) CryotracGreen else CryotracDim
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        2.dp, if (emfOn) CryotracGreen else CryotracDim
                    )
                ) {
                    Text(if (emfOn) "OFF" else "ON", fontFamily = FontFamily.Monospace, fontSize = 16.sp, letterSpacing = 2.sp)
                }
            }
        }

        // EMF bar
        val emfColor = when (emfStatus) {
            "ANOMALY"  -> CryotracRed
            "HIGH"     -> CryotracYellow
            "ELEVATED" -> CryotracCyan
            else       -> CryotracGreen
        }
        EmfBar(mag = emfMag, color = emfColor, modifier = Modifier.fillMaxWidth().height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = emfStatus,
                fontFamily = FontFamily.Monospace, fontSize = 18.sp,
                color = emfColor, letterSpacing = 3.sp
            )
            Text(
                text = if (emfMag > 0) "${"%.1f".format(emfMag)} μT" else "--.- μT",
                fontFamily = FontFamily.Monospace, fontSize = 22.sp,
                color = emfColor, letterSpacing = 2.sp
            )
        }

        // X Y Z axis readout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listOf("X" to emfX, "Y" to emfY, "Z" to emfZ).forEach { (axis, v) ->
                Text(
                    text = "$axis: ${"%.1f".format(v)}",
                    fontFamily = FontFamily.Monospace, fontSize = 14.sp,
                    color = CryotracMid, letterSpacing = 2.sp
                )
            }
        }

        // Calibrate button
        Button(
            onClick = { vm.calibrateEmf() },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent, contentColor = CryotracDim
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, CryotracDim)
        ) {
            Text("◎  CALIBRATE BASELINE", fontFamily = FontFamily.Monospace,
                fontSize = 14.sp, letterSpacing = 2.sp)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CryotracDim, RoundedCornerShape(2.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("EMF ANOMALIES", fontFamily = FontFamily.Monospace,
                fontSize = 12.sp, color = CryotracMid, letterSpacing = 2.sp,
                modifier = Modifier.weight(1f))
            Text(emfAnomalies.toString(), fontFamily = FontFamily.Monospace,
                fontSize = 22.sp, color = CryotracGreen)
        }

        Text(
            text = if (emfLive) "● LIVE MAGNETOMETER — REAL EMF DATA"
                   else "⚠ MAGNETOMETER UNAVAILABLE — SIMULATED DATA",
            fontFamily = FontFamily.Monospace, fontSize = 11.sp,
            color = if (emfLive) CryotracMid else CryotracDim,
            letterSpacing = 1.sp, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
        )

        Spacer(Modifier.height(4.dp))
        HorizontalDivider(color = CryotracDim)
        Spacer(Modifier.height(4.dp))

        // ── Questions ─────────────────────────────────────────────────────────
        QuestionsPanel(
            question = question,
            onNext   = { vm.showNextQuestion() },
            onSpeak  = { vm.speakCurrentQuestion() }
        )

        Text(
            text = "FOR ENTERTAINMENT PURPOSES ONLY",
            fontFamily = FontFamily.Monospace, fontSize = 11.sp,
            color = CryotracDim, letterSpacing = 2.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
    }
}

// ── Analog needle drawn with Canvas ──────────────────────────────────────────
fun DrawScope.drawMeter(signal: Float) {
    val cx = size.width / 2f
    val cy = size.height * 0.91f
    val r  = minOf(size.width * 0.44f, size.height * 0.78f)

    val trackBg     = Color(0xFF040D04)
    val trackColor  = Color(0xFF0C380C)
    val greenFg     = Color(0xFF39FF14)
    val yellowFg    = Color(0xFFFFDD00)
    val redFg       = Color(0xFFFF3300)
    val dimGreen    = Color(0xFF1A4A1A)
    val needleColor = Color(0xFF39FF14)
    val arcSize     = androidx.compose.ui.geometry.Size(r * 2, r * 2)
    val arcTopLeft  = Offset(cx - r, cy - r)

    // Background wedge fill
    drawArc(color = trackBg, startAngle = 180f, sweepAngle = 180f,
        useCenter = true, topLeft = arcTopLeft, size = arcSize)

    // Arc track ring
    drawArc(color = trackColor, startAngle = 180f, sweepAngle = 180f,
        useCenter = false, topLeft = arcTopLeft, size = arcSize,
        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Butt))

    // Arc foreground — coloured by zone (green 0–60, yellow 60–85, red 85–100)
    if (signal > 0f) {
        listOf(Triple(0f, 60f, greenFg), Triple(60f, 85f, yellowFg), Triple(85f, 100f, redFg))
            .forEach { (from, to, color) ->
                if (signal > from) {
                    drawArc(color = color,
                        startAngle = 180f + from * 1.8f,
                        sweepAngle = (minOf(signal, to) - from) * 1.8f,
                        useCenter  = false, topLeft = arcTopLeft, size = arcSize,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Butt))
                }
            }
    }

    // Major tick marks — inside the arc, zone-coloured
    listOf(0f, 25f, 50f, 75f, 100f).forEach { pct ->
        val rad   = Math.toRadians((180.0 + pct * 1.8))
        val inner = r - 22.dp.toPx()
        val outer = r - 5.dp.toPx()
        val col   = when { pct >= 85f -> redFg; pct >= 60f -> yellowFg; else -> greenFg }
        drawLine(color = col,
            start = Offset((cx + inner * cos(rad)).toFloat(), (cy + inner * sin(rad)).toFloat()),
            end   = Offset((cx + outer * cos(rad)).toFloat(), (cy + outer * sin(rad)).toFloat()),
            strokeWidth = 2.5.dp.toPx())
    }

    // Minor tick marks — between major ticks
    listOf(12.5f, 37.5f, 62.5f, 87.5f).forEach { pct ->
        val rad   = Math.toRadians((180.0 + pct * 1.8))
        val inner = r - 14.dp.toPx()
        val outer = r - 5.dp.toPx()
        drawLine(color = dimGreen,
            start = Offset((cx + inner * cos(rad)).toFloat(), (cy + inner * sin(rad)).toFloat()),
            end   = Offset((cx + outer * cos(rad)).toFloat(), (cy + outer * sin(rad)).toFloat()),
            strokeWidth = 1.dp.toPx())
    }

    // Needle
    val angleRad = Math.toRadians((180.0 + signal * 1.8))
    val needleLen = r * 0.80f
    val tailLen   = r * 0.12f
    val nx = (cx + needleLen * cos(angleRad)).toFloat()
    val ny = (cy + needleLen * sin(angleRad)).toFloat()
    val tx = (cx - tailLen  * cos(angleRad)).toFloat()
    val ty = (cy - tailLen  * sin(angleRad)).toFloat()

    // Glow
    drawLine(color = needleColor.copy(alpha = 0.18f), start = Offset(tx, ty), end = Offset(nx, ny),
        strokeWidth = 10.dp.toPx(), cap = StrokeCap.Round)
    // Needle body
    drawLine(color = needleColor, start = Offset(tx, ty), end = Offset(nx, ny),
        strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)

    // Pivot
    drawCircle(color = needleColor.copy(alpha = 0.22f), radius = 13.dp.toPx(), center = Offset(cx, cy))
    drawCircle(color = trackColor,  radius = 10.dp.toPx(), center = Offset(cx, cy))
    drawCircle(color = needleColor, radius =  5.dp.toPx(), center = Offset(cx, cy))
    drawCircle(color = Color.Black, radius =  2.dp.toPx(), center = Offset(cx, cy))
}

// ── EMF segmented bar ────────────────────────────────────────────────────────
@Composable
fun EmfBar(mag: Float, color: Color, modifier: Modifier = Modifier) {
    val segments = 20
    val lit = ((mag / 200f) * segments).toInt().coerceIn(0, segments)
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(segments) { i ->
            val segColor = when {
                i == lit - 1        -> color
                i >= lit - 3 && i < lit - 1 -> color.copy(alpha = 0.6f)
                i < lit             -> color.copy(alpha = 0.3f)
                else                -> Color(0xFF020F02)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(segColor, RoundedCornerShape(2.dp))
                    .border(1.dp, Color(0xFF0A2A0A), RoundedCornerShape(2.dp))
            )
        }
    }
}

// ── Panel header ─────────────────────────────────────────────────────────────
@Composable
fun PanelHeader(ch: String, title: String, trailing: @Composable () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f)) {
            Text(ch, fontFamily = FontFamily.Monospace, fontSize = 15.sp,
                color = CryotracGreen, letterSpacing = 2.sp)
            Text("  $title", fontFamily = FontFamily.Monospace, fontSize = 15.sp,
                color = CryotracMid, letterSpacing = 2.sp,
                maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }
        trailing()
    }
}

// ── Questions panel ───────────────────────────────────────────────────────────
@Composable
fun QuestionsPanel(question: String, onNext: () -> Unit, onSpeak: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CryotracGreen, RoundedCornerShape(2.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("SAMPLE INVESTIGATIVE QUESTIONS", fontFamily = FontFamily.Monospace,
            fontSize = 12.sp, color = CryotracMid, letterSpacing = 3.sp)
        Text(question, fontFamily = FontFamily.Monospace, fontSize = 14.sp,
            color = CryotracGreen, lineHeight = 20.sp, letterSpacing = 1.sp,
            modifier = Modifier.defaultMinSize(minHeight = 56.dp))
        Button(
            onClick = onSpeak, modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent, contentColor = CryotracGreen),
            border = androidx.compose.foundation.BorderStroke(1.dp, CryotracDim)
        ) {
            Text("▶   READ ALOUD", fontFamily = FontFamily.Monospace,
                fontSize = 16.sp, letterSpacing = 3.sp)
        }
        Button(
            onClick = onNext, modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent, contentColor = CryotracDim),
            border = androidx.compose.foundation.BorderStroke(1.dp, CryotracDim)
        ) {
            Text("NEXT QUESTION", fontFamily = FontFamily.Monospace,
                fontSize = 16.sp, letterSpacing = 3.sp)
        }
    }
}
