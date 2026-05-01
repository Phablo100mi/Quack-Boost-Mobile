package com.quackboost.mobile.viewmodel

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quackboost.mobile.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class HardwareState(
    val cpu:     CpuInfo?     = null,
    val memory:  MemoryInfo?  = null,
    val thermal: ThermalInfo? = null,
    val battery: BatteryInfo? = null,
    val network: NetworkInfo? = null,
    val device:  DeviceInfo?  = null,
    val isLoading: Boolean    = true
)

class HardwareViewModel(application: Application) : AndroidViewModel(application) {

    private val monitor = HardwareMonitor(application.applicationContext)

    private val _state = MutableStateFlow(HardwareState())
    val state: StateFlow<HardwareState> = _state.asStateFlow()

    // Ambient temperature from SensorManager
    private var ambientTemp = 0f
    private var sensorListener: SensorEventListener? = null

    init {
        registerAmbientTempSensor()
        startPolling()
    }

    private fun registerAmbientTempSensor() {
        val sm = getApplication<Application>()
            .getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager
        val sensor = sm.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        if (sensor != null) {
            sensorListener = object : SensorEventListener {
                override fun onSensorChanged(e: SensorEvent) { ambientTemp = e.values[0] }
                override fun onAccuracyChanged(s: Sensor?, a: Int) {}
            }
            sm.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun startPolling() {
        viewModelScope.launch(Dispatchers.IO) {
            // Initial fast load
            loadAll()

            // Poll every 2 seconds
            while (isActive) {
                delay(2000)
                loadAll()
            }
        }
    }

    private suspend fun loadAll() {
        try {
            val cpu     = withContext(Dispatchers.IO) { monitor.getCpuInfo() }
            val memory  = withContext(Dispatchers.IO) { monitor.getMemoryInfo() }
            val thermal = withContext(Dispatchers.IO) {
                var t = monitor.getThermalInfo()
                // Inject ambient sensor if available and zones are empty
                if (t.zones.isEmpty() && ambientTemp != 0f) {
                    t = t.copy(zones = listOf(ThermalZone("Ambiente", ambientTemp)))
                }
                t
            }
            val battery = withContext(Dispatchers.IO) { monitor.getBatteryInfo() }
            val network = withContext(Dispatchers.IO) { monitor.getNetworkInfo() }
            val device  = withContext(Dispatchers.IO) { monitor.getDeviceInfo() }

            _state.update {
                it.copy(
                    cpu = cpu, memory = memory, thermal = thermal,
                    battery = battery, network = network, device = device,
                    isLoading = false
                )
            }
        } catch (_: Exception) {
            _state.update { it.copy(isLoading = false) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorListener?.let {
            val sm = getApplication<Application>()
                .getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager
            sm.unregisterListener(it)
        }
    }
}
