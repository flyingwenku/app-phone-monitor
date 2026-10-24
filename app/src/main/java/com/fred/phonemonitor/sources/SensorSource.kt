package com.fred.phonemonitor.sources

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import com.fred.phonemonitor.core.Metric
import com.fred.phonemonitor.core.MetricSource

/** 设备传感器清单，温度类传感器会额外标注。 */
class SensorSource(private val context: Context) : MetricSource {

    override val key = "sensors"
    override val label = "传感器"

    override fun read(): List<Metric> {
        val manager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            ?: return listOf(Metric("sensor_na", "传感器", "不可用"))

        val sensors = manager.getSensorList(Sensor.TYPE_ALL)
        if (sensors.isEmpty()) {
            return listOf(Metric("sensor_none", "传感器", "未检测到"))
        }

        return sensors.map { sensor ->
            val isTemp = isTemperatureSensor(sensor.type)
            Metric(
                key = "sensor_${sensor.type}_${sensor.name}",
                label = "${if (isTemp) "[温度] " else ""}${sensor.name}",
                value = "类型 ${sensor.type} · ${sensor.vendor}"
            )
        }
    }

    private fun isTemperatureSensor(type: Int): Boolean =
        type == Sensor.TYPE_AMBIENT_TEMPERATURE || type == Sensor.TYPE_TEMPERATURE
}
