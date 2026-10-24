package com.fred.phonemonitor.sources

import android.content.Context
import android.os.Build
import android.os.PowerManager
import com.fred.phonemonitor.core.Metric
import com.fred.phonemonitor.core.MetricSource

/**
 * 系统热状态等级（API 29+）。
 *
 * 重要：这里只有「等级」，没有具体温度。CPU / GPU 温度需要
 * HardwarePropertiesManager + DEVICE_POWER 系统签名权限，第三方应用拿不到。
 */
class ThermalStatusSource(private val context: Context) : MetricSource {

    override val key = "thermal"
    override val label = "热状态"

    override fun read(): List<Metric> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return listOf(Metric("thermal_na", "系统热状态", "需 Android 10+"))
        }

        val manager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            ?: return listOf(Metric("thermal_na", "系统热状态", "不可用"))

        return listOf(
            Metric("thermal_status", "系统热状态等级", describe(manager.currentThermalStatus)),
            Metric("thermal_note", "说明", "系统仅提供等级，不提供具体温度")
        )
    }

    private fun describe(status: Int): String = when (status) {
        PowerManager.THERMAL_STATUS_NONE -> "正常"
        PowerManager.THERMAL_STATUS_LIGHT -> "轻微"
        PowerManager.THERMAL_STATUS_MODERATE -> "中度"
        PowerManager.THERMAL_STATUS_SEVERE -> "严重"
        PowerManager.THERMAL_STATUS_CRITICAL -> "临界"
        PowerManager.THERMAL_STATUS_EMERGENCY -> "紧急"
        PowerManager.THERMAL_STATUS_SHUTDOWN -> "即将关机"
        else -> "未知"
    }
}
