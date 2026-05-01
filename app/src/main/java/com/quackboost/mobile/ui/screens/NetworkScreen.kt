package com.quackboost.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quackboost.mobile.data.HardwareMonitor
import com.quackboost.mobile.data.NetworkInfo
import com.quackboost.mobile.ui.components.*
import com.quackboost.mobile.ui.theme.*

@Composable
fun NetworkScreen(info: NetworkInfo?) {
    val signalPct = ((info?.signalStrength ?: -100) + 100).coerceIn(0, 100).toFloat()
    val signalColor = when {
        signalPct >= 70 -> ColorGood
        signalPct >= 40 -> ColorWarn
        else -> ColorDanger
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Signal strength
        InfoCard(title = "Sinal Wi-Fi", icon = "📶") {
            LabeledProgressBar(
                label     = "Intensidade",
                value     = signalPct,
                valueText = "${info?.signalStrength ?: "—"} dBm",
                color     = signalColor
            )
            Spacer(Modifier.height(8.dp))
            InfoRow("SSID",         info?.ssid ?: "—")
            InfoRow("BSSID",        info?.bssid ?: "—")
            InfoRow("Velocidade",   "${info?.linkSpeed ?: "—"} Mbps")
            InfoRow("Frequência",   when {
                (info?.frequencyMhz ?: 0) in 2400..2500 -> "2.4 GHz"
                (info?.frequencyMhz ?: 0) in 5000..6000 -> "5 GHz"
                (info?.frequencyMhz ?: 0) >= 6000 -> "6 GHz"
                else -> "${info?.frequencyMhz ?: "—"} MHz"
            })
        }

        // Connection info
        InfoCard(title = "Conexão", icon = "🌐") {
            InfoRow("Endereço IP",  info?.ipAddress ?: "—")
            InfoRow("MAC Address",  info?.macAddress ?: "—")
            InfoRow("Tipo de Rede", info?.networkType ?: "—")
        }

        // Traffic
        InfoCard(title = "Tráfego da Sessão", icon = "📊") {
            InfoRow("Recebido",  HardwareMonitor.formatBytes(info?.rxBytes ?: 0), valueColor = ColorGood)
            InfoRow("Enviado",   HardwareMonitor.formatBytes(info?.txBytes ?: 0), valueColor = Accent)
        }

        Spacer(Modifier.height(80.dp))
    }
}
