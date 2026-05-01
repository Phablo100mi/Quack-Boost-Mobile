package com.quackboost.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quackboost.mobile.data.DeviceInfo
import com.quackboost.mobile.ui.components.*
import com.quackboost.mobile.ui.theme.*

@Composable
fun DeviceScreen(info: DeviceInfo?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero
        InfoCard(title = "") {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📱", fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${info?.brand ?: "—"} ${info?.model ?: ""}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Android ${info?.androidVersion ?: "—"} · API ${info?.apiLevel ?: "—"}",
                        fontSize = 13.sp,
                        color = Accent
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Uptime: ${info?.uptime ?: "—"}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }

        // Hardware
        InfoCard(title = "Hardware", icon = "🔧") {
            InfoRow("Placa",    info?.board ?: "—")
            InfoRow("Hardware", info?.hardware ?: "—")
            InfoRow("Produto",  info?.product ?: "—")
        }

        // Software
        InfoCard(title = "Software", icon = "💿") {
            InfoRow("Android",    info?.androidVersion ?: "—")
            InfoRow("API Level",  "${info?.apiLevel ?: "—"}")
            InfoRow("Build ID",   info?.buildId ?: "—")
            InfoRow("Kernel",     info?.kernelVersion ?: "—")
        }

        // Display
        InfoCard(title = "Tela", icon = "🖥️") {
            InfoRow("Resolução",     "${info?.screenWidth ?: "—"} × ${info?.screenHeight ?: "—"} px")
            InfoRow("Densidade",     "${info?.displayDensity ?: "—"} dpi")
            InfoRow("Taxa de Refresh","%.0f Hz".format(info?.refreshRate ?: 0f))
        }

        Spacer(Modifier.height(80.dp))
    }
}
