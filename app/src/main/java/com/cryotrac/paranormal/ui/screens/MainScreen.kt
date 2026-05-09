package com.cryotrac.paranormal.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cryotrac.paranormal.BuildConfig
import com.cryotrac.paranormal.R
import com.cryotrac.paranormal.ui.theme.*
import com.cryotrac.paranormal.viewmodel.CryotracViewModel

@Composable
fun MainScreen(onHome: () -> Unit, vm: CryotracViewModel = viewModel()) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val sessionTime by vm.sessionTime.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        // Header
        AppHeader(sessionTime = sessionTime, onHome = onHome, onRestart = { vm.resetSession() })
        HorizontalDivider(color = CryotracDim, thickness = 1.dp)

        // Page content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> ScannerScreen(vm = vm)
                1 -> GhostecScreen(vm = vm)
                2 -> InfoScreen()
            }
        }

        // Tab bar
        HorizontalDivider(color = CryotracDim, thickness = 1.dp)
        AppTabBar(selected = selectedTab, onSelect = { selectedTab = it })
    }
}

@Composable
fun AppHeader(sessionTime: String, onHome: () -> Unit, onRestart: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo — tap to return home
            Image(
                painter = painterResource(R.drawable.logofinal),
                contentDescription = "Home",
                modifier = Modifier.size(46.dp).clickable { onHome() }
            )

            Text(
                text = "CRYOTRAC",
                fontFamily    = FontFamily.Monospace,
                fontSize      = 28.sp,
                letterSpacing = 7.sp,
                color         = CryotracGreen,
                textAlign     = TextAlign.Center,
                modifier      = Modifier.weight(1f)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier.size(44.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CryotracDim),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CryotracDim)
                ) {
                    Text("↺", fontSize = 18.sp, color = CryotracDim)
                }
                Text(
                    text = sessionTime,
                    fontFamily    = FontFamily.Monospace,
                    fontSize      = 16.sp,
                    color         = CryotracMid,
                    letterSpacing = 2.sp
                )
            }
        }

        // Subtitle row
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PARANORMAL INVESTIGATION TOOL",
                fontFamily    = FontFamily.Monospace,
                fontSize      = 13.sp,
                color         = CryotracMid,
                letterSpacing = 2.sp,
                modifier      = Modifier.padding(start = 10.dp)
            )
            // ── Version stamp ──────────────────────────────────────────────
            Text(
                text = "v${BuildConfig.VERSION_NAME}·${BuildConfig.VERSION_CODE}",
                fontFamily    = FontFamily.Monospace,
                fontSize      = 10.sp,
                color         = CryotracDim,
                letterSpacing = 1.sp,
                modifier      = Modifier.padding(end = 10.dp)
            )
        }
    }
}

@Composable
fun AppTabBar(selected: Int, onSelect: (Int) -> Unit) {
    val tabs = listOf("⬡  SCANNER", "◈  GHOSTEC", "?  INFO")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(Color.Black)
    ) {
        tabs.forEachIndexed { i, label ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onSelect(i) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (i == selected) {
                    HorizontalDivider(
                        color = CryotracGreen, thickness = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = label,
                    fontFamily    = FontFamily.Monospace,
                    fontSize      = 13.sp,
                    letterSpacing = 2.sp,
                    color         = if (i == selected) CryotracGreen else CryotracDim
                )
                Spacer(Modifier.weight(1f))
            }
        }
    }
}
