package com.cryotrac.paranormal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cryotrac.paranormal.ui.theme.*

@Composable
fun InfoScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        InfoSection(
            heading = "⬡  CH-01 — TOUCH SENSOR",
            body = "Tap ON to activate. Touch the screen during an investigation. The analog needle and readout respond to the location of each touch. A spike in activity with no obvious cause may indicate a presence. Each touch is counted and logged in the Field Readings panel."
        )
        InfoDivider()
        InfoSection(
            heading = "⬡  CH-02 — EMF DETECTOR",
            body = "Uses your phone's built-in magnetometer to detect real electromagnetic field fluctuations in microteslas (μT) — the same principle as a K-II meter used by professional investigators.\n\nColour coding: AMBIENT → ELEVATED → HIGH → ANOMALY\n\nTap CALIBRATE BASELINE when entering a new location to set a reference point. Any significant deviation from baseline triggers an ANOMALY alert with a voice notification and increments the anomaly counter."
        )
        InfoDivider()
        InfoSection(
            heading = "⊞  FIELD READINGS",
            body = "Displays live session data at a glance:\n\n• EMF BASELINE — the calibrated reference value in μT\n• TOUCH EVENTS — total screen touches this session\n• EMF ANOMALIES — number of anomaly triggers recorded\n• SENSOR — LIVE if the device has a magnetometer, SIM if using the simulation fallback"
        )
        InfoDivider()
        InfoSection(
            heading = "◈  GHOSTEC — WORD ENGINE",
            body = "Tap INITIATE SEQUENCE to begin. Ghostec cycles through 5,000 words drawn from a curated paranormal lexicon and speaks each aloud via text-to-speech.\n\nThe spectrum analyser visualises processing activity in real time. Recent words are displayed in a fading trail below the current word. The sequence counter and progress bar track completion.\n\nListen for words that feel relevant to your location or questions. Tap STOP SEQUENCE at any time to end early."
        )
        InfoDivider()
        InfoSection(
            heading = "?  INVESTIGATIVE QUESTIONS",
            body = "Prompts to guide communication during a session. Tap READ ALOUD to have the question spoken out loud via text-to-speech. Tap NEXT QUESTION to cycle to the next prompt from the pool. The same question is shown on both the Scanner and Word Engine pages."
        )
        InfoDivider()
        InfoSection(
            heading = "⊙  RETURN TO HOME",
            body = "Tap the Ghostrac logo in the top-left at any time to return to the home screen. The session timer and counters continue running in the background."
        )
        InfoDivider()
        InfoSection(
            heading = "↺  SESSION RESET",
            body = "Tap the reset button (↺) in the top-right to restart the session timer and clear all counters — touch events, EMF anomalies, and the EMF baseline. Use this at the start of each new investigation location."
        )

        Text(
            text = "GHOSTRAC — FOR ENTERTAINMENT PURPOSES ONLY",
            fontFamily = FontFamily.Monospace, fontSize = 11.sp,
            color = CryotracDim, letterSpacing = 2.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
        )
    }
}

@Composable
private fun InfoSection(heading: String, body: String) {
    Column(
        modifier = Modifier.padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(heading, fontFamily = FontFamily.Monospace, fontSize = 15.sp,
            color = CryotracGreen, letterSpacing = 2.sp)
        Text(body, fontFamily = FontFamily.Monospace, fontSize = 13.sp,
            color = CryotracMid, lineHeight = 20.sp, letterSpacing = 0.5.sp)
    }
}

@Composable
private fun InfoDivider() = HorizontalDivider(color = CryotracDim, modifier = Modifier.padding(vertical = 2.dp))
