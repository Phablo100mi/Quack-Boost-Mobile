package com.quackboost.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quackboost.mobile.data.CpuInfo
import com.quackboost.mobile.ui.components.*
import com.quackboost.mobile.ui.theme.*

@Composable
fun CpuScreen(info: CpuInfo?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Usage gauge
        InfoCard(title = "Uso da CPU", icon = "🔥") {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ArcGauge(
                    value    = info?.usagePercent ?: 0f,
                    label    = "Uso",
                    sublabel = info?.governor ?: "",
                    size     = 150.dp,
                    color    = Accent
                )
            }
        }

        // Model & specs
        InfoCard(title = "Processador", icon = "💻") {
            InfoRow("Modelo", info?.model ?: "—")
            InfoRow("Arquitetura", info?.architecture ?: "—")
            InfoRow("ABI Principal", info?.abi ?: "—")
            InfoRow("Núcleos", "${info?.cores ?: "—"}")
            InfoRow("Freq. Mín.", "${info?.minFreq ?: 0} MHz")
            InfoRow("Freq. Máx.", "${info?.maxFreq ?: 0} MHz")
            InfoRow("Governor", info?.governor ?: "—")
        }

        // Per-core frequency
        if ((info?.frequencies?.size ?: 0) > 0) {
            InfoCard(title = "Frequência por Núcleo", icon = "📊") {
                CoreFrequencyGrid(
                    frequencies = info!!.frequencies,
                    maxFreq     = info.maxFreq
                )
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}
