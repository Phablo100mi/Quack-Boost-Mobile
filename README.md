# 🦆 QuackBoost Mobile v3.1

Monitor de hardware Android estilo CPU-Z, com visual do QuackBoost.

## Funcionalidades

| Aba | O que monitora |
|-----|---------------|
| 📱 Dispositivo | Marca, modelo, Android, kernel, tela, uptime |
| 🔥 CPU | Uso (gauge), modelo, arquitetura, frequência por núcleo, governor |
| 💾 Memória | Uso da RAM (gauge + barra), armazenamento interno |
| 🌡️ Thermal | Todas as zonas térmicas do `/sys/class/thermal/` em tempo real |
| 🔋 Bateria | Nível, saúde, status, temperatura, tensão, corrente, capacidade |
| 📶 Rede | SSID, IP, MAC, sinal (dBm), velocidade do link, tráfego |

## Como compilar (Android Studio)

### Pré-requisitos
- **Android Studio Hedgehog** ou mais recente
- **JDK 17** (já vem com o Android Studio)
- **Android SDK 34** (API 34)
- Dispositivo ou emulador Android 8.0+ (API 26+)

### Passos

1. **Abra o projeto**
   ```
   File → Open → selecione a pasta QuackBoostMobile
   ```

2. **Sincronize o Gradle**
   - Aguarde a sincronização automática, ou clique em **Sync Now** no banner

3. **Configure o `local.properties`** (automático, mas se necessário):
   ```
   sdk.dir=C:/Users/SeuNome/AppData/Local/Android/Sdk   # Windows
   sdk.dir=/Users/SeuNome/Library/Android/sdk            # macOS
   ```

4. **Build APK de debug**
   ```
   Build → Build Bundle(s) / APK(s) → Build APK(s)
   ```
   O APK ficará em:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

5. **Build APK de release (assinado)**
   ```
   Build → Generate Signed Bundle / APK → APK
   ```

### Linha de comando
```bash
# Debug APK
./gradlew assembleDebug

# Release APK  
./gradlew assembleRelease
```

## Arquitetura

```
QuackBoostMobile/
├── app/src/main/
│   ├── java/com/quackboost/mobile/
│   │   ├── MainActivity.kt              ← Entry point + navegação por abas
│   │   ├── data/
│   │   │   └── HardwareMonitor.kt       ← Leitura de todo o hardware
│   │   ├── viewmodel/
│   │   │   └── HardwareViewModel.kt     ← Polling a cada 2s, StateFlow
│   │   └── ui/
│   │       ├── theme/Theme.kt           ← Cores QuackBoost, tipografia
│   │       ├── components/Components.kt ← ArcGauge, InfoCard, TempBadge...
│   │       └── screens/
│   │           ├── CpuScreen.kt
│   │           ├── MemoryScreen.kt
│   │           ├── ThermalScreen.kt
│   │           ├── BatteryScreen.kt
│   │           ├── NetworkScreen.kt
│   │           └── DeviceScreen.kt
│   └── res/
│       └── ...
```

## Stack Técnica

- **Kotlin** + **Jetpack Compose** + **Material3**
- **ViewModel** + **StateFlow** para reactive UI
- **Hardware:** `/proc/stat`, `/proc/cpuinfo`, `/sys/class/thermal/`, BatteryManager, ActivityManager, WifiManager, TrafficStats
- Atualização automática a cada **2 segundos**
- Animações suaves nos gauges e barras de progresso

## Créditos
- **Criado por:** Phablo100mi
- **Comunidade:** Liliko's Party
- **Discord:** https://discord.gg/dDx5BNxATQ
- **Copyright:** 2026 Liliko's Party
