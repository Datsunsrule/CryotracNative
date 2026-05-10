package com.cryotrac.paranormal.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cryotrac.paranormal.R
import com.cryotrac.paranormal.ui.theme.*
import com.cryotrac.paranormal.viewmodel.CryotracViewModel
import kotlin.math.*

@Composable
fun ScannerScreen(vm: CryotracViewModel) {
    val ch01On       by vm.ch01On.collectAsState()
    val ch01Signal   by vm.ch01Signal.collectAsState()
    val ch01Angle    by vm.ch01Angle.collectAsState()
    val touchCount   by vm.touchCount.collectAsState()
    val emfMag       by vm.emfMag.collectAsState()
    val emfX         by vm.emfX.collectAsState()
    val emfY         by vm.emfY.collectAsState()
    val emfZ         by vm.emfZ.collectAsState()
    val emfStatus    by vm.emfStatus.collectAsState()
    val emfOn        by vm.emfOn.collectAsState()
    val emfAnomalies by vm.emfAnomalyCount.collectAsState()
    val emfLive      by vm.emfLive.collectAsState()
    val emfBaseline  by vm.emfBaseline.collectAsState()
    val question     by vm.currentQuestion.collectAsState()

    // ── Full-screen touch capture for CH-01 ──────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(ch01On) {
                if (!ch01On) return@pointerInput
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        for (change in event.changes) {
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val dx = change.position.x - cx
                            val dy = change.position.y - cy
                            val angleDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            val dist     = sqrt(dx * dx + dy * dy)
                            val maxDist  = sqrt(cx * cx + cy * cy)
                            val signal   = (dist / maxDist * 100f).coerceIn(0f, 100f)
                            when {
                                change.pressed && !change.previousPressed -> {
                                    vm.incrementTouch()
                                    vm.updateCh01Signal(signal, angleDeg)
                                }
                                change.pressed -> vm.updateCh01Signal(signal, angleDeg)
                                !change.pressed && change.previousPressed ->
                                    vm.updateCh01Signal(0f, angleDeg)
                            }
                        }
                    }
                }
            }
    ) {
        // Faded logo watermark
        Image(
            painter = painterResource(R.drawable.logofinal),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize().alpha(0.04f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp)
        ) {
            Spacer(Modifier.height(2.dp))

            // ── CH-01 Touch Sensor ────────────────────────────────────────────
            PanelHeader(ch = "CH-01", title = "TOUCH SENSOR") {
                ToggleButton(on = ch01On, onClick = { vm.toggleCh01() })
            }

            // Circular meter
            Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircularMeter(signal = ch01Signal, angleDeg = ch01Angle)
                }
            }

            // Signal + status + touch count
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
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(ch01Signal.toInt().toString().padStart(3, '0'),
                    fontFamily = FontFamily.Monospace, fontSize = 26.sp,
                    color = CryotracGreen, letterSpacing = 2.sp)
                Text(statusText, fontFamily = FontFamily.Monospace, fontSize = 18.sp,
                    color = statusColor, letterSpacing = 3.sp)
                Text("TOUCHES: $touchCount", fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp, color = CryotracMid, letterSpacing = 1.sp)
            }

            HorizontalDivider(color = CryotracDim, modifier = Modifier.padding(vertical = 2.dp))

            // ── CH-02 EMF ─────────────────────────────────────────────────────
            PanelHeader(ch = "CH-02", title = "EMF DETECTOR") {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("ΔEMF: $emfAnomalies", fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp, color = CryotracMid, letterSpacing = 1.sp)
                    ToggleButton(on = emfOn, onClick = { vm.toggleEmf() })
                }
            }

            val emfColor = when (emfStatus) {
                "ANOMALY"  -> CryotracRed
                "HIGH"     -> CryotracYellow
                "ELEVATED" -> CryotracCyan
                else       -> CryotracGreen
            }
            EmfBar(mag = emfMag, color = emfColor, modifier = Modifier.fillMaxWidth().height(34.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(emfStatus, fontFamily = FontFamily.Monospace, fontSize = 18.sp,
                    color = emfColor, letterSpacing = 3.sp)
                Text(if (emfMag > 0) "${"%.1f".format(emfMag)} μT" else "--.- μT",
                    fontFamily = FontFamily.Monospace, fontSize = 20.sp,
                    color = emfColor, letterSpacing = 2.sp)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                listOf("X" to emfX, "Y" to emfY, "Z" to emfZ).forEach { (axis, v) ->
                    Text("$axis: ${"%.1f".format(v)}", fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp, color = CryotracMid, letterSpacing = 1.sp)
                }
            }

            val calibrated = emfBaseline != null
            Button(
                onClick = { vm.calibrateEmf() },
                modifier = Modifier.fillMaxWidth().height(36.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (calibrated) CryotracGreen.copy(alpha = 0.08f) else Color.Transparent,
                    contentColor   = if (calibrated) CryotracGreen else CryotracDim),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, if (calibrated) CryotracGreen else CryotracDim)
            ) {
                Text(
                    if (calibrated) "✓  CALIBRATED" else "◎  CALIBRATE BASELINE",
                    fontFamily = FontFamily.Monospace, fontSize = 13.sp, letterSpacing = 2.sp
                )
            }

            HorizontalDivider(color = CryotracDim, modifier = Modifier.padding(vertical = 2.dp))

            // ── Field Readings ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CryotracDim, RoundedCornerShape(2.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("FIELD READINGS", fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp, color = CryotracDim, letterSpacing = 2.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ReadingCell(
                            label = "EMF BASELINE",
                            value = if (emfBaseline != null) "${"%.1f".format(emfBaseline)} μT" else "---"
                        )
                        ReadingCell(label = "TOUCH EVENTS",
                            value = touchCount.toString().padStart(3, '0'))
                        ReadingCell(label = "EMF ANOMALIES",
                            value = emfAnomalies.toString().padStart(3, '0'))
                        ReadingCell(
                            label = "SENSOR",
                            value = if (emfLive) "LIVE" else "SIM",
                            valueColor = if (emfLive) CryotracGreen else CryotracDim
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Questions ─────────────────────────────────────────────────────
            QuestionsPanel(
                question = question,
                onNext   = { vm.showNextQuestion() },
                onSpeak  = { vm.speakCurrentQuestion() },
                modifier = Modifier.height(178.dp)
            )

            Spacer(Modifier.height(2.dp))
        }
    }
}

// ── Circular needle meter ─────────────────────────────────────────────────────
fun DrawScope.drawCircularMeter(signal: Float, angleDeg: Float) {
    val cx    = size.width / 2f
    val cy    = size.height / 2f
    val maxR  = minOf(size.width, size.height) / 2f - 4.dp.toPx()

    val green  = Color(0xFF39FF14)
    val yellow = Color(0xFFFFDD00)
    val red    = Color(0xFFFF3300)
    val trackBg  = Color(0xFF020D02)
    val trackDim = Color(0xFF0C380C)

    // Zone fills: outer → inner so inner overwrites
    drawCircle(color = Color(0xFF120202), radius = maxR,           center = Offset(cx, cy)) // red zone bg
    drawCircle(color = Color(0xFF0D0D00), radius = maxR * 0.70f,   center = Offset(cx, cy)) // yellow zone bg
    drawCircle(color = trackBg,           radius = maxR * 0.40f,   center = Offset(cx, cy)) // green zone bg

    // Zone ring borders
    drawCircle(color = trackDim.copy(alpha = 0.6f), radius = maxR * 0.40f,
        center = Offset(cx, cy), style = Stroke(1.dp.toPx()))
    drawCircle(color = Color(0xFF2A2A00).copy(alpha = 0.5f), radius = maxR * 0.70f,
        center = Offset(cx, cy), style = Stroke(1.dp.toPx()))
    drawCircle(color = trackDim, radius = maxR,
        center = Offset(cx, cy), style = Stroke(1.5.dp.toPx()))

    // Cardinal ticks (N/E/S/W)
    for (tickDeg in listOf(0f, 90f, 180f, 270f)) {
        val rad   = Math.toRadians(tickDeg.toDouble())
        val inner = maxR - 14.dp.toPx()
        val outer = maxR - 4.dp.toPx()
        drawLine(color = green.copy(alpha = 0.45f),
            start = Offset((cx + inner * cos(rad)).toFloat(), (cy + inner * sin(rad)).toFloat()),
            end   = Offset((cx + outer * cos(rad)).toFloat(), (cy + outer * sin(rad)).toFloat()),
            strokeWidth = 2.dp.toPx())
    }
    // Diagonal ticks
    for (tickDeg in listOf(45f, 135f, 225f, 315f)) {
        val rad   = Math.toRadians(tickDeg.toDouble())
        val inner = maxR - 9.dp.toPx()
        val outer = maxR - 4.dp.toPx()
        drawLine(color = trackDim,
            start = Offset((cx + inner * cos(rad)).toFloat(), (cy + inner * sin(rad)).toFloat()),
            end   = Offset((cx + outer * cos(rad)).toFloat(), (cy + outer * sin(rad)).toFloat()),
            strokeWidth = 1.dp.toPx())
    }

    // Needle (only when active)
    if (signal > 0f) {
        val angleRad  = Math.toRadians(angleDeg.toDouble())
        val needleLen = (signal / 100f) * maxR * 0.82f
        val nx = (cx + needleLen * cos(angleRad)).toFloat()
        val ny = (cy + needleLen * sin(angleRad)).toFloat()

        val needleColor = when {
            signal >= 85 -> red
            signal >= 60 -> yellow
            else         -> green
        }

        // Glow
        drawLine(color = needleColor.copy(alpha = 0.14f),
            start = Offset(cx, cy), end = Offset(nx, ny),
            strokeWidth = 14.dp.toPx(), cap = StrokeCap.Round)
        // Body
        drawLine(color = needleColor,
            start = Offset(cx, cy), end = Offset(nx, ny),
            strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        // Tip dot
        drawCircle(color = needleColor, radius = 4.dp.toPx(), center = Offset(nx, ny))
        // Tip glow
        drawCircle(color = needleColor.copy(alpha = 0.3f), radius = 8.dp.toPx(), center = Offset(nx, ny))
    }

    // Pivot
    drawCircle(color = green.copy(alpha = 0.20f), radius = 13.dp.toPx(), center = Offset(cx, cy))
    drawCircle(color = trackDim,  radius = 10.dp.toPx(), center = Offset(cx, cy))
    drawCircle(color = green,     radius =  5.dp.toPx(), center = Offset(cx, cy))
    drawCircle(color = Color.Black, radius = 2.dp.toPx(), center = Offset(cx, cy))
}

// ── EMF segmented bar ────────────────────────────────────────────────────────
@Composable
fun EmfBar(mag: Float, color: Color, modifier: Modifier = Modifier) {
    val segments = 20
    val lit = ((mag / 200f) * segments).toInt().coerceIn(0, segments)
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(segments) { i ->
            val segColor = when {
                i == lit - 1             -> color
                i >= lit - 3 && i < lit - 1 -> color.copy(alpha = 0.6f)
                i < lit                  -> color.copy(alpha = 0.3f)
                else                     -> Color(0xFF020F02)
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

// ── Toggle button ─────────────────────────────────────────────────────────────
@Composable
fun ToggleButton(on: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(36.dp).widthIn(min = 72.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (on) CryotracGreen.copy(alpha = 0.15f) else Color.Transparent,
            contentColor   = if (on) CryotracGreen else CryotracDim
        ),
        border = androidx.compose.foundation.BorderStroke(2.dp, if (on) CryotracGreen else CryotracDim)
    ) {
        Text(if (on) "OFF" else "ON", fontFamily = FontFamily.Monospace,
            fontSize = 16.sp, letterSpacing = 2.sp)
    }
}

// ── Reading cell ─────────────────────────────────────────────────────────────
@Composable
fun ReadingCell(label: String, value: String, valueColor: Color = CryotracGreen) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontFamily = FontFamily.Monospace, fontSize = 15.sp,
            color = valueColor, letterSpacing = 1.sp)
        Text(label, fontFamily = FontFamily.Monospace, fontSize = 8.sp,
            color = CryotracDim, letterSpacing = 1.sp)
    }
}

// ── Questions panel ───────────────────────────────────────────────────────────
@Composable
fun QuestionsPanel(
    question: String,
    onNext: () -> Unit,
    onSpeak: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CryotracGreen, RoundedCornerShape(2.dp))
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("INVESTIGATIVE QUESTIONS", fontFamily = FontFamily.Monospace,
            fontSize = 11.sp, color = CryotracMid, letterSpacing = 2.sp)
        Text(question, fontFamily = FontFamily.Monospace, fontSize = 13.sp,
            color = CryotracGreen, lineHeight = 18.sp, letterSpacing = 1.sp,
            modifier = Modifier.weight(1f))
        Button(
            onClick = onSpeak,
            modifier = Modifier.fillMaxWidth().height(32.dp),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent, contentColor = CryotracGreen),
            border = androidx.compose.foundation.BorderStroke(1.dp, CryotracDim)
        ) {
            Text("▶   READ ALOUD", fontFamily = FontFamily.Monospace,
                fontSize = 14.sp, letterSpacing = 3.sp)
        }
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(32.dp),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent, contentColor = CryotracDim),
            border = androidx.compose.foundation.BorderStroke(1.dp, CryotracDim)
        ) {
            Text("NEXT QUESTION", fontFamily = FontFamily.Monospace,
                fontSize = 14.sp, letterSpacing = 3.sp)
        }
    }
}
