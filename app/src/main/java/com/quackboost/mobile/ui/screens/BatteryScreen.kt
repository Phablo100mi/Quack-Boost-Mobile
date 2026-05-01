package com.quackboost.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quackboost.mobile.data.BatteryInfo
import com.quackboost.mobile.ui.components.*
import com.quackboost.mobile.ui.theme.*

@Composable
fun BatteryScreen(info: BatteryInfo?) {
    val levelColor = when {
        (info?.levelPercent ?: 100) <= 15 -> ColorDanger
        (info?.levelPercent ?: 100) <= 30 -> ColorWarn
        else -> ColorGood
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Level gauge
        InfoCard(title = "Nível de Bateria", icon = "🔋") {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ArcGauge(
                    value    = (info?.levelPercent ?: 0).toFloat(),
                    label    = info?.status ?: "—",
                    sublabel = if (info?.isCharging == true) "⚡ ${info.chargeType}" else "",
                    size     = 150.dp,
                    color    = levelColor
                )
            }
        }

        // Details
        InfoCard(title = "Informações da Bateria", icon = "📋") {
            InfoRow("Nível",       "${info?.levelPercent ?: "—"}%",       valueColor = levelColor)
            InfoRow("Status",      info?.status ?: "—")
            InfoRow("Saúde",       info?.health ?: "—",
                valueColor = if (info?.health == "Bom") ColorGood else ColorWarn)
            InfoRow("Tecnologia",  info?.technology ?: "—")
            InfoRow("Carregando",  if (info?.isCharging == true) "Sim (${info.chargeType})" else "Não",
                valueColor = if (info?.isCharging == true) ColorGood else TextPrimary)
        }

        // Electrical
        InfoCard(title = "Dados Elétricos", icon = "⚡") {
            InfoRow("Temperatura",  "%.1f°C".format(info?.tempCelsius ?: 0f),
                valueColor = if (info != null) com.quackboost.mobile.data.HardwareMonitor.tempColor(info.tempCelsius) else TextPrimary)
            InfoRow("Tensão",       "%.2f V".format(info?.voltageV ?: 0f))
            InfoRow("Corrente",     if ((info?.currentMa ?: 0L) > 0) "${info?.currentMa} mA" else "—")
            InfoRow("Capacidade",   if ((info?.capacityMah ?: 0L) > 0) "${info?.capacityMah} mAh" else "—")
        }

        Spacer(Modifier.height(80.dp))
    }
}
