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
import com.quackboost.mobile.data.ThermalInfo
import com.quackboost.mobile.ui.components.*
import com.quackboost.mobile.ui.theme.*
import com.quackboost.mobile.data.HardwareMonitor

@Composable
fun ThermalScreen(info: ThermalInfo?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Overview - hottest sensor
        val hottest = info?.zones?.maxByOrNull { it.tempCelsius }
        InfoCard(title = "Temperatura Principal", icon = "🌡️") {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (hottest != null) "%.1f°C".format(hottest.tempCelsius) else "—",
                        fontSize   = 52.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (hottest != null)
                            HardwareMonitor.tempColor(hottest.tempCelsius)
                        else TextMuted
                    )
                    Text(
                        text  = hottest?.name ?: "Sem sensor",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(8.dp))
                    val status = when {
                        (hottest?.tempCelsius ?: 0f) >= 80 -> "⚠️ CRÍTICO"
                        (hottest?.tempCelsius ?: 0f) >= 60 -> "⚡ QUENTE"
                        (hottest?.tempCelsius ?: 0f) > 0   -> "✅ NORMAL"
                        else -> "—"
                    }
                    val statusColor = when {
                        (hottest?.tempCelsius ?: 0f) >= 80 -> ColorDanger
                        (hottest?.tempCelsius ?: 0f) >= 60 -> ColorWarn
                        else -> ColorGood
                    }
                    Text(status, fontSize = 12.sp, color = statusColor, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // All zones
        InfoCard(title = "Todas as Zonas Térmicas", icon = "📡") {
            if (info?.zones.isNullOrEmpty()) {
                Text(
                    "Sensores térmicos não disponíveis neste dispositivo",
                    fontSize = 12.sp,
                    color    = TextMuted
                )
            } else {
                info!!.zones.forEachIndexed { i, zone ->
                    val pct = (zone.tempCelsius / 100f * 100f).coerceIn(0f, 100f)
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(zone.name, fontSize = 12.sp, color = TextSecondary)
                            TempBadge(zone.tempCelsius)
                        }
                        Spacer(Modifier.height(4.dp))
                        LabeledProgressBar(
                            label     = "",
                            value     = pct,
                            valueText = "",
                            color     = HardwareMonitor.tempColor(zone.tempCelsius)
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}
