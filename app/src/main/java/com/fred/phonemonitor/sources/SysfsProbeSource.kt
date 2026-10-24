package com.fred.phonemonitor.sources

import com.fred.phonemonitor.core.Metric
import com.fred.phonemonitor.core.MetricSource
import java.io.File

/**
 * Sysfs 探测面板。
 *
 * 目的：不同厂商 ROM 的限制不同，本页逐条尝试读取常见 thermal / cpufreq /
 * power_supply 路径，把「可读 / 不可读 / 不存在」直接显示出来，
 * 用于在真机上实证到底能拿到什么，而不是依赖猜测。
 */
class SysfsProbeSource : MetricSource {

    override val key = "probe"
    override val label = "Sysfs 探测"

    private val paths: List<String> = buildList {
        // 温度：thermal zone 0 ~ 9
        for (i in 0..9) {
            add("/sys/class/thermal/thermal_zone$i/temp")
            add("/sys/class/thermal/thermal_zone$i/type")
        }
        // CPU 频率
        for (cpu in listOf(0, 4, 7)) {
            add("/sys/devices/system/cpu/cpu$cpu/cpufreq/scaling_cur_freq")
            add("/sys/devices/system/cpu/cpu$cpu/cpufreq/cpuinfo_max_freq")
            add("/sys/devices/system/cpu/cpu$cpu/cpufreq/scaling_available_frequencies")
        }
        // 电池 sysfs（可验证电流、温度是否另有来源）
        add("/sys/class/power_supply/battery/current_now")
        add("/sys/class/power_supply/battery/voltage_now")
        add("/sys/class/power_supply/battery/temp")
        add("/sys/class/power_supply/battery/charge_counter")
        // MTK 平台备用温度路径（荣耀实测 /sys/class/thermal 下无节点）
        add("/proc/driver/thermal/temperature1")
        add("/proc/driver/thermal/temperature")
        add("/sys/class/hwmon/hwmon0/temp1_input")
        add("/sys/devices/virtual/thermal/thermal_zone0/temp")
    }

    override fun read(): List<Metric> = paths.map { path ->
        val result = probe(path)
        Metric(
            key = "probe_$path",
            label = path.removePrefix("/sys/"),
            value = result
        )
    }

    private fun probe(path: String): String = try {
        val file = File(path)
        when {
            !file.exists() -> "不存在"
            !file.canRead() -> "不可读（权限拒绝）"
            else -> {
                val raw = file.readText().trim()
                if (raw.isEmpty()) "可读（空内容）" else "可读：$raw"
            }
        }
    } catch (e: Exception) {
        "不可读：${e.javaClass.simpleName}"
    }
}
