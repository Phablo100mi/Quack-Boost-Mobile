package com.quackboost.mobile.data

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.TrafficStats
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.os.health.SystemHealthManager
import android.telephony.TelephonyManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.RandomAccessFile
import java.net.NetworkInterface
import java.text.DecimalFormat

// ─────────────────────────────────────────────────────────────────────────────
// Data classes
// ─────────────────────────────────────────────────────────────────────────────

data class CpuInfo(
    val model: String,
    val cores: Int,
    val architecture: String,
    val abi: String,
    val usagePercent: Float,
    val frequencies: List<Long>,        // MHz per core
    val minFreq: Long,
    val maxFreq: Long,
    val governor: String
)

data class MemoryInfo(
    val totalRam: Long,                 // bytes
    val availableRam: Long,
    val usedRam: Long,
    val usagePercent: Float,
    val isLowMemory: Boolean,
    val totalStorage: Long,
    val usedStorage: Long,
    val availableStorage: Long
)

data class ThermalInfo(
    val zones: List<ThermalZone>
)

data class ThermalZone(
    val name: String,
    val tempCelsius: Float
)

data class BatteryInfo(
    val levelPercent: Int,
    val health: String,
    val status: String,
    val tempCelsius: Float,
    val voltageV: Float,
    val currentMa: Long,                // Android 5.0+
    val capacityMah: Long,
    val technology: String,
    val isCharging: Boolean,
    val chargeType: String
)

data class NetworkInfo(
    val ssid: String,
    val bssid: String,
    val ipAddress: String,
    val macAddress: String,
    val signalStrength: Int,           // dBm
    val linkSpeed: Int,                // Mbps
    val frequencyMhz: Int,
    val rxBytes: Long,
    val txBytes: Long,
    val networkType: String
)

data class DeviceInfo(
    val brand: String,
    val model: String,
    val product: String,
    val board: String,
    val hardware: String,
    val androidVersion: String,
    val apiLevel: Int,
    val buildId: String,
    val kernelVersion: String,
    val uptime: String,
    val screenWidth: Int,
    val screenHeight: Int,
    val displayDensity: Int,
    val refreshRate: Float
)

// ─────────────────────────────────────────────────────────────────────────────
// HardwareMonitor
// ─────────────────────────────────────────────────────────────────────────────

class HardwareMonitor(private val context: Context) {

    // ── CPU ─────────────────────────────────────────────────────────────────

    fun getCpuInfo(): CpuInfo {
        val cores = Runtime.getRuntime().availableProcessors()
        val frequencies = mutableListOf<Long>()
        val minFreqs = mutableListOf<Long>()
        val maxFreqs = mutableListOf<Long>()

        for (i in 0 until cores) {
            frequencies.add(readCpuFreq("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq"))
            minFreqs.add(readCpuFreq("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_min_freq"))
            maxFreqs.add(readCpuFreq("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq"))
        }

        val governor = readFile("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor") ?: "unknown"
        val model    = getCpuModelName()
        val abi      = Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown"
        val arch     = if (abi.contains("arm64")) "AArch64" else if (abi.contains("arm")) "ARM" else "x86"

        return CpuInfo(
            model        = model,
            cores        = cores,
            architecture = arch,
            abi          = abi,
            usagePercent = readCpuUsage(),
            frequencies  = frequencies,
            minFreq      = minFreqs.maxOrNull() ?: 0L,
            maxFreq      = maxFreqs.maxOrNull() ?: 0L,
            governor     = governor.trim()
        )
    }

    private fun getCpuModelName(): String {
        return try {
            val reader = BufferedReader(FileReader("/proc/cpuinfo"))
            var line: String?
            var hardware = ""
            var processor = ""
            while (reader.readLine().also { line = it } != null) {
                when {
                    line!!.startsWith("Hardware") -> hardware = line!!.substringAfter(":").trim()
                    line!!.startsWith("model name") -> processor = line!!.substringAfter(":").trim()
                    line!!.startsWith("Processor") && processor.isEmpty() ->
                        processor = line!!.substringAfter(":").trim()
                }
            }
            reader.close()
            when {
                hardware.isNotEmpty() -> hardware
                processor.isNotEmpty() -> processor
                else -> Build.HARDWARE
            }
        } catch (e: Exception) { Build.HARDWARE }
    }

    private var lastCpuTotal = 0L
    private var lastCpuIdle  = 0L

    private fun readCpuUsage(): Float {
        return try {
            val reader = RandomAccessFile("/proc/stat", "r")
            val line   = reader.readLine()
            reader.close()
            val toks = line.trim().split("\\s+".toRegex())
            // cpu user nice system idle iowait irq softirq
            val user   = toks[1].toLong()
            val nice   = toks[2].toLong()
            val system = toks[3].toLong()
            val idle   = toks[4].toLong()
            val iowait = if (toks.size > 5) toks[5].toLong() else 0L
            val irq    = if (toks.size > 6) toks[6].toLong() else 0L
            val softirq= if (toks.size > 7) toks[7].toLong() else 0L

            val total  = user + nice + system + idle + iowait + irq + softirq
            val diffTotal = total - lastCpuTotal
            val diffIdle  = idle - lastCpuIdle

            lastCpuTotal = total
            lastCpuIdle  = idle

            if (diffTotal == 0L) return 0f
            ((diffTotal - diffIdle).toFloat() / diffTotal.toFloat() * 100f)
                .coerceIn(0f, 100f)
        } catch (e: Exception) { 0f }
    }

    private fun readCpuFreq(path: String): Long {
        return try {
            val v = readFile(path)?.trim()?.toLongOrNull() ?: 0L
            v / 1000L // kHz -> MHz
        } catch (e: Exception) { 0L }
    }

    // ── Memory ──────────────────────────────────────────────────────────────

    fun getMemoryInfo(): MemoryInfo {
        val am  = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi  = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)

        val used      = mi.totalMem - mi.availMem
        val pct       = used.toFloat() / mi.totalMem.toFloat() * 100f

        val storagePath = Environment.getDataDirectory()
        val sf          = StatFs(storagePath.path)
        val totalStorage = sf.totalBytes
        val freeStorage  = sf.availableBytes

        return MemoryInfo(
            totalRam        = mi.totalMem,
            availableRam    = mi.availMem,
            usedRam         = used,
            usagePercent    = pct,
            isLowMemory     = mi.lowMemory,
            totalStorage    = totalStorage,
            usedStorage     = totalStorage - freeStorage,
            availableStorage = freeStorage
        )
    }

    // ── Thermal ─────────────────────────────────────────────────────────────

    fun getThermalInfo(): ThermalInfo {
        val zones = mutableListOf<ThermalZone>()

        // Read from sysfs thermal zones
        val thermalDir = File("/sys/class/thermal")
        if (thermalDir.exists()) {
            thermalDir.listFiles()
                ?.filter { it.name.startsWith("thermal_zone") }
                ?.sortedBy { it.name.removePrefix("thermal_zone").toIntOrNull() ?: 999 }
                ?.take(16)
                ?.forEach { dir ->
                    try {
                        val typeFile = File(dir, "type")
                        val tempFile = File(dir, "temp")
                        if (typeFile.exists() && tempFile.exists()) {
                            val type  = typeFile.readText().trim()
                            val rawTemp = tempFile.readText().trim().toLongOrNull() ?: 0L
                            // Most sensors report in millidegrees
                            val celsius = when {
                                rawTemp > 1000 -> rawTemp / 1000f
                                rawTemp > 200  -> rawTemp / 10f
                                else           -> rawTemp.toFloat()
                            }
                            if (celsius in -50f..200f) {
                                zones.add(ThermalZone(prettifyZoneName(type), celsius))
                            }
                        }
                    } catch (_: Exception) {}
                }
        }

        // Fallback: SensorManager temperature sensor
        if (zones.isEmpty()) {
            val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            sm.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)?.let {
                zones.add(ThermalZone("Ambiente", 0f)) // populated by SensorEventListener in VM
            }
        }

        return ThermalInfo(zones = zones)
    }

    private fun prettifyZoneName(raw: String): String {
        return when {
            raw.contains("cpu", true)     -> "CPU"
            raw.contains("gpu", true)     -> "GPU"
            raw.contains("batt", true)    -> "Bateria"
            raw.contains("npu", true)     -> "NPU"
            raw.contains("dsp", true)     -> "DSP"
            raw.contains("skin", true)    -> "Superfície"
            raw.contains("board", true)   -> "Placa"
            raw.contains("charger", true) -> "Carregador"
            raw.contains("pa", true)      -> "Amplificador"
            raw.contains("mdm", true)     -> "Modem"
            raw.contains("cam", true)     -> "Câmera"
            else -> raw.take(20).replaceFirstChar { it.uppercase() }
        }
    }

    // ── Battery ─────────────────────────────────────────────────────────────

    fun getBatteryInfo(): BatteryInfo {
        val intent = context.registerReceiver(null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED))!!

        val level   = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale   = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val pct     = if (scale > 0) (level * 100 / scale) else 0
        val health  = healthString(intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0))
        val status  = statusString(intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0))
        val temp    = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000f
        val tech    = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-Ion"
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        val isCharging = plugged != 0
        val chargeType = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC     -> "AC"
            BatteryManager.BATTERY_PLUGGED_USB    -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "—"
        }

        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val currentMa = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            bm.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) / 1000L
        } else 0L
        val capacityMah = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            bm.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) / 1000L
        } else 0L

        return BatteryInfo(
            levelPercent = pct,
            health       = health,
            status       = status,
            tempCelsius  = temp,
            voltageV     = voltage,
            currentMa    = Math.abs(currentMa),
            capacityMah  = capacityMah,
            technology   = tech,
            isCharging   = isCharging,
            chargeType   = chargeType
        )
    }

    private fun healthString(v: Int) = when (v) {
        BatteryManager.BATTERY_HEALTH_GOOD              -> "Bom"
        BatteryManager.BATTERY_HEALTH_OVERHEAT          -> "Superaquecido"
        BatteryManager.BATTERY_HEALTH_DEAD               -> "Morta"
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE       -> "Sobretensão"
        BatteryManager.BATTERY_HEALTH_COLD               -> "Fria"
        BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Falha"
        else -> "Desconhecido"
    }

    private fun statusString(v: Int) = when (v) {
        BatteryManager.BATTERY_STATUS_CHARGING    -> "Carregando"
        BatteryManager.BATTERY_STATUS_DISCHARGING -> "Descarregando"
        BatteryManager.BATTERY_STATUS_FULL        -> "Completa"
        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Não carregando"
        else -> "Desconhecido"
    }

    // ── Network ─────────────────────────────────────────────────────────────

    fun getNetworkInfo(): NetworkInfo {
        val wm = context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wm.connectionInfo

        val rawSsid = wifiInfo?.ssid ?: "<desconhecido>"
        val ssid = rawSsid.removePrefix("\"").removeSuffix("\"")

        val ipInt = wifiInfo?.ipAddress ?: 0
        val ipStr = if (ipInt != 0) {
            String.format(
                "%d.%d.%d.%d",
                ipInt and 0xff,
                (ipInt shr 8) and 0xff,
                (ipInt shr 16) and 0xff,
                (ipInt shr 24) and 0xff
            )
        } else "—"

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netType = cm.activeNetworkInfo?.typeName ?: "Nenhuma"

        val uid = android.os.Process.myUid()

        return NetworkInfo(
            ssid          = if (ssid == "<unknown ssid>") "—" else ssid,
            bssid         = wifiInfo?.bssid ?: "—",
            ipAddress     = ipStr,
            macAddress    = getMacAddress(),
            signalStrength = wifiInfo?.rssi ?: 0,
            linkSpeed     = wifiInfo?.linkSpeed ?: 0,
            frequencyMhz  = if (Build.VERSION.SDK_INT >= 21) wifiInfo?.frequency ?: 0 else 0,
            rxBytes       = TrafficStats.getUidRxBytes(uid),
            txBytes       = TrafficStats.getUidTxBytes(uid),
            networkType   = netType
        )
    }

    private fun getMacAddress(): String {
        return try {
            NetworkInterface.getNetworkInterfaces()
                ?.toList()
                ?.find { it.name.equals("wlan0", true) }
                ?.hardwareAddress
                ?.joinToString(":") { String.format("%02X", it) }
                ?: "—"
        } catch (e: Exception) { "—" }
    }

    // ── Device ──────────────────────────────────────────────────────────────

    fun getDeviceInfo(): DeviceInfo {
        val dm = context.resources.displayMetrics
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
        val refreshRate = wm.defaultDisplay?.refreshRate ?: 60f

        val uptimeMs = SystemClock.elapsedRealtime()
        val hours   = uptimeMs / 3600000
        val minutes = (uptimeMs % 3600000) / 60000
        val uptimeStr = "${hours}h ${minutes}m"

        val kernelVersion = try {
            readFile("/proc/version")
                ?.substringAfter("version ")
                ?.substringBefore(" (")
                ?.take(30) ?: "—"
        } catch (_: Exception) { "—" }

        return DeviceInfo(
            brand         = Build.BRAND.replaceFirstChar { it.uppercase() },
            model         = Build.MODEL,
            product       = Build.PRODUCT,
            board         = Build.BOARD,
            hardware      = Build.HARDWARE,
            androidVersion = Build.VERSION.RELEASE,
            apiLevel      = Build.VERSION.SDK_INT,
            buildId       = Build.DISPLAY,
            kernelVersion = kernelVersion ?: "—",
            uptime        = uptimeStr,
            screenWidth   = dm.widthPixels,
            screenHeight  = dm.heightPixels,
            displayDensity = dm.densityDpi,
            refreshRate   = refreshRate
        )
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private fun readFile(path: String): String? {
        return try { File(path).readText() } catch (_: Exception) { null }
    }

    companion object {
        fun formatBytes(bytes: Long): String {
            val df = DecimalFormat("#.##")
            return when {
                bytes >= 1_073_741_824L -> "${df.format(bytes / 1_073_741_824.0)} GB"
                bytes >= 1_048_576L     -> "${df.format(bytes / 1_048_576.0)} MB"
                bytes >= 1024L          -> "${df.format(bytes / 1024.0)} KB"
                else                    -> "$bytes B"
            }
        }

        fun tempColor(c: Float): androidx.compose.ui.graphics.Color = when {
            c >= 80 -> com.quackboost.mobile.ui.theme.ColorDanger
            c >= 60 -> com.quackboost.mobile.ui.theme.ColorWarn
            else    -> com.quackboost.mobile.ui.theme.ColorGood
        }
    }
}
