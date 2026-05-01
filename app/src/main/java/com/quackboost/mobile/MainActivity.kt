package com.quackboost.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.quackboost.mobile.ui.screens.*
import com.quackboost.mobile.ui.theme.*
import com.quackboost.mobile.viewmodel.HardwareViewModel

// ─────────────────────────────────────────────────────────────────────────────
// Tab definitions
// ─────────────────────────────────────────────────────────────────────────────

data class NavTab(val id: String, val icon: String, val label: String)

val navTabs = listOf(
    NavTab("device",  "📱", "Dispositivo"),
    NavTab("cpu",     "🔥", "CPU"),
    NavTab("memory",  "💾", "Memória"),
    NavTab("thermal", "🌡️", "Thermal"),
    NavTab("battery", "🔋", "Bateria"),
    NavTab("network", "📶", "Rede"),
)

// ─────────────────────────────────────────────────────────────────────────────
// MainActivity
// ─────────────────────────────────────────────────────────────────────────────

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuackBoostTheme {
                QuackBoostApp()
            }
        }
    }
}

@Composable
fun QuackBoostApp() {
    val vm: HardwareViewModel = viewModel()
    val state by vm.state.collectAsState()

    // Status/nav bar colors
    val sysUi = rememberSystemUiController()
    SideEffect {
        sysUi.setSystemBarsColor(color = BgBase, darkIcons = false)
    }

    var selectedTab by remember { mutableStateOf("cpu") }

    Scaffold(
        containerColor = BgBase,
        topBar = { TopBar() },
        bottomBar = {
            BottomTabBar(
                tabs       = navTabs,
                selected   = selectedTab,
                onSelect   = { selectedTab = it }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                LoadingView()
            } else {
                AnimatedContent(
                    targetState  = selectedTab,
                    transitionSpec = {
                        fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                    },
                    label = "tab_transition"
                ) { tab ->
                    when (tab) {
                        "device"  -> DeviceScreen(state.device)
                        "cpu"     -> CpuScreen(state.cpu)
                        "memory"  -> MemoryScreen(state.memory)
                        "thermal" -> ThermalScreen(state.thermal)
                        "battery" -> BatteryScreen(state.battery)
                        "network" -> NetworkScreen(state.network)
                        else      -> CpuScreen(state.cpu)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top Bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgBase)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🦆", fontSize = 22.sp)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    "QuackBoost",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextPrimary
                )
                Text(
                    "Monitor de Hardware",
                    fontSize = 11.sp,
                    color    = TextMuted
                )
            }
        }
        // Live indicator
        LiveBadge()
    }
}

@Composable
fun LiveBadge() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue  = 0.4f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(ColorGood.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(ColorGood.copy(alpha = alpha))
        )
        Spacer(Modifier.width(5.dp))
        Text("LIVE", fontSize = 10.sp, color = ColorGood, fontWeight = FontWeight.Bold)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bottom Tab Bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun BottomTabBar(
    tabs: List<NavTab>,
    selected: String,
    onSelect: (String) -> Unit
) {
    NavigationBar(
        containerColor = BgSurface,
        tonalElevation = 0.dp,
        modifier       = Modifier
            .navigationBarsPadding()
    ) {
        tabs.forEach { tab ->
            val isSelected = tab.id == selected
            NavigationBarItem(
                selected = isSelected,
                onClick  = { onSelect(tab.id) },
                icon = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(tab.icon, fontSize = if (isSelected) 20.sp else 17.sp)
                        if (isSelected) {
                            Spacer(Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Accent)
                            )
                        }
                    }
                },
                label = {
                    Text(
                        text     = tab.label,
                        fontSize = 9.sp,
                        color    = if (isSelected) Accent else TextMuted,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = Accent,
                    unselectedIconColor = TextMuted,
                    indicatorColor      = AccentGlow
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Loading
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🦆", fontSize = 48.sp)
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator(color = Accent, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(12.dp))
            Text("Lendo hardware...", fontSize = 13.sp, color = TextSecondary)
        }
    }
}
