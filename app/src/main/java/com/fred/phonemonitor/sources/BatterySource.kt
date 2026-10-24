package com.fred.phonemonitor.sources

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.fred.phonemonitor.core.Metric
import com.fred.phonemonitor.core.MetricSource
import java.util.Locale
import kotlin.math.abs

/**
 * 电池数据。
 *
 * 电池温度是本应用唯一能稳定拿到「具体摄氏度」的温度指标——
 * CPU 温度在 Android 10+ 上受 SELinux 限制无法读取。
 */
class BatterySource(private val context: Context) : MetricSource {

    override val key = "battery"
    override val label = "电池"

    override fun read(): List<Metric> {
        // ACTION_BATTERY_CHANGED 是 sticky 广播，传 null receiver 即可取回最近一次值
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?: return listOf(Metric("battery_na", "电池信息", "不可用"))

        val tempRaw = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
        val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val tech = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY).orEmpty()

        return listOf(
            Metric(
                "battery_temp", "电池温度",
                if (tempRaw == Int.MIN_VALUE) "—"
                else String.format(Locale.getDefault(), "%.1f °C", tempRaw / 10.0)
            ),
            Metric(
                "battery_level", "电量",
                if (level < 0 || scale <= 0) "—"
                else String.format(Locale.getDefault(), "%d%%  (%d / %d)", level * 100 / scale, level, scale)
            ),
            Metric(
                "battery_voltage", "电压",
                if (voltageMv < 0) "—"
                else String.format(Locale.getDefault(), "%.3f V", voltageMv / 1000.0)
            ),
            Metric("battery_current", "电流", readCurrent()),
            Metric("battery_health", "健康状态", describeHealth(health)),
            Metric("battery_status", "充电状态", describeStatus(status)),
            Metric("battery_plugged", "供电方式", describePlugged(plugged)),
            Metric("battery_tech", "电池类型", tech.ifBlank { "—" })
        )
    }

    /** 瞬时电流（µA）。正值 = 充电，负值 = 放电。部分 ROM 恒定返回 0。 */
    private fun readCurrent(): String {
        val manager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager ?: return "—"
        val microAmps = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        if (microAmps == Int.MIN_VALUE) return "—"

        val milliAmps = microAmps / 1000.0
        val direction = if (milliAmps > 0) "充电" else "放电"
        return String.format(Locale.getDefault(), "%.0f mA (%s)", abs(milliAmps), direction)
    }

    private fun describeHealth(health: Int): String = when (health) {
        BatteryManager.BATTERY_HEALTH_UNKNOWN -> "未知"
        BatteryManager.BATTERY_HEALTH_GOOD -> "良好"
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "过热"
        BatteryManager.BATTERY_HEALTH_DEAD -> "损坏"
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "过压"
        BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "未指定故障"
        BatteryManager.BATTERY_HEALTH_COLD -> "过冷"
        else -> "—"
    }

    private fun describeStatus(status: Int): String = when (status) {
        BatteryManager.BATTERY_STATUS_CHARGING -> "充电中"
        BatteryManager.BATTERY_STATUS_DISCHARGING -> "放电中"
        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "未充电"
        BatteryManager.BATTERY_STATUS_FULL -> "已充满"
        BatteryManager.BATTERY_STATUS_UNKNOWN -> "未知"
        else -> "—"
    }

    private fun describePlugged(plugged: Int): String = when (plugged) {
        BatteryManager.BATTERY_PLUGGED_AC -> "交流电"
        BatteryManager.BATTERY_PLUGGED_USB -> "USB"
        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "无线"
        0 -> "电池供电"
        else -> "—"
    }
}
