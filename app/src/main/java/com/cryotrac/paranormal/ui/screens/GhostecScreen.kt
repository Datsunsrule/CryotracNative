package com.cryotrac.paranormal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cryotrac.paranormal.ui.theme.*
import com.cryotrac.paranormal.viewmodel.CryotracViewModel

@Composable
fun GhostecScreen(vm: CryotracViewModel) {
    val running  by vm.ghostecRunning.collectAsState()
    val word     by vm.ghostecWord.collectAsState()
    val progress by vm.ghostecProgress.collectAsState()
    val counter  by vm.ghostecCounter.collectAsState()
    val question by vm.currentQuestion.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 10.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        PanelHeader(ch = "GHOSTEC", title = "WORD ENGINE") {
            Text(counter, fontFamily = FontFamily.Monospace, fontSize = 13.sp,
                color = CryotracMid, letterSpacing = 2.sp)
        }

        // Word display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = word,
                fontFamily    = FontFamily.Monospace,
                fontSize      = 42.sp,
                letterSpacing = 4.sp,
                color         = CryotracGreen,
                textAlign     = TextAlign.Center
            )
        }

        // Progress bar
        LinearProgressIndicator(
            progress          = { progress },
            modifier          = Modifier.fillMaxWidth().height(3.dp),
            color             = CryotracGreen,
            trackColor        = CryotracDim,
        )
        Spacer(Modifier.height(4.dp))

        // Start/stop button
        Button(
            onClick = { vm.toggleGhostec() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
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
        Spacer(Modifier.height(8.dp))

        // Questions (shared pool with scanner screen)
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
