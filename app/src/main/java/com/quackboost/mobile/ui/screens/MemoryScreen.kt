package com.quackboost.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quackboost.mobile.data.HardwareMonitor
import com.quackboost.mobile.data.MemoryInfo
import com.quackboost.mobile.ui.components.*
import com.quackboost.mobile.ui.theme.*

@Composable
fun MemoryScreen(info: MemoryInfo?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // RAM gauge
        InfoCard(title = "RAM", icon = "💾") {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ArcGauge(
                    value    = info?.usagePercent ?: 0f,
                    label    = "Uso",
                    sublabel = if (info != null)
                        "${HardwareMonitor.formatBytes(info.usedRam)} / ${HardwareMonitor.formatBytes(info.totalRam)}"
                    else "",
                    size     = 150.dp,
                    color    = ColorInfo
                )
            }
            Spacer(Modifier.height(12.dp))
            LabeledProgressBar(
                label     = "Em uso",
                value     = info?.usagePercent ?: 0f,
                valueText = HardwareMonitor.formatBytes(info?.usedRam ?: 0),
                color     = ColorInfo
            )
            Spacer(Modifier.height(8.dp))
            LabeledProgressBar(
                label     = "Disponível",
                value     = 100f - (info?.usagePercent ?: 0f),
                valueText = HardwareMonitor.formatBytes(info?.availableRam ?: 0),
                color     = ColorGood
            )
        }

        // RAM details
        InfoCard(title = "Detalhes da RAM", icon = "📋") {
            InfoRow("Total",       HardwareMonitor.formatBytes(info?.totalRam ?: 0))
            InfoRow("Em uso",      HardwareMonitor.formatBytes(info?.usedRam ?: 0), valueColor = ColorInfo)
            InfoRow("Disponível",  HardwareMonitor.formatBytes(info?.availableRam ?: 0), valueColor = ColorGood)
            InfoRow("Memória baixa", if (info?.isLowMemory == true) "⚠️ Sim" else "Não",
                valueColor = if (info?.isLowMemory == true) ColorDanger else ColorGood)
        }

        // Storage
        InfoCard(title = "Armazenamento Interno", icon = "💿") {
            val storagePct = if ((info?.totalStorage ?: 0L) > 0L)
                (info!!.usedStorage.toFloat() / info.totalStorage.toFloat() * 100f) else 0f

            LabeledProgressBar(
                label     = "Usado",
                value     = storagePct,
                valueText = HardwareMonitor.formatBytes(info?.usedStorage ?: 0),
                color     = Accent
            )
            Spacer(Modifier.height(12.dp))
            InfoRow("Total",      HardwareMonitor.formatBytes(info?.totalStorage ?: 0))
            InfoRow("Usado",      HardwareMonitor.formatBytes(info?.usedStorage ?: 0), valueColor = Accent)
            InfoRow("Disponível", HardwareMonitor.formatBytes(info?.availableStorage ?: 0), valueColor = ColorGood)
        }

        Spacer(Modifier.height(80.dp))
    }
}
