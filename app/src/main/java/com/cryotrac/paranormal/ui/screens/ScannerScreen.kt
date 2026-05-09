package com.cryotrac.paranormal.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
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
        PanelHeader(ch = "CH-01", title = "SCREEN TOUCH SENSOR") {
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
                .height(160.dp)
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
            Text("NUMBER OF TOUCHES THIS SESSION", fontFamily = FontFamily.Monospace,
                fontSize = 12.sp, color = CryotracMid, letterSpacing = 2.sp)
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
                fontFamily = FontFamily.Monospace, fontSize = 32.sp,
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
            Text("NUMBER OF EMF ANOMALIES THIS SESSION", fontFamily = FontFamily.Monospace,
                fontSize = 12.sp, color = CryotracMid, letterSpacing = 2.sp)
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
    val cy = size.height * 0.9f
    val r  = size.height * 0.82f
    val arcColor   = Color(0xFF0A4A0A)
    val needleColor = Color(0xFF39FF14)
    val fgColor    = Color(0xFF39FF14)

    // Arc background (−90° to +90°, i.e. 180°–360° in standard terms)
    drawArc(
        color       = arcColor,
        startAngle  = 180f,
        sweepAngle  = 180f,
        useCenter   = false,
        topLeft     = Offset(cx - r, cy - r),
        size        = androidx.compose.ui.geometry.Size(r * 2, r * 2),
        style       = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
    )

    // Arc foreground (proportional to signal)
    if (signal > 0) {
        drawArc(
            color       = fgColor,
            startAngle  = 180f,
            sweepAngle  = signal * 1.8f,
            useCenter   = false,
            topLeft     = Offset(cx - r, cy - r),
            size        = androidx.compose.ui.geometry.Size(r * 2, r * 2),
            style       = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )
    }

    // Needle
    val angleDeg = 180f + signal * 1.8f
    val angleRad = Math.toRadians(angleDeg.toDouble())
    val needleLen = r * 0.85f
    val nx = (cx + needleLen * cos(angleRad)).toFloat()
    val ny = (cy + needleLen * sin(angleRad)).toFloat()
    drawLine(color = needleColor, start = Offset(cx, cy), end = Offset(nx, ny),
        strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)

    // Pivot
    drawCircle(color = needleColor, radius = 7.dp.toPx(), center = Offset(cx, cy))
    drawCircle(color = Color.Black,  radius = 3.dp.toPx(), center = Offset(cx, cy))

    // Tick marks
    listOf(0f, 25f, 50f, 75f, 100f).forEach { pct ->
        val tickDeg = 180f + pct * 1.8f
        val tickRad = Math.toRadians(tickDeg.toDouble())
        val inner = r - 12.dp.toPx()
        val outer = r + 12.dp.toPx()
        drawLine(
            color = Color(0xFF20C020),
            start = Offset((cx + inner * cos(tickRad)).toFloat(), (cy + inner * sin(tickRad)).toFloat()),
            end   = Offset((cx + outer * cos(tickRad)).toFloat(), (cy + outer * sin(tickRad)).toFloat()),
            strokeWidth = 2.dp.toPx()
        )
    }
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
        Row {
            Text(ch, fontFamily = FontFamily.Monospace, fontSize = 17.sp,
                color = CryotracGreen, letterSpacing = 3.sp)
            Text("  $title", fontFamily = FontFamily.Monospace, fontSize = 17.sp,
                color = CryotracMid, letterSpacing = 3.sp)
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
